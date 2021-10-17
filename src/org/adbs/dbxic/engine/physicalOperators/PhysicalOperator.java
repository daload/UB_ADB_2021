package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * PhysicalOperator: Abstract class with the basic operator toolset. Given a set of
 * input operators, it builds up an output relation.
 * It masks through a buffer the possible internal multiple-tuple processing to be
 * able to serve tuples one-by-one.
 */
public abstract class PhysicalOperator {

    protected List<PhysicalOperator> inOps;
    protected Relation outRelation;
	
    private boolean firstGetNext;
    protected int tupleCounter; // to count the tuples already processed
    protected boolean [] inOpsDone;

    private List<Tuple> multiNextBuffer;
    private int mnBufferIdx;
    private boolean isMnBufferLoaded;
		
    /**
     * Constructor. Emtpy
     */
    public PhysicalOperator() throws DBxicException {
        this(new ArrayList<PhysicalOperator>());
    } // Operator()

    
    /**
     * Constructor. Creates a new operator given the input operator(s).
     */
    public PhysicalOperator(List<PhysicalOperator> inOps) throws DBxicException {
        tupleCounter = 0;
        outRelation = null;
        firstGetNext = true;
        isMnBufferLoaded = false;
        mnBufferIdx = 0;
        setInputs(inOps);
    } // Operator()
    
    /**
     * setInputs: sets the inputs of the current operator
     */
    protected final void setInputs(List<PhysicalOperator> inOps) {
        this.inOps = inOps;
        inOpsDone = new boolean[inOps.size()];
        for (int i = 0; i < inOpsDone.length; i++) inOpsDone[i] = false;
    } // setInputs()


    /**
     * getNumberOfInputs: returns the number of inputs of this operator.
     */
    public int getNumberOfInputs() {
        return inOps.size();
    } // getNumberOfInputs()


    /**
     * getInputOperator: returns the specified input operator.
     */
    public PhysicalOperator getInputOperator(int i) throws DBxicException {
        try {
            return inOps.get(i);
        }
        catch (ArrayIndexOutOfBoundsException aibe) {
            throw new DBxicException("Error: requested input operator doesn't exist (i>size).", aibe);
        }
    } // getInputOperator()

    
    /**
     * getOutputRelation: returns the operator's output relation
     */
    public Relation getOutputRelation() throws DBxicException {
        if (outRelation == null) {
            outRelation = createOutputRelation();
        }
        return outRelation;
    } // getOutputRelation()

    /**
     * createOutputRelation: creates a relation to be used as the output relation of the operator
     */
    protected abstract Relation createOutputRelation() throws DBxicException;

    /**
     * getNextOutTuple: Wrapper for the multi-output getNext method. We use a buffer to
     * hold the multiple tuples that come from MultiGetNext, and serve them one-by-one.
     */
    public Tuple getNextOutTuple() throws DBxicException {
        // check if the buffer is empty. If so, load it
        if (!isMnBufferLoaded) {
            // reload the buffer
            multiNextBuffer = getNextOutTupleList();
            // if there's nothing there, we are done; otherwise, position the index at the beginning
            if (multiNextBuffer == null || multiNextBuffer.size() == 0) return null;
            else mnBufferIdx = 0;
        }

        // If we are here, we do have a  in a buffer, so get the current tuple and set up
        // the next call
        Tuple t = multiNextBuffer.get(mnBufferIdx++);
        isMnBufferLoaded = mnBufferIdx < multiNextBuffer.size();
        return t;
    } // getNextOutTuple()

    
    /**
     * getNextOutTupleList: Basic function that serves (sets of) tuples after being processed and produced
     * according to the current operator. Set ups and closes the tuple processing system, when needed.
     */	
    public List<Tuple> getNextOutTupleList() throws DBxicException {
        // we need to call setup the first time the operator is requested a tuple
        if (firstGetNext) {
            setup();
            firstGetNext = false;
        }

        List<Tuple> t = getNextProcessedTupleList();
        while (t.size() == 0) {        // keep asking until we obtain a non-empty result
            t = getNextProcessedTupleList();
        }
        if (t.size() == 1 && t.get(0).isClosing()) {
            // clean up if we reach the end
            cleanup();
        }
        return t;
    } // getNext()

    /**
     * setup: performs the initial setting up of the operator.
     */
    protected abstract void setup() throws DBxicException;

    /**
     * getNextProcessedTupleList: returns the set of tuples produced after a single step.
     */
    protected abstract List<Tuple> getNextProcessedTupleList() throws DBxicException;

    /**
     * cleanup: cleans up any resources held by the operator
     */
    protected abstract void cleanup() throws DBxicException;
    /**
     * finished: Are all the incoming operators done or not?
     */
    protected boolean finished() {
        for (boolean d : inOpsDone) if (! d) return false;
        return true;
    } // finished()





    /**
     * toString
     */
    @Override
    public String toString () {
        return optToString(0);
    } // toString()


    /**
     * optToString: recursive call (first, this; then, input operators)
     */
    public String optToString(int level) {
        StringBuffer sb = new StringBuffer();
        sb.append(indentation(level) + optToString() + "\n");
        for (PhysicalOperator physicalOperator : inOps)
            sb.append(physicalOperator.optToString(level+1) + "\n");
        sb.setLength(sb.length()-1);
        return sb.toString();
    } // toString()


    /**
     * indentation: creates an indentation (tabulator) of level jumps
     */
    protected String indentation(int level) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < level; i++) sb.append("\t");
        return sb.toString();
    } // prefix()


    /**
     * optToString:
     */
    protected abstract String optToString();




    /**
     * tuples: returns an iterable over the tuples of this operator.
     */
    public Iterable<Tuple> tuples() throws DBxicException {
        return new TupleIterable(this);
    }

    /**
     * TupleIterable: iterable class over tuples of an operator
     */
    class TupleIterable implements Iterable<Tuple> {

        private boolean more;
        private Tuple tuple;
        private PhysicalOperator physicalOperator;

        /**
         * Constructor: sets up the iterator given an operator
         */
        public TupleIterable(PhysicalOperator op) throws DBxicException {
            physicalOperator = op;
            tuple = physicalOperator.getNextOutTuple();
            more = (tuple != null ? (!tuple.isClosing()) : true);
        }

        /**
         * iterator: returns the iterator implementation over the operator.
         */
        public Iterator<Tuple> iterator() {
            return new Iterator<Tuple>(){
                public boolean hasNext() {
                    return more;
                } // hasNext()
                public Tuple next() throws NoSuchElementException {
                    try {
                        Tuple ret = tuple;
                        tuple = physicalOperator.getNextOutTuple();
                        more = (tuple != null ? (!tuple.isClosing()) : true);
                        return ret;
                    }
                    catch (DBxicException ee) {
                        throw new NoSuchElementException("Error: couldn't move to next tuple: " + ee.getMessage());
                    }
                } // next()
                public void remove() throws UnsupportedOperationException {
                    throw new UnsupportedOperationException("Error: removal through an operator's iterator is not supported.");
                } // remove()
            }; // return new Iterator<Tuple>()
        } // iterator()
    } // TupleIterable

} // Operator
