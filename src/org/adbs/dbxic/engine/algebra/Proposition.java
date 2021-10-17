package org.adbs.dbxic.engine.algebra;

import org.adbs.dbxic.utils.Logics;
import org.adbs.dbxic.utils.Logics.CompareRelation;

/**
 * Proposition: propositions in relational algebra.
 */
public class Proposition<T> {

    private CompareRelation relationship;
    private Variable left;
    private T right;

    /**
     * Constructor: creates a new relationship between variables.
     */
    public Proposition (CompareRelation relationship, Variable left, T right) {
        this.relationship = relationship;
        this.left = left;
        this.right = right;
    } // Proposition()


    /**
     * getLeftVariable: returns the left-hand side variable.
     */
    public Variable getLeftVariable() {
        return left;
    } // getLeftVariable()


    /**
     * getRightVariable: returns the right-hand side as a variable.
     */
    public Variable getRightVariable() {
        return (Variable) right;
    } // getRightVariable()

    /**
     * rightIsVariable: returns true if the right-hand side is a variable.
     */
    public boolean rightIsVariable() {
        return right instanceof Variable;
    } // getRightVariable()

    /**
     * rightIsValue: returns true if the right-hand side is a value (string).
     */
    public boolean rightIsValue() {
        return right instanceof String;
    } // getRightValue()

    /**
     * getRightValue: returns the right-hand side as a value.
     */
    public String getRightValue() {
        return (String) right;
    } // getRightVariable()

    /**
     * getRelationship: returns this proposition's conditional relationship.
     */
    public CompareRelation getRelationship() {
        return relationship;
    } // getRelationship()


    /**
     * toString
     */
    @Override
    public String toString() {
        return left.toString() + " " + Logics.compareRelationToString(relationship) + " " + right.toString();
    } // toString()


} // Qualification
