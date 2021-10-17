package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.FileHandling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NestedLoopsJoin: Implements a nested loops join. This is a blocking operation: needs writes to files
 */
public class NestedLoopsJoin extends BasicJoinOperator {

    // Filenames of temporary relations
    private String leftInFileName;
    private String rightInFileName;
    private String outFileName;


    private Iterator<Tuple> outputTuples;
    private List<Tuple> returnList;
	
    /**
     * Constructor: creates a new nested loops join operator given the input operators (relations), the join
     * condition and the storage manager.
     */
    public NestedLoopsJoin(PhysicalOperator left, PhysicalOperator right, StorageManager sm, Predicate predicate)
            throws DBxicException {
        super(left, right, sm, predicate);
        try {
            leftInFileName = FileHandling.getTempFileName();
            sm.createFile(leftInFileName);
            rightInFileName = FileHandling.getTempFileName();
            sm.createFile(rightInFileName);
            returnList = new ArrayList<Tuple>();            
        }
        catch (DBxicException sme) {
            throw new DBxicException("Could not instantiate nested-loops join", sme);
        }
    } // NestedLoopsJoin()

    
    /**
     * setup: sets up a nested loops join operator.
     */
    @Override
    protected void setup() throws DBxicException {
        try {
            // first of all, we need to store both input relations in temporary files
            StorageManager sm = getStorageManager();
            // store the left input relation in a temporary file
            Relation leftRel = getInputOperator(LEFT).getOutputRelation();
            boolean done = false;
            while (! done) {
                Tuple tuple = getInputOperator(LEFT).getNextOutTuple();
                if (tuple != null) {
                    done = tuple.isClosing();
                    if (!done) sm.insertTuple(leftRel, leftInFileName, tuple);
                }
            }
            
            // store the right input relation in a temporary file
            Relation rightRel = getInputOperator(RIGHT).getOutputRelation();
            done = false;
            while (! done) {
                Tuple tuple = getInputOperator(RIGHT).getNextOutTuple();
                if (tuple != null) {
                    done = tuple.isClosing();
                    if (! done) sm.insertTuple(rightRel, rightInFileName, tuple);
                }
            }
            
            // after the inputs are stored, we can carry out the nested loops join
            Predicate pred = getPredicate();
            outFileName = FileHandling.getTempFileName();
            sm.createFile(outFileName);
            for (Tuple leftTuple : sm.tuples(leftRel, leftInFileName)) {
                for (Tuple rightTuple : sm.tuples(rightRel, rightInFileName)) {
                    pred.insertTuples(leftTuple, rightTuple);
                    if (pred.evaluate()) { // if the condition is true, we can save a new tuple from the combination

                        Tuple newTuple = combineTuples(leftTuple, rightTuple);
                        sm.insertTuple(getOutputRelation(), outFileName, newTuple);
                    }
                }
            }
            outputTuples = sm.tuples(getOutputRelation(), outFileName).iterator();
        }
        catch (IOException ioe) {
            throw new DBxicException("Could not create block/tuple iterators.", ioe);
        }
        catch (DBxicException sme) {
            throw new DBxicException("Could not store intermediate relations to files.", sme);
        }
    } // setup()

    
    /**
     * cleanup: cleanup after the join.
     */
    @Override
    protected void cleanup() throws DBxicException {
        try {
            getStorageManager().deleteFile(leftInFileName);
            getStorageManager().deleteFile(rightInFileName);
            getStorageManager().deleteFile(outFileName);
        }
        catch (DBxicException sme) {
            throw new DBxicException("Error: Couldn't clean up final output", sme);
        }
    } // cleanup()
    
    /**
     * getNextProcessedTupleList: inner method to propagate a tuple.
     */
    @Override
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        try {
            returnList.clear();
            if (outputTuples.hasNext()) returnList.add(outputTuples.next());
            else returnList.add(new Tuple(Tuple.tupleType.CLOSING));
            return returnList;
        }
        catch (Exception sme) {
            throw new DBxicException("Could not read tuples from intermediate file.", sme);
        }
    } // getNextProcessedTupleList()


    /**
     * Textual representation.
     */
    @Override
    protected String optToString() {
        return "nlj <" + getPredicate() + ">";
    } // optToString()

} // NestedLoopsJoin
