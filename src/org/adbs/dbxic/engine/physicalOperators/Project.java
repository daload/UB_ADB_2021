package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: to project attributes in a new relation (create a new select a subset of attributes from an input relation)
 */
public class Project extends UnaryOperator {
    private int [] slots; // attributes to be returned
    private List<Tuple> returnList;
    
    /**
     * Constructor: creates a new projection operator given the slots to keep
     */
    public Project(PhysicalOperator physicalOperator, int [] slots) throws DBxicException {
        super(physicalOperator);
        this.slots = slots;
        returnList = new ArrayList<Tuple>();
    } // Project()

    /**
     * processTuple: processes an incoming tuple
     */
    @Override
    protected List<Tuple> processTuple(Tuple tuple) throws DBxicException {
        returnList.clear();
        List<Comparable> reducedListOfValues = new ArrayList<Comparable>();
        for (int i = 0; i < tuple.size(); i++) {
            if (containsSlot(i)) reducedListOfValues.add(tuple.getValue(i));
        }
        returnList.add(new Tuple(null, tupleCounter++, reducedListOfValues, Tuple.tupleType.INTERMEDIATE));
        return returnList;
    } // processTuple()

    
    /**
     * containsSlot: Is the given slot contained in the projection array?
     */
    protected boolean containsSlot(int slot) {
        
        boolean found = false;		
        for (int i = 0; i < slots.length && !found; i++)
            found = (slots[i] == slot);
        return found;
    } // containsSlot()


    /**
     * setOutputRelation: sets the output relation of this projection.
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        try {
            Relation inputRelation = getInputOperator().getOutputRelation();
            List<Attribute> attrs = new ArrayList<Attribute>();
            for (int slot = 0; slot < inputRelation.getNumberOfAttributes(); slot++) {
                if (containsSlot(slot)) attrs.add(inputRelation.getAttribute(slot));
            }
            return new Relation(attrs);
        }
        catch (Exception e) {
            throw new DBxicException("Error: couldn't set the output relation.", e);
        }
    } // setOutputRelation()

    @Override
    protected void setup() throws DBxicException {}

    @Override
    protected void cleanup() throws DBxicException {}


    /**
     * toString
     */
    @Override
    protected String optToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("project <");
        for (int i = 0; i < slots.length-1; i++)
            sb.append(slots[i] + ", ");
        if (slots.length >= 1) {
			sb.append(slots[slots.length-1]);
        }
        sb.append(">");
        return sb.toString();
    } // toStringSingle()
    
} // Project
