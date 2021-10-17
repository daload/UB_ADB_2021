package org.adbs.dbxic.utils;

import java.util.Objects;

public class Triplet<F, S, T> {
    public F first;
    public S second;
    public T third;

    /**
     * Constructor: empty
     */
    public Triplet() { this(null, null, null); }

    /**
     * Constructor: creates a triplet given the three variables with their types
     */
    public Triplet(F f, S s, T t) {
        first = f;
        second = s;
        third = t;
    }





    /**
     * equals
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (! (o instanceof Triplet)) return false;
        try {
            Triplet<F, S, T> p = (Triplet<F, S, T>) o;
            return (Objects.equals(first, p.first)) && (Objects.equals(second, p.second))
                    && (Objects.equals(third, p.third));
        }
        catch (ClassCastException cce) {
            return false;
        }
    } // equals()

    /**
     * hashCode
     */
    @Override
    public int hashCode() {
        int hash = 17;
        int code = (first != null ? first.hashCode() : 0);
        hash = hash*37 + code;
        code = (second != null ? second.hashCode() : 0);
        hash = hash*37 + code;
        code = (third != null ? third.hashCode() : 0);
        return hash*37 + code;
    } // hashCode()

    /**
     * toString
     */
    public String toString() {
        return "(" + (first != null ? first : ("null"))
                + ", " + (second != null ? second : ("null")) + ")"
                + ", " + (third != null ? third : ("null")) + ")";
    }
}
