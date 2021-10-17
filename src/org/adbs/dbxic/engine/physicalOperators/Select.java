package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * Select: to select tuples from an input operator
 */
public class Select extends UnaryOperator {
	
    private Predicate predicate; // selection based on this condition
    private List<Tuple> returnList;
	
    /**
     * Constructor: creates a new selection operator given its input operator and the condition.
     */
    public Select(PhysicalOperator physicalOperator, Predicate predicate) throws DBxicException {
        super(physicalOperator);
        this.predicate = predicate;
        returnList = new ArrayList<Tuple>();
    } // Select()

    
    /**
     * processTuple: Processes an incoming tuple.
     */
    @Override
    protected List<Tuple> processTuple(Tuple tuple) throws DBxicException {
        returnList.clear();
        predicate.insertTuples(tuple, null);
        if (predicate.evaluate())
            returnList.add(new Tuple(null, tupleCounter++, tuple.getValues(), Tuple.tupleType.INTERMEDIATE));
        return returnList;
    } // processTuple()

    
    /**
     * createOutputRelation: returns a new relation for this operator's output relation.
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        return new Relation(getInputOperator().getOutputRelation());
    } // createOutputRelation()

    @Override
    protected void setup() throws DBxicException { }

    @Override
    protected void cleanup() throws DBxicException { }


    /**
     * Textual representation.
     */
    @Override
    protected String optToString() {
        return "select <" + predicate + ">";
    } // toStringSingle()

} // Select
