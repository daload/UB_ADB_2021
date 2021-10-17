package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * BasicJoinOperator: base abstract class for all joins
 */
public abstract class BasicJoinOperator extends BinaryOperator {
    private StorageManager sm;
    private Predicate predicate; // condition for join


    /**
     * Constructor: creates a new basic join operator given the input operators (relations), the condition, 
     * and the storage manager
     */
    public BasicJoinOperator(PhysicalOperator left, PhysicalOperator right, StorageManager sm, Predicate predicate)
            throws DBxicException {
        super(left, right);
        this.sm = sm;
        this.predicate = predicate;
    } // BasicJoinOperator()


    /**
     * getStorageManager: returns the used storage manager
     */
    protected StorageManager getStorageManager() {
        return sm;
    } // getStorageManager()


    /**
     * getPredicate: returns the condition for the join
     */
    protected Predicate getPredicate () {
        return predicate;
    } // getPredicate()

    /**
     * setPredicate: sets the condition for the join
     */
    protected void setPredicate(Predicate pred) {
        this.predicate = pred;
    } // setPredicate()

    
    /**
     * createOutputRelation: sets the output relation for this operator, that uses all the attributes of
     * both input relations
     */
    protected Relation createOutputRelation() throws DBxicException {
        List<Attribute> attributes = new ArrayList<Attribute>();
        Relation rel = getInputOperator(LEFT).getOutputRelation();
        for (int i = 0; i < rel.getNumberOfAttributes(); i++)
            attributes.add(rel.getAttribute(i));
    
        rel = getInputOperator(RIGHT).getOutputRelation();
        for (int i = 0; i < rel.getNumberOfAttributes(); i++)
            attributes.add(rel.getAttribute(i));
		
        return new Relation(attributes);
    } // createOutputRelation()

    /**
     * combineTuples: given two tuples, combine them into a single one.
     */    
    protected Tuple combineTuples(Tuple left, Tuple right) {
        List<Comparable> values = new ArrayList<Comparable>();
        values.addAll(left.getValues());
        values.addAll(right.getValues());
        return new Tuple(null, tupleCounter++, values, Tuple.tupleType.INTERMEDIATE);
    } // combineTuples()

} // BasicJoinOperator
