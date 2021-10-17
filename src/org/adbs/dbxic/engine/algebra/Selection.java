package org.adbs.dbxic.engine.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Selection: To represent an algebraic selection
 */
public class Selection extends AlgebraicOperator {
	
    private Proposition proposition; // selection criteria
    
    /**
     * Default constructor.
     */
    public Selection() {
        this(null);
    } // Selection()

    
    /**
     * Constructor: creates a new algebraic selection operator given the proposition
     */
    public Selection(Proposition proposition) {
        this.proposition = proposition;
    } // Selection()

    
    /**
     * getProposition: returns this selection's proposition.
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
        return tables;
    }

    /**
     * getVariables: returns this selection's variables
     */
    @Override
    public List<Variable> getVariables() {
        List<Variable> vars = new ArrayList<Variable>();
        vars.add(proposition.getLeftVariable());
        return vars;
    } // getVariables()
    
    /**
     * toString
     */
    @Override
    public String toString() {
        return "[select (" + proposition.toString() + ")]";
    } // toString()

} // Selection
