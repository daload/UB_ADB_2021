package org.adbs.dbxic.engine.algebra;

import java.util.List;
import java.util.Set;

/**
 * AlgebraicOperator: abstract class for algebraic operators
 */
public abstract class AlgebraicOperator {

    public abstract Set<String> getTables();

    public abstract List<Variable> getVariables();
} // AlgebraicOperator
