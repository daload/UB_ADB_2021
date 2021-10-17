package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * UnaryOperator: Models a unary engine operator.
 */
public abstract class UnaryOperator extends PhysicalOperator {

    /**
     * Constructor: creates a new empty unary operator
     */
    public UnaryOperator() throws DBxicException {
        super();
    } // UnaryOperator()
    
    /**
     * Constructor: creates a new unary operator given the input operator.
     */
    public UnaryOperator(PhysicalOperator physicalOperator) throws DBxicException {
        super();
        List<PhysicalOperator> inops = new ArrayList<PhysicalOperator>();
        inops.add(physicalOperator);
        setInputs(inops);
    } // UnaryOperator()


    /**
     * getInputOperator: returns the single input operator of this unary operator.
     */
    public PhysicalOperator getInputOperator() throws DBxicException {
        return getInputOperator(0);
    } // getInputOperator()



    /**
     * getNextProcessedTupleList: protected method to handle the next tuple(s) from this operator.
     * We check, if it is not done, the input tuples, one by one, and process them to create
     * (a list of) tuples
     */
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        List<Tuple> outgoing = new ArrayList<Tuple>();

        if (!inOpsDone[0]) {
            List<Tuple> inTuples = inOps.get(0).getNextOutTupleList();
            for (Tuple tuple : inTuples) {
                List<Tuple> outTuples = new ArrayList<Tuple>();
                if (tuple.isClosing()) {
                    outTuples.add(new Tuple(Tuple.tupleType.CLOSING));
                }
                else{
                    outTuples.addAll( processTuple(tuple) );
                }
                outgoing.addAll( outTuples );
            }
        }
        return outgoing;
    } // getNext()


    /**
     * processTuple: abstract method the inner tuple processing method
     */
    protected abstract List<Tuple> processTuple(Tuple tuple) throws DBxicException;

} // UnaryOperator
