package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.catalog.Table;
import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * TableCreation: All you need to run a table creation statement.
 */
public class TableCreation extends Statement {
	
    private Table table;

    /**
     * Constructor: creates an instance of a table creation action.
     */
    public TableCreation(Table table) {
        this.table = table;
    } // TableCreation()

    
    /**
     * getTable: returns the table to be created
     */
    public Table getTable() {
        return table;
    } // getTable()

    /**
     * execute: run the statement
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        engine.storManager.createTable(table);
        engine.catalog.saveCatalog();
        return new MessageThroughPipe("Table " + table.getName() + " successfully created");
    }// execute()


    /**
     * help: returns how to create a table
     */
    public static String help() {
        return " > create table <table_name> (attr_name type [, attr_name type]+);\n" +
                "To create a new table with name 'table_name' and as many attributes as\n" +
                "required, each of them with a given name 'attr_name' and type 'type'\n";
    } // help()
} // TableCreation
