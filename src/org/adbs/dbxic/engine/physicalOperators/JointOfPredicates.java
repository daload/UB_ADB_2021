package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.List;

/**
 * ListPredicate: A predicate over a list of predicates.
 * There are two possibilities: conjunction (AND) and disjunction (OR).
 */
public class JointOfPredicates extends Predicate {
	public enum JunctionType {OR, AND};
    private List<Predicate> list;
    private JunctionType type;
	
    /**
     * Constructor: creates a new list predicate with type.
     */
    public JointOfPredicates(List<Predicate> list, JunctionType type) {
        this.list = list;
        this.type = type;
    } // JointOfPredicates()

    
    /**
     * numberOfPredicates: returns the number of predicates
     */
    public int numberOfPredicates() {
        return list.size();
    } // numberOfPredicates()

    /**
     * getPredicate: returns a specific predicate.
     */
    public Predicate getPredicate(int i) {
        return list.get(i);
    } // getPredicate()

    
    /**
     * setPredicate: sets a specific predicate
     */
    public void setPredicate(int i, Predicate predicate) {
        list.set(i, predicate);
    } // setPredicate()


    /**
     * predicates: iterator over the list of predicates
     */
    public Iterable<Predicate> predicates() {
        return list;
    } // predicates()

    /**
     * evaluate: There are two possibilities: conjunction (AND) and disjunction (OR).
     * As only one of the predicates takes the "taking_value", the corresponding
     * junction takes that value (false in conjunctions; true in disjunctions).
     * Otherwise, the junction returns the negation of the "taking_value".
     */
    @Override
    public boolean evaluate() {
        boolean taking_value = false;
        if (type == JunctionType.OR)
            taking_value = true;
        for (Predicate predicate : predicates())
            if (predicate.evaluate()==taking_value) return taking_value;
        return !taking_value;
    } // evaluate()

    /**
     * insertTuples: sets the tuples that are going to be evaluated
     */
    @Override
    public void insertTuples(Tuple leftTuple, Tuple rightTuple) throws DBxicException {
        for (Predicate p : predicates())
            p.insertTuples(leftTuple, rightTuple);
    }

    /**
     * junctionOperator2String: returns the string that represents the junction operator
     */
    protected String junctionOperator2String() {
        switch (type) {
            case OR:
                return "OR";
            case AND:
                return "AND";
            default:
                return "??";
        }
    } // listSymbol()

    
    /**
     * toString
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("((" + getPredicate(0) +")");
        for (int i = 1; i < numberOfPredicates(); i++) {
            sb.append(junctionOperator2String() + "(" + getPredicate(i) +")");
        }
        sb.append(")");
        return sb.toString();
    } // toString()

} // ListPredicate
