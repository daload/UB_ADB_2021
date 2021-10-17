package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * MergeJoin: Implements a merge join for two ordered input relations.
 * TODO: implement in this class your code for Merge Join. We assume
 *  the inputs are sorted on the join attributes. Only join with an
 *  equality as join condition are allowed.
 * It is not blocking (the previous sorting is).
 */
public class MergeJoin extends BasicJoinOperator {
	

    private int leftSlot;
    private int rightSlot;

    private Tuple lastRightTuple;
    private ArrayList<Tuple> rightSetOfTuples;
    private Tuple leftTuple;

    private List<Tuple> returnList;
	
    /**
     * Constructs a new mergejoin operator.
     */
    public MergeJoin(PhysicalOperator leftOp, PhysicalOperator rightOp, StorageManager sm,
                     int leftSlot, int rightSlot, Predicate predicate) throws DBxicException {
        
        super(leftOp, rightOp, sm, predicate);
        this.leftSlot = leftSlot;
        this.rightSlot = rightSlot;
        returnList = new ArrayList<Tuple>();
        rightSetOfTuples = new ArrayList<Tuple>();
        lastRightTuple = null;
    } // MergeJoin()


    /**
     * setup: not necessary.
     */    
    @Override
    protected void setup()  { } // setup()
    
    
    /**
     * cleanup: not necessary
     */
    @Override
    protected void cleanup() { } // cleanup()


    /**
     * innerGetNext: Inner method to propagate a tuple.
     */
    @Override
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        try {
            returnList.clear();
            Predicate pred = getPredicate();

            /////////////////////////////////////////////////////////
            //
            // TODO: MergeJoin: YOUR CODE GOES HERE
            // Save (a few) processed tuples in returnList to return them (not-blocking operator)
            //
            /////////////////////////////////////////////////////////

            // get next output tuple from left operator
            // get next set of output tuples from right operator (if necessary)
            // if the value of the join attribute is equal in tuples from both input operators:
            //    combine tuples
            // else
            //    get more tuples from the corresponding input operator

            if (returnList.isEmpty()) returnList.add(new Tuple(Tuple.tupleType.CLOSING));
            return returnList;
        }
        catch (Exception sme) {
            throw new DBxicException("Could not read tuples from intermediate file.", sme);
        }
    } // innerGetNext()

    /**
     * getNextRightSetOfTuples: returns the next set of tuples from the right input operator.
     *
     */
    private ArrayList<Tuple> getNextRightSetOfTuples() throws DBxicException {
        ArrayList<Tuple> res = new ArrayList<>();
        Comparable act_value = null;

        if (lastRightTuple == null)
            lastRightTuple = getInputOperator(RIGHT).getNextOutTuple();
        Tuple act_tuple = lastRightTuple;
        if (act_tuple.isClosing()) return res; // completely processed!

        act_value = lastRightTuple.getValue(rightSlot);
        while (!act_tuple.isClosing()) {
            if (act_value.equals(act_tuple.getValue(rightSlot))) {
                res.add(act_tuple);
                act_tuple = getInputOperator(1).getNextOutTuple();
            } else {
                break;
            }
        }
        lastRightTuple = act_tuple;

        return res;
    }

    @Override
    protected String optToString() {
        return "mj <" + getPredicate() + ">";
    }

} // MergeJoin
