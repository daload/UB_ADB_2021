package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;

/**
 * ShowCommandList: To run a show command list statement
 */
public class ShowCommandList extends Statement {

    /**
     * Constructor: empty
     */
    public ShowCommandList() {
    } // ShowCommandList()

    /**
     * execute: run the statement
     * Could this be automated for any child of Statement?
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        String res = "Use:\n" + this.help();
        res += ShowCatalog.help();
        res += TableCreation.help();
        res += TableDescription.help();
        res += TableDeletion.help();
        res += TupleInsertion.help();
        res += Query.help();

        MessageThroughPipe ms = new MessageThroughPipe(res);
        return ms;
    } // execute()

    /**
     * help: returns how to ask for the catalog
     */
    public static String help() {
        return " > commandlist; \nTo consult the available set of operations.\n";
    } // help()
} // ShowCatalog
