package org.adbs.dbxic.engine.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sort: To represent a sorting operation
 */
public class Sort extends AlgebraicOperator {
    private List<Variable> sortList;
	
    /**
     * Constructor: empty
     */
    public Sort() {
        this(new ArrayList<Variable>());
    } // Sort()

    
    /**
     * Constructor: creates a new sorting operator given a list of attributes to sort by
     */
    public Sort(List<Variable> sortList) {
        this.sortList = sortList;
    } // Sort()


    /**
     * sorts: returns the sorting's list of attributes as an iterable
     */
    public Iterable<Variable> sorts() {
        return sortList;
    }
	
    /**
     * getSortList: returns the sorting's list of attributes.
     */
    public List<Variable> getSortList() {
        return sortList;
    } // getSortList()

    /**
     * getVariables: returns all sorting variables
     */
    @Override
    public List<Variable> getVariables() {
        return sortList;
    } // getVariables()

    /**
     * getTables: return the tables associated to this operation
     */
    @Override
    public Set<String> getTables() {
        Set<String> tables = new LinkedHashSet<String>();
        for (Variable v : sortList) {
            tables.add(v.getTable());
        }
        return tables;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "[sort (" + sortList.toString() + ")]";
    } // toString()
} // Sort
