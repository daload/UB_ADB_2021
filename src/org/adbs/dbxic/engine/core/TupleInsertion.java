package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

import java.util.List;

/**
 * TupleInsertion: To run a tuple insertion instruction.
 */
public class TupleInsertion extends Statement {

    private String tablename;
    private List<Comparable> values;

    
    /**
     * Constructor: creates a new tuple insertion command given the name of the table where insertion is expected
     * and the values
     */
    public TupleInsertion(String tablename, List<Comparable> values) {
        this.tablename = tablename;
        this.values = values;
    } // TupleInsertion()
    
    /**
     * getTableName: returns the name of the table where the tuple is to be inserted.
     */
    public String getTableName() {
        return tablename;
    } // getTableName()
    
    /**
     * getValues: returns the values of the new tuple
     */
    public List<Comparable> getValues() {
        return values;
    } // getValues()

    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        engine.storManager.castAndInsertTuple(tablename, values);
        return new MessageThroughPipe("Tuple was successfully inserted into table " + tablename);
    }

    /**
     * help: returns how to ask for the catalog
     */
    public static String help() {
        return " > insert into <table_name> values (value_1, ..., value_n); \n" +
                "To insert a new tuple in the table with name 'table_name' and\n" +
                "a value for each attribute of the table scheme.\n";
    } // help()
} // TupleInsertion
