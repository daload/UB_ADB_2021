package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * TableDeletion: All you need to run a table drop statement.
 */
public class TableDeletion extends Statement {
    private String tablename;
    
    /**
     * Constructor: creates an instance of a table drop action.
     */
    public TableDeletion(String tablename) {
        this.tablename = tablename;
    } // TableDeletion()

    
    /**
     * getTableName: returns the name of the table to be deleted.
     */
    public String getTableName() {
        return tablename;
    } // getTableName()

    /**
     * execute: run the statement
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        engine.storManager.deleteTable(tablename);
        MessageThroughPipe ms = new MessageThroughPipe("Table " + tablename + " successfully dropped");
        return ms;
    }// execute()

    /**
     * help: returns how to create a table
     */
    public static String help() {
        return " > drop table <table_name>; \n" +
                "To remove the table with name 'table_name'\n";
    } // help()
} // TableDeletion
