package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageThroughPipe: To provide the functionality of sending DB messages to the user as an operator.
 */
public class MessageThroughPipe extends Pipe {
    private String message;

    /**
     * Constructor: creates a new message pipe.
     */
    public MessageThroughPipe(String message) throws DBxicException {
        super();
        this.message = message;
    } // MessageThroughPipe()

    
    /**
     * getMultiNext: creates the return list, a tuple that only saves the message and a second ClosingTuple one
     */
    @Override
    public List<Tuple> getNextOutTupleList() throws DBxicException {
        List<Comparable> values = new ArrayList<Comparable>();
        values.add(message);
        Tuple tuple = new Tuple("DBResult", 0, values);
        List<Tuple> returnList = new ArrayList<Tuple>();
        returnList.add(tuple);
        returnList.add(new Tuple(Tuple.tupleType.CLOSING));
        return returnList;
    } // getNext()

    
    /**
     * setOutputRelation: empty
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        return null;
    } // setOutputRelation()
    
} // MessageThroughPipe
