package org.adbs.dbxic.storage;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.ByteConversion;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.Pair;
//import org.dejave.util.Pair;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * blockIOManager: class with static methods that help with the low-level I/O operations of blocks.
 */
public class BlockIOManager {
	
    /**
     * Constructs a new block I/O manager.
     */
    public BlockIOManager() {}


    /****************************************** WRITE OPERATIONS ******************************************/

    /**
     * writeBlock: writes a block to an output file.
     */
    public static void writeBlock(RandomAccessFile raf, Block block) throws DBxicException {
        
        try {
            // seek to the correct place of the block in the file
            long seek = block.getBlockNumber() * Block.BLOCK_SIZE;
            raf.seek(seek);
            byte [] bytes = new byte[Block.BLOCK_SIZE];
            dumpNumber(block, bytes);
            dumpTuples(block, bytes);
            raf.write(bytes);
        }
        catch (IOException ioe) {
            throw new DBxicException("Exception while writing block [" +
                    block.getFileName() + ":" + block.getBlockNumber() + "] to disk.", ioe);
        }
    } // writeBlock()


    /**
     * dumpNumber: transforms the number of tuples of the block into bytes and save it into the given array,
     * which can then be used to write to disk
     */     
    protected static void dumpNumber(Block block, byte [] bytes) {
        byte [] b = ByteConversion.toByte(block.getNumberOfTuples());
        System.arraycopy(b, 0, bytes, 0, b.length);
    } // dumpNumber()

    
    /**
     * dumpTuples: transforms the tuples of the block into bytes and save them into the given array,
     * which can then be used to write to disk
     */
    protected static void dumpTuples(Block block, byte [] bytes) throws DBxicException {

        // initial offset (int size) as we firstly save the number of tuples
        int offset = ByteConversion.INT_SIZE;
        // iterate over all tuples, place them in the array
        for (Tuple tuple : block)
            offset = dumpTuple(block.getRelation(), tuple, bytes, offset);
        pad(bytes, offset);
    } // dumpTuples()

    /**
     * dumpTuple: transforms a tuple of the given relation into bytes and save it into the given array (,
     * which can then be used to write to disk
     */
    public static int dumpTuple(Relation relation, Tuple tuple, byte[] bytes, int offset) throws DBxicException {

        // write the tuple id
        byte [] b = ByteConversion.toByte(tuple.getIdNumber());
        System.arraycopy(b, 0, bytes, offset, b.length);
        offset += b.length;
        b = null;

        int slot = 0;
        for (Attribute attr : relation)
            offset = dumpSlot(attr.getType(), tuple, slot++, bytes, offset);
        return offset;
    } // dumpTuple()


    /**
     * dumpSlot: low-level method to convert a tuple slot into a byte array.
     */
    protected static int dumpSlot(Class<? extends Comparable> type, Tuple tuple, int slot, byte[] bytes, int start)
            throws DBxicException {

        if (type.equals(Character.class)) {
            byte [] b = ByteConversion.toByte(tuple.asChar(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(Byte.class)) {
            bytes[start] = tuple.asByte(slot);
            return start+1;
        }
        else if (type.equals(Short.class)) {
            byte [] b = ByteConversion.toByte(tuple.asShort(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(Integer.class)) {
            byte [] b = ByteConversion.toByte(tuple.asInt(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(Long.class)) {
            byte [] b = ByteConversion.toByte(tuple.asLong(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(Float.class)) {
            byte [] b = ByteConversion.toByte(tuple.asFloat(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(Double.class)) {
            byte [] b = ByteConversion.toByte(tuple.asDouble(slot));
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else if (type.equals(String.class)) {
            String st = tuple.asString(slot);
            int len = st.length();
            byte [] b = ByteConversion.toByte(len);
            System.arraycopy(b, 0, bytes, start, b.length);
            start += b.length;
            b = ByteConversion.toByte(st);
            System.arraycopy(b, 0, bytes, start, b.length);
            return start + b.length;
        }
        else {
            throw new DBxicException("Unsupported type when writing tuple: " + type.getClass().getName() + ".");
        }
    } // dumpSlot()


    /**
     * pad: introduce empty bytes to pad a byte array and write completely the array (usually, up to block size).
     */
    protected static void pad(byte [] bytes, int start) {
        for (int i = start; i < bytes.length; i++) bytes[i] = (byte) 0;
    } // pad()






    /****************************************** READ OPERATIONS ******************************************/


    /**
     * readBlock: reads a block from disk.
     */
    public static Block readBlock(Relation relation, String fileName, int number, RandomAccessFile raf)
            throws DBxicException {

        try {
            // seek to the appropriate place of the block in the file
            long seek = number * Block.BLOCK_SIZE;
            raf.seek(seek);
            byte [] bytes = new byte[Block.BLOCK_SIZE];
            int bytesRead = raf.read(bytes);
            if (bytesRead == -1) {
                // we've reached the end of file, so we need a new block
                raf.setLength(seek + Block.BLOCK_SIZE);
                return new Block(relation, fileName, number);
            }
            if (bytesRead != Block.BLOCK_SIZE) {
                throw new DBxicException("Error: block [" + fileName +": " + number + "] was not fully read.");
            }
            return fetchTuples(relation, fileName, number, bytes);
        }
        catch (IOException ioe) {
            throw new DBxicException("Error while reading block [" + fileName +": " + number + "] from disk.", ioe);
        }
    } // readBlock()


    /**
     * fetchTuples: reads tuples from disk and puts them in a new block.
     */
    protected static Block fetchTuples(Relation relation, String fileName, int number, byte [] bytes)
            throws DBxicException {

        // start reading the number of tuples
        int numberOfTuples = fetchNumber(bytes);
        Block block = new Block(relation, fileName, number);
        int offset = ByteConversion.INT_SIZE; // starts with one-int offset, for the previously read number of tuples
        for (int i = 0; i < numberOfTuples; i++) {
            Pair<Tuple,Integer> pair = fetchTuple(relation, fileName, bytes, offset);
            Tuple tuple = pair.first;
            offset = pair.second.intValue();
            block.addTuple(tuple);
        }
		
        return block;
    } // fetchTuples()

    /**
     * fetchNumber: reads an integer from a byte array
     */
    public static int fetchNumber(byte [] bytes) {
        byte [] b = new byte[ByteConversion.INT_SIZE];
        System.arraycopy(bytes, 0, b, 0, b.length);
        return ByteConversion.toInt(b);
    } // fetchNumber()

    /**
     * fetchTuple: reads a tuple from a byte array
     */
    public static Pair<Tuple, Integer> fetchTuple(Relation relation, String filename, byte[] bytes, int offset)
            throws DBxicException {

        // firstly, reads in the tuple id
        byte [] b = new byte[ByteConversion.INT_SIZE];
        System.arraycopy(bytes, offset, b, 0, b.length);
        int id = ByteConversion.toInt(b);
        offset += b.length;
        // then, reads in the different attributes of the tuple
        List<Comparable> values = new ArrayList<Comparable>();
        Iterator<Attribute> it = relation.iterator();
        while (it.hasNext()) {
            Pair<? extends Comparable, Integer> pair = fetchSlot(it.next().getType(), bytes, offset);
            offset = pair.second;
            values.add(pair.first);
        }
        Tuple t = new Tuple(filename, id, values);
        return new Pair<Tuple, Integer>(t, Integer.valueOf(offset));
    } // fetchTuple()

    /**
     * fetchSlot: low-level method to read a slot from a byte array.
     */
    protected static Pair<? extends Comparable, Integer> fetchSlot(Class<? extends Comparable> type,
                                                                   byte[] bytes, int start) throws DBxicException {
        try {
            if (type.equals(Character.class)) {
                byte [] b = new byte[ByteConversion.CHAR_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Character, Integer>(Character.valueOf(ByteConversion.toChar(b)),start + b.length);
            }
            else if (type.equals(Byte.class)) {
                return new Pair<Byte, Integer>(bytes[start],
                        start + 1);
            }
            else if (type.equals(Short.class)) {
                byte [] b = new byte[ByteConversion.SHORT_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Short, Integer>(Short.valueOf(ByteConversion.toShort(b)),start + b.length);
            }
            else if (type.equals(Integer.class)) {
                byte [] b = new byte[ByteConversion.INT_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Integer, Integer>(Integer.valueOf(ByteConversion.toInt(b)),start + b.length);
            }
            else if (type.equals(Long.class)) {
                byte [] b = new byte[ByteConversion.LONG_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Long, Integer>(Long.valueOf(ByteConversion.toLong(b)),start + b.length);
            }
            else if (type.equals(Float.class)) {
                byte [] b = new byte[ByteConversion.FLOAT_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Float, Integer>(Float.valueOf(ByteConversion.toFloat(b)),start + b.length);
            }
            else if (type.equals(Double.class)) {
                byte [] b = new byte[ByteConversion.DOUBLE_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                return new Pair<Double, Integer>(Double.valueOf(ByteConversion.toDouble(b)),start + b.length);
            }
            else if (type.equals(String.class)) {
                byte [] b = new byte[ByteConversion.INT_SIZE];
                System.arraycopy(bytes, start, b, 0, b.length);
                start += b.length;
                int stLength = ByteConversion.toInt(b);
                b = new byte[2*stLength];
                System.arraycopy(bytes, start, b, 0, b.length);
                String str = ByteConversion.toString(b);
                return new Pair<String, Integer>(str, start + b.length);
            }
            else {
                throw new DBxicException("Error: Unsupported type " + type.getClass().getName() + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException aiob) {
            throw new DBxicException("Error while reading table row (boundary error.)", aiob);
        }
    } // fetch()

} // blockIOManager
