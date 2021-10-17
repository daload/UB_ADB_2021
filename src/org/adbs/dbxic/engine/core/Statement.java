package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * Statement: abstract class
 */
public abstract class Statement {

    /**
     * Constructor: empty
     */
    public Statement() {
    } // Statement()


    /**
     * execute: run the statement
     */
    public abstract Pipe execute(DBMS engine) throws DBxicException; // execute()

    public static String help() { return "Generic statement"; } // help()
} // Statement
