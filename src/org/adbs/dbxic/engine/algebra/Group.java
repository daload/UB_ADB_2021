package org.adbs.dbxic.engine.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Projection: To represent a grouping operation
 */
public class Group extends AlgebraicOperator {
    private List<Variable> groupList;
	
    /**
     * Constructor: empty
     */
    public Group() {
        this(new ArrayList<Variable>());
    } // Group()

    /**
     * Constructor: creates a new grouping operator given the list of variables to group by
     */
    public Group(List<Variable> groupList) {
        this.groupList = groupList;
    } // Group()

    /**
     * groups: returns all group variables as an iterable
     */
    public Iterable<Variable> groups() {
        return groupList;
    }
	
    /**
     * getVariables: returns all grouping variables
     */
    @Override
    public List<Variable> getVariables() {
        return groupList;
    } // getVariables()

    /**
     * getTables: return the tables associated to this operation
     */
    @Override
    public Set<String> getTables() {
        Set<String> tables = new LinkedHashSet<String>();
        for (Variable v : groupList) {
            tables.add(v.getTable());
        }
        return tables;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "[group (" + groupList.toString() + ")]";
    } // toString()
} // Group
