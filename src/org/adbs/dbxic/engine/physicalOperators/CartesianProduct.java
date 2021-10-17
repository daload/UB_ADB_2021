package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;

/**
 * CartesianProduct: A cartesian product between two input operators.
 */
public class CartesianProduct extends NestedLoopsJoin {
	
    /**
     * Constructor: creates a new Cartesian product operator given the input operators (relations) and the
     * storage manager.
     */
    public CartesianProduct(PhysicalOperator left, PhysicalOperator right, StorageManager sm) throws DBxicException {
        super(left, right, sm, null);
        AtomicCondition t = new AtomicCondition();
        t.setAsTrue();
        setPredicate(t); // It is just a NestedLoopsJoin that accepts all the tuple combinations.
    } // CartesianProduct()
} // CartesianProduct
