package org.adbs.dbxic.catalog;

import java.io.Serializable;

/**
 * Attribute: All the details of an attribute.
 */
public class Attribute implements Serializable {
    
    private String name;
    private String table;
    private Class<? extends Comparable> type;

    /**
     * Constructor: creates an attribute with a name and a type
     */
     public Attribute(String name, String table, Class<? extends Comparable> type) {
         this.name = name;
         this.table = table;
         this.type = type;
     } // Attribute()

    /**
     * Constructor: creates an attribute from another instance of the class
     */
    public Attribute(Attribute attr) {
        this.name = new String(attr.getName());
        this.table = new String(attr.getTable());
        this.type = attr.getType();
    } // Attribute()

    
    /**
     * getName: returns the name of the attribute
     */
    public String getName() {
        return name;
    } // getName()

    /**
     * getTable: returns the table of this attribute.
     */
    public String getTable() {
        return table;
    } // getTable()

    /**
     * getType: returns the type of the attribute
     */
    public Class<? extends Comparable> getType() {
        return type;
    } // getType()

    /**
     * equals: are these two objects the same?
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Attribute)) return false;

        Attribute aux = (Attribute) o;
        return getName().equals(aux.getName()) && getType().equals(aux.getType())
                && getTable().equals(aux.getTable());
    }

    /**
     * hashcode: to compare two instances
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash*37 + getName().hashCode();
        hash = hash*37 + (getTable()==null?0:getTable().hashCode());
        return hash*37 + getType().hashCode();
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return (getTable()==null?"":getTable()+".") + getName() + ": " + getType();
    } // toString()
    
} // Attribute
