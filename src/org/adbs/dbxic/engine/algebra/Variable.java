package org.adbs.dbxic.engine.algebra;

import org.adbs.dbxic.utils.Pair;

/**
 * Variable: representation of a variable as a pair of table-name and attribute-name
 */
public class Variable extends Pair<String, String> {
	
    /**
     * Constructor: creates a new representation of a variable given the table-name and the attribute-name
     */
    public Variable(String table, String attribute) {
        super(table, attribute);
    } // Variable()

    /**
     * getTable: returns the table to which this variable is associated (first element of the pair)
     */
    public String getTable() {
        return first;
    } // getTable()

    /**
     * getAttribute: returns the attribute of the table that this variable represents (second element of the pair)
     */
    public String getAttribute() {
        return second;
    } // getAttribute()
} // Variable
