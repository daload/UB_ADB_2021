package org.adbs.dbxic.catalog;

import java.util.ArrayList;
import java.util.List;

/**
 * Tuple: to represent tuples in DBxic
 */
public class Tuple {
    public enum tupleType { REGULAR, INTERMEDIATE, CLOSING }
	
    private List<Comparable> values;

    private String filename;
    private int idNumber;
    private tupleType type;

    /**
     * Constructor: creates a new tuple with identification values (filename and number),
     * a type and a list of values
     */
    public Tuple(String filename, int idNumber, List<Comparable> values, tupleType type) {
        this.filename = filename;
        this.idNumber = idNumber;
        this.values = values;
        this.type = type;
    } // Tuple()

    /**
     * Constructor: creates an empty tuple
     */
    public Tuple() {
        this("", -1, new ArrayList<Comparable>(), tupleType.REGULAR);
    } // Tuple()

    /**
     * Constructor: creates an empty tuple with type
     */
    public Tuple(tupleType type) {
        this("", -1, new ArrayList<Comparable>(), type);
    } // Tuple()

    /**
     * Constructor: creates a new tuple with identification values (filename and number)
     * and a list of values
     */
    public Tuple(String filename, int idNumber, List<Comparable> values) {
        this(filename, idNumber, values, tupleType.REGULAR);
    } // Tuple()

    /**
     * setIntermediate: to indicate that a tuple is intermediate type
     */
    public void setIntermediate() {
        type = tupleType.INTERMEDIATE;
    } // setIntermediate()

    /**
     * isIntermediate?
     */
    public boolean isIntermediate() {
        return type == tupleType.INTERMEDIATE;
    } // isIntermediate()

    /**
     * setClosing: to indicate that a tuple is closing type
     */
    public void setClosing() {
        type = tupleType.CLOSING;
    } // setClosing()

    /**
     * isClosing?
     */
    public boolean isClosing() {
        return type == tupleType.CLOSING;
    } // isClosing()


    /**
     * getNumber: returns the number in the file
     */
    public int getIdNumber() {
        return idNumber;
    } // getNumber()


    /**
     * getFilename: returns the filename where the tuple is
     */
    public String getFilename() {
        return filename;
    } // getFilename()


    /**
     * setNumber: sets the number of this tuple in the file
     */
    public void setIdNumber(int idNumber) {
        this.idNumber = idNumber;
    } // setNumber()

    /**
     * setFilename: sets the tuple's filename
     */
    public void setFilename(String fileName) {
        this.filename = fileName;
    } // setFilename()

    
    /**
     * setValue: sets a value at position i in the tuple without type and bounds checking.
     */
    public void setValue(int i, Comparable value) {
        values.set(i, value);
    } // setValue()

    
    /**
     * setValues: Sets the values of this tuple.
     */
    public void setValues(List<Comparable> values) {        
        this.values.clear();
        this.values = values;
    } // setValues()


    /**
     * size: returns the size of the tuple.
     */
    public int size() {
        return values.size();
    } // size()


    /**
     * getValues: Returns the values of this tuple.
     */
    public List<Comparable> getValues() {
        return values;
    } // getValues()


    /**
     * getValue: returns value at position idx in the tuple  as a generic Comparable.
     */
    public Comparable getValue(int idx) {
        return values.get(idx);
    } // getValue()


    /**
     * asChar: returns value at position idx in the tuple cast to a primitive character.
     */
    public char asChar(int idx) throws ClassCastException {
        Character c  = (Character) values.get(idx);
        return c.charValue();
    } // asChar()

    
    /**
     * asByte: returns value at position idx in the tuple cast to a primitive byte.
     */
    public byte asByte(int idx) throws ClassCastException {
        Byte b  = (Byte) values.get(idx);
        return b.byteValue();
    } // asByte()

    
    /**
     * asShort: returns value at position idx in the tuple cast to a primitive short.
     */
    public short asShort(int idx) throws ClassCastException {
        Short s  = (Short) values.get(idx);
        return s.shortValue();
    } // asShort()
    
    
    /**
     * asInt: returns value at position idx in the tuple cast to a primitive integer.
     */
    public int asInt(int idx) throws ClassCastException {
        Integer in = (Integer) values.get(idx);
        return in.intValue();
    } // asInt()

    
    /**
     * asLong: returns value at position idx in the tuple cast to a primitive long.
     */
    public long asLong(int idx) throws ClassCastException {
        Long l = (Long) values.get(idx);
        return l.longValue();
    } // asLong()
	

    /**
     * asFloat: returns value at position idx in the tuple cast to a primitive  float.
     */
    public float asFloat(int idx) throws ClassCastException {
        Float f = (Float) values.get(idx);
        return f.floatValue();
    } // asFloat() 

    
    /**
     * asDouble: returns value at position idx in the tuple cast to a primitive double.
     */
    public double asDouble(int idx) throws ClassCastException {
        Double doub = (Double) values.get(idx);
        return doub.doubleValue();
    } // asDouble() 
	

    /**
     * asString: returns value at position idx in the tuple cast to a string
     */
    public String asString(int idx) throws ClassCastException {
        String str = (String) values.get(idx);
        return str;
    } // asString()

    



    /**
     * equals: checks whether two tuples are the same
     */
    @Override
    public boolean equals(Object o) {
        
        if (o == this) return true;
        if (! (o instanceof Tuple)) return false;
        Tuple t = (Tuple) o;
        if (size() != t.size()) return false;

        if (! getFilename().equals(t.getFilename()) && getIdNumber() == t.getIdNumber()) return false;
        if (isIntermediate() != t.isIntermediate()) return false;
        int i = 0;
        for (Comparable comp : values) {
            if (! comp.equals(t.getValue(i))) return false;
            i++;
        }
        return true;
    }

    /**
     * hashCode
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash*37 + getFilename().hashCode();
        hash = hash*37 + getIdNumber();
        hash = hash*37 + (isIntermediate()?1:0);

        for (Comparable comp : values)
            hash = hash*37 + comp.hashCode();
        return hash;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "[" + (getFilename() != null ? getFilename() : "") + " - " + getIdNumber() + "]" + " : " + values.toString();
    } // toString()

    public String toStringFormatted() {
        return "(" + getIdNumber() + ") : " + values.toString();
    }
} // Tuple
