package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * ShowCatalog: To run a show catalog statement
 */
public class ShowCatalog extends Statement {

    /**
     * Constructor: empty
     */
    public ShowCatalog () {
    } // ShowCatalog()

    /**
     * execute: run the statement
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        MessageThroughPipe ms = new MessageThroughPipe(engine.catalog.toString());
        return ms;
    } // execute()

    /**
     * help: returns how to ask for the catalog
     */
    public static String help() {
        return " > catalog; \nTo consult the catalog of the database.\n";
    } // help()
} // ShowCatalog
