package org.adbs.dbxic.catalog;

import org.adbs.dbxic.utils.ByteConversion;
import org.adbs.dbxic.utils.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Relation: All the details of a relation
 */
public class Relation implements Serializable, Iterable<Attribute> {
    
    private List<Attribute> listOfAttributes;

    
    /**
     * Constructor: empty creation of a relation
     */
    public Relation(){
        listOfAttributes = new ArrayList<Attribute>();
    } // Relation()

    
    /**
     * Constructor: creates a relation with a list of attributes.
     */
    public Relation(List<Attribute> attributes) {
        listOfAttributes = new ArrayList<Attribute>(attributes);
    } // Relation()

    
    /**
     * Constructor: creates a relation from another Relation instance
     */
    public Relation(Relation relation) {
        listOfAttributes = new ArrayList<Attribute>();
        for (Attribute attr : relation) 
            addAttribute(attr);
    } // Relation()

    /**
     * addAttribute: Adds an attribute to this relation.
     */
    protected void addAttribute(Attribute attr) {
        listOfAttributes.add(attr);
    } // addAttribute()

    
    /**
     * getNumberOfAttributes
     */
    public int getNumberOfAttributes() {
        return listOfAttributes.size();
    } // getNumberOfAttributes()

    
    /**
     * iterator: returns an iterator over the attributes
     */
    public Iterator<Attribute> iterator(){
        return listOfAttributes.iterator();
    } // iterator()

    /**
     * getTypes: returns the list of types of the different attributes of the relation (same order)
     */
    public List<Class<? extends Comparable>> getTypes() {
        List<Class<? extends Comparable>> ltypes = new LinkedList<Class<? extends Comparable>>();
        for (int i=0; i < getNumberOfAttributes(); i++) {
            Class<? extends Comparable> type = getAttribute(i).getType();
            ltypes.add(type);
        }
        return ltypes;
    }

    /**
     * getAttribute: returns the i-th attribute
     */
    public Attribute getAttribute(int i) {
        return listOfAttributes.get(i);
    } // getAttribute()

    /**
     * getAttributeByName: returns the attribute with the given name
     */
    public Pair<Integer,Attribute> getAttributeByName(String name) {
        Iterator<Attribute> iterator = this.iterator();
        Attribute att = null;
        int i=0;
        boolean is_found = false;
        while (iterator.hasNext() && !is_found) {
            att = iterator.next();
            if (att.getName().equals(name))
                is_found=true;
            else
                i++;
        }
        return new Pair<Integer, Attribute>(i, att);
    } // getAttributeByName()


    /**
     * byteSize: calculate the byte size of a tuple of this relation
     */
    public int byteSize(Tuple t) {
        int size = ByteConversion.INT_SIZE; // one INT to save the identifier
        int slot = 0;
        for (Attribute it : this) {
            Class<?> type = it.getType();
            if (type.equals(Character.class)) size += ByteConversion.CHAR_SIZE;
            else if (type.equals(Byte.class)) size += 1;
            else if (type.equals(Short.class)) size += ByteConversion.SHORT_SIZE;
            else if (type.equals(Integer.class)) size += ByteConversion.INT_SIZE;
            else if (type.equals(Long.class)) size += ByteConversion.LONG_SIZE;
            else if (type.equals(Float.class)) size += ByteConversion.FLOAT_SIZE;
            else if (type.equals(Double.class)) size += ByteConversion.DOUBLE_SIZE;
            else if (type.equals(String.class)) size += ByteConversion.INT_SIZE + 2 * t.asString(slot).length();

            slot++;
        }

        return size;
    } // byteSize()



    /**
     * toString
     */
    @Override
    public String toString () {
        String res = "{";
        int n = getNumberOfAttributes();
        for (int i = 0; i < n; i++) {
            if (i > 0)
                res += ", ";
            res += getAttribute(i).toString();
        }
        res += "}";
        return res;
    } // toString()

} // Relation()
