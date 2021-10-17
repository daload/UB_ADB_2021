package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

/**
 * Predicate: Abstract class for all predicates in conditions
 */
public abstract class Predicate {

    public abstract boolean evaluate();

    public abstract void insertTuples(Tuple leftTuple, Tuple rightTuple) throws DBxicException;
} // Predicate
