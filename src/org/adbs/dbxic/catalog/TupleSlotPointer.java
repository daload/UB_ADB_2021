package org.adbs.dbxic.catalog;

/**
 * TupleSlotPointer: A pointer to a slot of a tuple.
 */
public class TupleSlotPointer implements Comparable {

    private Tuple tuple;

    private int slot;
    private Class<? extends Comparable> type;

    
    /**
     * Constructor: creates a new tuple slot pointer
     */
    public TupleSlotPointer(Tuple tuple, int slot, Class<? extends Comparable> type) {
        this.tuple = tuple;
        this.slot = slot;
        this.type = type;
    } // TupleSlotPointer()


    /**
     * getTuple: returns the tuple which is pointed
     */
    public Tuple getTuple() {
        return tuple;
    } // getTuple()

    /**
     * getValue: returns the value of the tuple in the slot
     */
    public Comparable getValue() {
        return tuple.getValue(slot);
    } // getValue()

    /**
     * getSlot: returns the slot which is pointed
     */
    public int getSlot() {
        return slot;
    } // getSlot()

    /**
     * getType: returns the type of the pointed slot.
     */
    public Class<? extends Comparable> getType() {
        return type;
    } // getType()

    /**
     * setTuple: sets the tuple which is pointed.
     */
    public void setTuple(Tuple tuple) {
        this.tuple = tuple;
    } // setTuple()





    /**
     * toString
     */
    @Override
    public String toString() {
        return "@[" + getSlot() + "," + getType() + "]";
    } // toString()

    @Override
    public int compareTo(Object o) {
        if (o instanceof TupleSlotPointer)
            return getValue().compareTo(((TupleSlotPointer)o).getValue());
        else
            return tuple.getValue(this.slot).compareTo(o);
    }


} // TupleSlotPointer
