package org.adbs.dbxic.engine.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Projection: To represent an algebraic projection
 */
public class Projection extends AlgebraicOperator {
	
    private List<Variable> projectionAttrs;
	
    /**
     * Constructor: empty
     */
    public Projection() {
        this(new ArrayList<Variable>());
    } // Projection()

    /**
     * Constructor: creates a new projection operation given a list of attributes
     */
    public Projection(List<Variable> projectionAttrs) {
        this.projectionAttrs = projectionAttrs;
    } // Projection()

    /**
     * projectionAttrs: returns this projection's attribute list as an iterable
     */
    public Iterable<Variable> projectionAttrs() {
        return projectionAttrs;
    } // projectionAttrs()

    /**
     * getVariables: returns this projection's attribute list
     */
    @Override
    public List<Variable> getVariables() {
        return projectionAttrs;
    } // getVariables()

    /**
     * getTables: return the tables associated to this operation
     */
    @Override
    public Set<String> getTables() {
        Set<String> tables = new LinkedHashSet<String>();
        for (Variable var : projectionAttrs()) {
            tables.add(var.getTable());
        }
        return tables;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "[projection (" + projectionAttrs.toString() + ")]";
    } // toString()

} // Projection
