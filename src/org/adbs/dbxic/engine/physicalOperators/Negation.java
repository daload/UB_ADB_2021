package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

/**
 * Negation: class to represent the negation of a predicate.
 */
public class Negation extends Predicate {
    private Predicate predicate;
    
    /**
     * Constructor: creates a negation of a predicate.
     */
    public Negation(Predicate predicate) {
        this.predicate = predicate;
    } // Negation()

    /**
     * getPredicate: returns the predicate to be negated
     */
    public Predicate getPredicate() {
        return predicate;
    } // getPredicate()

    
    /**
     * setPredicate: sets the predicate to be negated
     */
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    } // setPredicate()

    
    /**
     * evaluate: negates the evaluation of the predicate
     */
    @Override
    public boolean evaluate() {
        return !predicate.evaluate();
    } // Negation()

    /**
     * insertTuples: sets the tuples that are going to be evaluated
     */
    @Override
    public void insertTuples(Tuple leftTuple, Tuple rightTuple) throws DBxicException {
        predicate.insertTuples(leftTuple, rightTuple);
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "NOT (" + predicate + ")";
    } // toString()

} // Negation
