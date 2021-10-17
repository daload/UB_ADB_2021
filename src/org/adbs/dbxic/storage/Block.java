package org.adbs.dbxic.storage;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.utils.ByteConversion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * block: class that represents a storage block.
 *
 * Be aware that this is a representation, disk blocks will likely vary!
 */
public class Block implements Iterable<Tuple> {
    public static int BLOCK_SIZE = 512;  // Usually 4096; 512 for testing purposes only

    private Relation relation;
    private List<Tuple> tuples;

    private String fileName;
    private int idNumber;
	
    private int freeSpace;
    
    /**
     * Constructor: creates a new block given its schema, the filename of the relation and the number-identifier.
     */
    public Block(Relation relation, String fileName, int blockNumber) {
        this.fileName = fileName;
        this.idNumber = blockNumber;
        this.relation = relation;
        this.tuples = new ArrayList<Tuple>();
        freeSpace = BLOCK_SIZE - ByteConversion.INT_SIZE;
    } // block()
	

    /**
     * getRelation: returns the relation to which this block is related
     */
    public Relation getRelation() {
        return relation;
    } // getRelation()

    /**
     * getNumberOfTuples: returns the number of tuples in this block.
     */
    public int getNumberOfTuples() {
        return tuples.size();
    } // getNumberOfTuples()

    /**
     * getFileName: returns the name of the file (relation) this block belongs to.
     */
    public String getFileName() {

        return fileName;
    } // getFileName()

    /**
     * getBlockNumber: returns the number-ID of this block in the relation file
     */
    public int getBlockNumber() {
        return idNumber;
    } // getBlockNumber()

    /**
     * hasRoom: returns whether this block has room for this tuple
     */
    public boolean hasRoomFor(Tuple t) {
        return freeSpace >= getRelation().byteSize(t);
    } // hasSpace()

    /**
     * hasRoomForSubstitution: returns whether this  given tuple can  specified index can be substituted for the
     * new tuple.
     */
    public boolean hasRoomForSubstitution(int index, Tuple nt) throws ArrayIndexOutOfBoundsException {
        return (freeSpace + getRelation().byteSize(tuples.get(index)) - getRelation().byteSize(nt)) >= 0;
    } // hasRoomForSubstitution()

    /**
     * numTuplesOverflow: returns the number of tuples which overflow if a tuple is inserted in a given position
     */
    public int numTuplesOverflow(int index, Tuple nt) throws ArrayIndexOutOfBoundsException {
        int count=0, tsize=tuples.size();
        int actFreeSpace = freeSpace - getRelation().byteSize(nt);
        while (actFreeSpace < 0) {
            actFreeSpace += getRelation().byteSize(tuples.get(tsize-(count++)));
        }
        return count;
    } // numTuplesOverflow()

    /**
     * addTuple: adds a new tuple to the block.
     */
    public void addTuple(Tuple tuple)  throws ArrayIndexOutOfBoundsException {
        if (hasRoomFor(tuple)) {
            tuples.add(tuple);
            freeSpace -= getRelation().byteSize(tuple);
        }
        else throw new ArrayIndexOutOfBoundsException("Error: No more space in block.");
    } // addTuple()

    /**
     * addTupleInPosition: adds a new tuple to the block in the position 'index'.
     */
    public void addTupleInPosition(Tuple tuple, int index)  throws ArrayIndexOutOfBoundsException {
        if (hasRoomFor(tuple)) {
            tuples.add(index, tuple);
            freeSpace -= getRelation().byteSize(tuple);
        }
        else throw new ArrayIndexOutOfBoundsException("Error: No more space in block.");
    } // addTupleInPosition()

    /**
     * addTuplesInPositionWithOverflow: adds new tuples to the block in the position 'index' and, if
     * the block overflows, the last tuples of the block are removed and retrieved.
     * TODO: We really should remove before insert
     */
    public ArrayList<Tuple> addTuplesInPositionWithOverflow(ArrayList<Tuple> lTuples, int index)
            throws ArrayIndexOutOfBoundsException {
        ArrayList<Tuple> lOverflow = new ArrayList<>();
        for (int i=lTuples.size()-1; i>=0; i--) {
            Tuple actTuple = lTuples.get(i);
            tuples.add(index, actTuple);
            freeSpace -= getRelation().byteSize(actTuple);
        }
        while (freeSpace<0) {
            Tuple actTuple = tuples.remove(tuples.size()-1);
            lOverflow.add(0, actTuple);
            freeSpace += getRelation().byteSize(actTuple);
        }
        return lOverflow;
    } // addTuplesInPositionWithOverflow()

    
    /**
     * setTuple: sets the given tuple in the position 'index'.
     */
    public void setTuple(int index, Tuple tuple) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (! hasRoomForSubstitution(index, tuple))
            throw new IllegalArgumentException("Error: New tuple does not fit in the slot of the one to substitute.");
        tuples.set(index, tuple);
    } // setTuple()


    /**
     * swap: swaps two tuples by their indexes.
     */
    public void swap(int x, int y) throws ArrayIndexOutOfBoundsException {
        Tuple t = tuples.get(x);
        tuples.set(x, tuples.get(y));
        tuples.set(y, t);
    } // swap()

    
    /**
     * retrieveTuple: returns the specified tuple from the block.
     */
    public Tuple getTuple(int index) throws ArrayIndexOutOfBoundsException {
        return tuples.get(index);
    } // retrieveTuple()



    /**
     * toString
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("block: [" + getFileName() + ":" + getBlockNumber() + "], tuples: {\n");
        int tid = 0;
        for (Tuple it : this)
            sb.append("\t" + tid++ + ": " + it.toString() + "\n");
        sb.append("}");
        return sb.toString();
    } // toString()




    
    /**
     * iterator: returns an iterator over this block.
     */
    public Iterator<Tuple> iterator() {
        return new blockIterator();
    }

    /**
     * blockIterator: builds an iterator over the tuples of this block.
     */
    private class blockIterator implements Iterator<Tuple> {
        private int currentIndex;

        public blockIterator() { currentIndex = 0; }
        public boolean hasNext() { return currentIndex < tuples.size(); }
        public Tuple next() { return tuples.get(currentIndex++); }
        public void remove() {
            int size = getRelation().byteSize(tuples.get(currentIndex));
            freeSpace += size;
            tuples.remove(currentIndex);
        }
    } // blockIterator()
} // block
