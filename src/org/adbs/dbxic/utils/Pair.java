package org.adbs.dbxic.utils;

import java.util.Objects;

/**
 * Pair: to wrap a pair of values of possible two different types.
 */

public class Pair<F, S> {

    public F first;
    public S second;


    /**
     * Constructor: empty
     */
    public Pair() {
        first = null;
        second = null;
    }

    /**
     * Constructor: creates a new pair given two elements
     */
    public Pair(F f, S s) {
        first = f;
        second = s;
    } // Pair()


    /**
     * equals
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o == this) return true;
        if (! (o instanceof Pair)) return false;
        try {
            Pair<F, S> p = (Pair<F, S>) o;
            return (Objects.equals(first, p.first)) && (Objects.equals(second, p.second));
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
        return hash*37 + code;
    } // hashCode()

    /**
     * toString
     */
    @Override
    public String toString() {
        return "(" + (first != null ? first : ("null"))
            + ", " + (second != null ? second : ("null")) + ")";
    } // toString()
}
