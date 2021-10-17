package org.adbs.dbxic.catalog;

import java.io.Serializable;
import java.util.List;

/**
 * Table: Extends Relation to create a Table by assigning a tablename
 */
public class Table extends Relation implements Serializable {
	
    private String tableName;
    
    /**
     * Constructor: creates a new table with a name (without attributes).
     */
    public Table(String name) {
        super();
        this.tableName = name;
    } // Table()

    
    /**
     * Constructor: creates a table with a name and a list of attributes.
     */
    public Table(String name, List<Attribute> attributes) {
        super(attributes);
        this.tableName = name;
    } // Table()

    /**
     * getName: returns the name of the table
     */
    public String getName() {
        return tableName;
    } // getName()

} // Table
