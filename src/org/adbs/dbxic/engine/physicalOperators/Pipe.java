package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Pipe: An operator acting as a pipe for other operators. It simply saves its input
 * and propagates it on getNext() calls.
 */
public class Pipe extends UnaryOperator {
    
    private StorageManager sm;
    private String filename;    // Temporary file to store the result
    private Iterator<Tuple> tuples;
    private List<Tuple> returnList;
    
    /**
     * Constructor: empty
     */
    public Pipe() throws DBxicException {
        super();
    } // Pipe()
    
    /**
     * Constructor: creates a new pipe operator given an operator, the storage manager and a filename
     * for the temporary file.
     */
    public Pipe(PhysicalOperator inPhysicalOperator, StorageManager sm, String filename)  throws DBxicException {
        super(inPhysicalOperator);
        this.sm = sm;
        this.filename = filename;
    } // Pipe()

    
    /**
     * setup: sets up this pipe operator.
     */
    @Override
    protected void setup() throws DBxicException {
        try {
            sm.createFile(filename);
            Relation rel = getInputOperator().getOutputRelation();
            boolean done = false;
            while (! done) {
                Tuple tuple = getInputOperator().getNextOutTuple();
                if (tuple != null) {
                    done = tuple.isClosing();
                    if (! done) sm.insertTuple(rel, filename, tuple);
                }
            }
            tuples = sm.tuples(rel, filename).iterator();
            returnList = new ArrayList<Tuple>();
        }
        catch (IOException ioe) {
            throw new DBxicException("Could not create output iterator.", ioe);
        }
        catch (DBxicException sme) {
            throw new DBxicException("Could not store final output.", sme);
        } 
    } // setup()

    
    /**
     * cleanup: Cleans up after all processing. We just remove the temporary file.
     */
    @Override
    protected void cleanup() throws DBxicException {
        try {
            sm.deleteFile(filename);
        }
        catch (DBxicException sme) {
            throw new DBxicException("Could not clean up final output", sme);
        }
    } // cleanup()
    

    /**
     * getNextProcessedTupleList: the inner method to access tuples one by one
     */
    @Override
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        try {
            returnList.clear();
            if (tuples.hasNext()) returnList.add(tuples.next());
            else returnList.add(new Tuple(Tuple.tupleType.CLOSING));
            return returnList;
        }
        catch (Exception sme) {
            throw new DBxicException("Error: couldn't read tuples from intermediate file.", sme);
        }
    } // getNextProcessedTupleList()

    
    /**
     * processTuple: empty implementation of an abstract method of PhysicalOperator interface. Never reached.
     */
    @Override
    protected List<Tuple> processTuple(Tuple tuple) throws DBxicException {
        return null;
    } // processTuple()


    /**
     * createOutputRelation: the output relation of this operator is just
     * the output of this pipe's input operator.
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        return new Relation(getInputOperator().getOutputRelation());
    } // createOutputRelation()

    
    /**
     * Textual representation
     */
    @Override
    public String optToString() {
        return "pipe <" + filename + ">";
    } // optToString()
    
} // Pipe
