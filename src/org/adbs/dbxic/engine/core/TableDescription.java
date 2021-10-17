package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.catalog.Table;
import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * TableDescription: To run a table description statement
 */
public class TableDescription extends Statement {
	
    private String tablename;

    /**
     * Constructor: creates an instance of a table description request
     */
    public TableDescription(String tablename) {
        this.tablename = tablename;
    } // TableDescription()

    
    /**
     * getTableName: returns the name of the table to describe
     */
    public String getTableName() {
        return tablename;
    } // getTableName()

    /**
     * execute: run the statement
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        Table table = engine.catalog.getTable(tablename);
        MessageThroughPipe ms = new MessageThroughPipe(table.toString());
        return ms;
    } // execute()

    /**
     * help: returns how to know the scheme of a table
     */
    public static String help() {
        return " > describe table <table_name>; \nTo consult the scheme of table 'table_name'.\n";
    } // help()
} // TableDescription()
