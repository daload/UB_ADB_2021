package org.adbs.dbxic.engine.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Join: To represent an algebraic join operation
 */
public class Join extends AlgebraicOperator {
	
    private Proposition proposition; // join condition/proposition

    /**
     * Constructor: empty
     */
    public Join() {
        this(null);
    } // Join()

    /**
     * Constructor: creates a new algebraic join operator given the join condition/proposition
     */
    public Join(Proposition proposition) {
        this.proposition = proposition;
    } // Join()

    /**
     * getProposition: return this join's proposition
     */
    public Proposition getProposition() {
        return proposition;
    } // getQualification()

    /**
     * getTables: return the tables associated to this operation
     */
    @Override
    public Set<String> getTables() {
        Set<String> tables = new LinkedHashSet<String>();
        tables.add(proposition.getLeftVariable().getTable());
        tables.add(proposition.getRightVariable().getTable());
        return tables;
    }

    /**
     * getVariables: returns this join's variables
     */
    @Override
    public List<Variable> getVariables() {
        List<Variable> vars = new ArrayList<Variable>();
        vars.add(proposition.getLeftVariable());
        vars.add(proposition.getRightVariable());
        return vars;
    } // getVariables()

    /**
     * toString
     */
    @Override
    public String toString() {
        return "[join (" + proposition.toString() + ")]";
    } // toString()

} // Join
