package org.adbs.dbxic.storage;

import org.adbs.dbxic.catalog.Catalog;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Table;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.FileHandling;
import org.adbs.dbxic.utils.TypeCasting;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * StorageManager: provides low-level I/O management of files and blocks
 * TODO: we should check the use of 'dirty' flag (writeBlock, readBlock) and
 *  maybe use it to decide if evicted blocks need to be written back to disk or not
 */
public class StorageManager {

    private Catalog catalog;
    private BufferManager buffer;

    /**
     * Constructor: creates a new storage manager with a catalog and a buffer manager.
     */
    public StorageManager(Catalog catalog, BufferManager buffer) {
        this.catalog = catalog;
        this.buffer = buffer;
    } // StorageManager()


    /**
     * registerBlock: registers a block within the block buffer
     */
    public void registerBlock(Block block) throws DBxicException {
        //buffer.putBlock(block);
        writeBlock(block);
    } // registerBlock()

    
    /**
     * writeBlock: Writes a block to disk.
     */
    public synchronized void writeBlock(Block block) throws DBxicException {
        
        try {
            // Put the block in the buffer. If a block has been evicted, write it to disk
            Block evictedBlock = buffer.putBlock(block, false);
            if (evictedBlock != null) {
                String fn = evictedBlock.getFileName();
                RandomAccessFile raf = new RandomAccessFile(fn, FileHandling.READ_WRITE);
                BlockIOManager.writeBlock(raf, evictedBlock);
                raf.close();
            }
        }
        catch (Exception e) {
            throw new DBxicException("Error: could not write block to disk.", e);
        }
    } // writeBlock()

    
    /**
     * readBlock: reads a block from the database given the block's identifier.
     */
    public synchronized Block readBlock(Relation relation, String filename, int block_number) throws DBxicException {
        
        try {
            // if the buffer has the block read it from there
            if (buffer.containsBlock(filename, block_number)) {
                return buffer.getBlock(filename, block_number);
            }
            // otherwise read it from file and put it in the buffer pool
            else {
                RandomAccessFile raf = new RandomAccessFile(filename, FileHandling.READ_WRITE);
                Block block = BlockIOManager.readBlock(relation, filename, block_number, raf);
                raf.close();
                Block evictedBlock = buffer.putBlock(block, true);
                if (evictedBlock != null) {
                    String fn = evictedBlock.getFileName();
                    raf = new RandomAccessFile(fn, FileHandling.READ_WRITE);
                    BlockIOManager.writeBlock(raf, evictedBlock);
                    raf.close();
                }
                return block;
            }
        }
        catch (Exception e) {
            throw new DBxicException("Error: couldn't read block from disk.", e);
        }
    } // readBlock()


    /**
     * createTable: Creates a new table in the database.
     */
    public void createTable(Table table) throws DBxicException {
        try {
            catalog.addTable(table);
            createFile(catalog.getTableFileName(table.getName()));
        }
        catch (IllegalArgumentException iae) {
            throw new DBxicException("Error: A table with " + table.getName() + " already exists", iae);
        }
        catch (NoSuchElementException nsee) {
            throw new DBxicException("Error: Could not retrieve table " + table.getName() + ", which was just created", nsee);
        }
    } // createTable()
    
    
    /**
     * deleteTable: delete a table from the database
     */
    public void deleteTable(String tableName) throws NoSuchElementException, DBxicException {
        deleteFile(catalog.getTableFileName(tableName));
        catalog.deleteTable(tableName);
    } // deleteTable()


    /**
     * createFile: creates a new file with the given file name.
     */
    public void createFile(String fileName) throws DBxicException {
        try {
            File file = new File(fileName);
            if (! file.createNewFile()) {
                file.delete();
                file.createNewFile();
            } 
        }
        catch (Exception e) {
            throw new DBxicException("Error: Could not create file " + fileName, e);
        }
    } // createFile()

    
    /**
     * deleteFile: deletes file with the given file name and removes its blocks from buffer (if any)
     */
    public void deleteFile(String fileName) throws DBxicException {
        try {
            buffer.removeAll(fileName); // remove all the file's blocks that might be at buffer
            File file = new File(fileName);
            file.delete();
        }
        catch (Exception e) {
            throw new DBxicException("Error! Could not delete file " + fileName, e);
        }
    } // deleteFile()    



    /**
     * insertTuple: inserts a new tuple into a table, given a table name and a tuple
     */
    public void insertTuple(String tablename, Tuple tuple) throws NoSuchElementException, DBxicException {
        Table table = catalog.getTable(tablename);
        String filename = catalog.getTableFileName(tablename);
        insertTuple(table, filename, tuple);
    } // insertTuple()


    /**
     * insertTuple: inserts a new tuple into a table, given a table name and a list of values.
     */
    public void insertTuple(String tablename, List<Comparable> values) throws NoSuchElementException, DBxicException {
        insertTuple(tablename, new Tuple(null, 0, values));
    } // insertTuple()

    //public void insertTuple(Relation relation, String fileName, Tuple tuple) throws DBxicException {


    /**
     * castAndInsertTuple: casts the list of values to the types of the relation before inserting the tuple.
     */
    public void castAndInsertTuple(String tablename, Tuple tuple) throws NoSuchElementException, DBxicException {
        Table table = catalog.getTable(tablename);
        String filename = catalog.getTableFileName(tablename);
        castAttributes(tuple, table);
        insertTuple(table, filename, tuple);
    } // castAndInsertTuple()

    /**
     * castAndInsertTuple: casts the list of values to the types of the relation before inserting the tuple.
     */
    public void castAndInsertTuple(String tablename, List<Comparable> values) throws NoSuchElementException, DBxicException {
        castAndInsertTuple(tablename, new Tuple(null, 0, values));
    } // castAndInsertTuple()



    /**
     * insertTuple: inserts a new tuple into the relation with the given file.
     */
    public void insertTuple(Relation relation, String relationFileName, Tuple tuple) throws DBxicException {

        try {
            // read in the last block of the file
            int blockNum = FileHandling.getNumberOfBlocks(relationFileName);
            blockNum = (blockNum == 0) ? 0 : blockNum-1;
            Block block = readBlock(relation, relationFileName, blockNum);
            // calculate the number id that we will assign to the tuple
            int num = 0;
            if (block.getNumberOfTuples() != 0) {
                Tuple t = block.getTuple(block.getNumberOfTuples()-1);
                num = t.getIdNumber()+1;
            }
            tuple.setFilename(relationFileName);
            tuple.setIdNumber(num);
            // save the tuple in a block (if the last one is full, create a new one)
            if (!block.hasRoomFor(tuple)) {
                block = new Block(relation, relationFileName, ++blockNum);
                FileHandling.setNumberOfBlocks(relationFileName, blockNum+1);
            }
            block.addTuple(tuple);
            writeBlock(block);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new DBxicException("I/O Error while inserting tuple to file: " + relationFileName +
                    " (" + e.getMessage() + ")", e);
        }
    } // insertTuple ()


    /**
     * insertTupleInPosition: inserts a new tuple into the relation with the given file in a specific position.
     */
    public void insertTupleInPosition(Relation relation, String relFileName, Tuple tuple, int position)
            throws DBxicException {

        try {
            // read in the last block of the file
            int numBlocks = FileHandling.getNumberOfBlocks(relFileName);
            //numBlocks = (numBlocks == 0) ? 0 : numBlocks-1;
            // Find in which block, initially, goes the tuple, and its position in it
            int respBlock = 0, inBlockPosition=position;
            for (int i = 0; i < (numBlocks-1); i++) {
                int actBlockSize = readBlock(relation, relFileName, respBlock).getNumberOfTuples();
                if (inBlockPosition < actBlockSize) {
                    break;
                }
                else {
                    respBlock++;
                    inBlockPosition -= actBlockSize;
                }
            }
            ArrayList<Tuple> lTuples = new ArrayList<>();
            lTuples.add(tuple);
            while (!lTuples.isEmpty()) { // account for overflow of tuples
                Block block = readBlock(relation, relFileName, respBlock++); // if doesn't exist, creates it
                lTuples = block.addTuplesInPositionWithOverflow(lTuples, inBlockPosition);
                // TODO: update the number id of the tuples which have been moved through blocks
                writeBlock(block);
                inBlockPosition=0; // in the rest of block, overflow tuples always go to first position
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new DBxicException("Error: couldn't insert tuple in position to file: " + relFileName, e);
        }
    } // insertTupleInPosition ()


    /**
     * updateTupleInPosition: updates the tuple in a specific position from the relation with the given file.
     * It doesn't assume anything about the tuples (fixed-size, etc.)
     */
    public void updateTupleInPosition(Relation relation, String relFileName, Tuple tuple, int position)
            throws DBxicException {

        try {
            // Find in which block goes the tuple, and its position in it
            int respBlock = 0, inBlockPosition=position;
            boolean not_this_block = true;
            Block block = null;
            while (not_this_block) {
                block = readBlock(relation, relFileName, respBlock);
                int actBlockSize = block.getNumberOfTuples();
                // TODO: how to know which is the correct block to insert the tuple into?
                // make use of all the variables defined in the context
            }
            block.setTuple(inBlockPosition,tuple);
            writeBlock(block);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new DBxicException("Error: couldn't insert tuple in position to file: " + relFileName, e);
        }
    } // updateTupleInPosition()

    /**
     * castAttributes: cast the values of the tuple to the types of the relation
     */
    void castAttributes(Tuple tuple, Relation relation) throws DBxicException {
        TypeCasting.castValuesToTypes(tuple.getValues(), relation.getTypes());
    }


    /**
     * Close session and save everything to disk.
     */
    public void close() throws DBxicException {
        try {
            for (Block block : buffer.blocks()) {
                String fn = block.getFileName();
                RandomAccessFile raf = new RandomAccessFile(fn, FileHandling.READ_WRITE);
                BlockIOManager.writeBlock(raf, block);
                raf.close();
            }
        }
        catch (Exception e) {
            throw new DBxicException("Error! Couldn't shut down the storage manager: " + e.getMessage(), e);
        }
    } // shutdown()

    /**
     * getNumberOfBlocksInBuffer: returns the number of blocks in the buffer
     */
    public int getNumberOfBlocksInBuffer() {
        return buffer.getNumberOfBlocks();
    } // getNumberOfBlocksInBuffer()



    /**
     * Opens a tuple iterator over this relation.
     */
    public Iterable<Tuple> tuples(Relation relation, String filename) throws IOException, DBxicException {
        return new TupleIteratorWrapper(relation, filename);
    } // tuples()

    /**
     * Opens a block iterator over this relation.
     */
    public Iterable<Block> blocks(Relation relation, String filename) throws IOException, DBxicException {
        return new BlockIteratorWrapper(relation, filename);
    } // tuples()


    /**
     * BlockIteratorWrapper: basic iterator over blocks of this relation.
     */
    class BlockIteratorWrapper implements Iterable<Block> {
        private Block currentBlock;
        private int numBlocks;
        private Relation relation;
        private String filename;
        private int blockOffset;

        /**
         * Constructor: creates a new block iterator for this relation
         */
        public BlockIteratorWrapper(Relation relation, String filename) throws IOException {
            this.relation = relation;
            this.filename = filename;
            blockOffset = 0;
            numBlocks = FileHandling.getNumberOfBlocks(filename);
        } // BlockIteratorWrapper()

        /**
         * iterator: returns an iterator over blocks.
         */
        public Iterator<Block> iterator() {
            return new Iterator<Block>() {
                public boolean hasNext() { return blockOffset < numBlocks; }
                public Block next() throws NoSuchElementException {
                    try {
                        currentBlock = readBlock(relation, filename, blockOffset++);
                        return currentBlock;
                    }
                    catch (DBxicException sme) {
                        throw new NoSuchElementException("Error: could not read block to advance the iterator.");
                    }
                } // next()
                public void remove() throws UnsupportedOperationException {
                    throw new UnsupportedOperationException("Error: block removal from iterator not supported.");
                } // remove()
            }; // new Iterator
        } // iterator()
    } // BlockIteratorWrapper


    /**
     * TupleIteratorWrapper: basic iterator over tuples of this relation.
     */
    class TupleIteratorWrapper implements Iterable<Tuple> {
        private Iterator<Block> blocks;
        private Iterator<Tuple> tuples;
        private boolean more;

        /**
         * Constructor: creates a new tuple iterator.
         */
        public TupleIteratorWrapper(Relation relation, String filename) throws IOException, DBxicException {
            blocks = (new BlockIteratorWrapper(relation, filename)).iterator();
            more = false;
            while (blocks.hasNext() && !more) {
                tuples = blocks.next().iterator();
                more = tuples.hasNext();
            }
        } // TupleIterator()

        /**
         * iterator: returns an iterator over tuples in this relation.
         */
        public Iterator<Tuple> iterator() {
            return new Iterator<Tuple>() {
                public boolean hasNext() { return more; }
                public Tuple next() throws NoSuchElementException {
                    Tuple tuple = tuples.next();
                    if (tuples.hasNext()) more = true;
                    else {
                        more = false;
                        while (blocks.hasNext() && !more) {
                            tuples = blocks.next().iterator();
                            more = tuples.hasNext();
                        }
                    }
                    return tuple;
                } // next()
                public void remove() throws UnsupportedOperationException {
                    throw new UnsupportedOperationException("Error: tuple removal from iterator not supported.");
                } // remove()
            }; // new Iterator
        } // iterator()
    } // TupleIteratorWrapper

} // StorageManager
