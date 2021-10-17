package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * BinaryOperator: Models a basic binary operator.
 */
public abstract class BinaryOperator extends PhysicalOperator {
    
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    /**
     * Constructor: creates a new binary operator given a left and a right input operators.
     */
    public BinaryOperator(PhysicalOperator leftInput, PhysicalOperator rightInput) throws DBxicException {
        super();
        List<PhysicalOperator> inops = new ArrayList<PhysicalOperator>();
        inops.add(leftInput); // left first, the order is important!
        inops.add(rightInput);
        setInputs(inops);
    } // BinaryOperator()
    
} // BinaryOperator
