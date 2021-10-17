package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.catalog.TupleSlotPointer;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.Logics;
import org.adbs.dbxic.utils.Logics.CompareRelation;

/**
 * Condition: Implements a condition between two values.
 */
public class AtomicCondition extends Predicate {

    private Comparable leftValue;
    private Comparable rightValue;
	
    private CompareRelation operator;
    
    /**
     * Constructor: creates a new condition without arguments
     */
    public AtomicCondition() {
        this(null, null, CompareRelation.EQUALS);
    } // Condition() 
	

    /**
     * Constructor: creates a new condition with two Comparable values and operator.
     */
    public AtomicCondition(Comparable left, Comparable right, CompareRelation condOperator) {
        this.leftValue = left;
        this.rightValue = right;
        this.operator = condOperator;
    } // Condition()

    /**
     * setAsTrue: so that it always evaluates to true
     */
    public void setAsTrue() {
        this.leftValue = Integer.valueOf(1);
        this.rightValue = Integer.valueOf(1);
        this.operator = CompareRelation.EQUALS;
    }

    /**
     * evaluate: evaluates the condition and returns the corresponding boolean value
     */
    @Override
    public boolean evaluate() {
        return Logics.compare(leftValue, rightValue, operator);
    } // evaluate()


    /**
     * insertTuples: sets the tuples that are going to be evaluated
     */
    @Override
    public void insertTuples(Tuple leftTuple, Tuple rightTuple) throws DBxicException {
        if (leftTuple != null) {
            if (leftValue instanceof TupleSlotPointer)
                ((TupleSlotPointer)leftValue).setTuple(leftTuple);
            else {
                throw new DBxicException("Error! Left position in the condition is not a TupleSlotPointer");
            }
        }
        if (rightTuple != null) {
            if (rightValue instanceof TupleSlotPointer)
                ((TupleSlotPointer)rightValue).setTuple(rightTuple);
            else {
                throw new DBxicException("Error! Right position in the condition is not a TupleSlotPointer");
            }
        }
    }


    /**
     * toString
     */
    @Override
    public String toString () {
        return "" + leftValue + Logics.compareRelationToString(operator) + rightValue;
    } // toString()

} // Condition
