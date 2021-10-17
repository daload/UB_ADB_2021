package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.FileHandling;
import org.adbs.dbxic.engine.algebra.AlgebraicOperator;

import java.util.List;

/**
 * Query: To run a query based on algebraic operations
 */
public class Query extends Statement {
	
    private List<AlgebraicOperator> algebra;
    
    /**
     * Constructor: creates a new query given a list of algebraic operations
     */
    public Query(List<AlgebraicOperator> algebra) {
        this.algebra = algebra;
    } // Query()

    /**
     * getAlgebra: returns the algebra for this query.
     */
    public List<AlgebraicOperator> getAlgebra() {
        return algebra;
    } // getAlgebra()

    /**
     * execute: returns the plan built by the PlanBuilder for this algebraic operations list
     */
    @Override
    public Pipe execute(DBMS engine) throws DBxicException {
        PlanBuilder pb = new PlanBuilder(engine.catalog, engine.storManager);
        String file = FileHandling.getTempFileName();
        return (Pipe) pb.buildPlan(file, algebra);
    }

    /**
     * help: returns how to ask for the catalog
     */
    public static String help() {
        return " > select <table_name>.<attr_name> [, <table_name>.<attr_name>]+ from <table_name> [,<table_name>]+ where <condition>; \n" +
                "To scan and retrieve the tuples of table 'table_name' that\n" +
                "fulfill the given 'condition'.\n";
    } // help()
} // Statement
