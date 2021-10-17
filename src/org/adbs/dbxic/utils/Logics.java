package org.adbs.dbxic.utils;

public class Logics {

    public enum CompareRelation {
        EQUALS, NOT_EQUALS, GREATER, LESS, GREATER_EQUALS, LESS_EQUALS
    }

    /**
     * compareRelationToString: returns the string that represents the condition operator
     */
    public static String compareRelationToString(CompareRelation operator) {
        switch (operator) {
            case EQUALS:
                return "=";
            case NOT_EQUALS:
                return "!=";
            case GREATER:
                return ">";
            case LESS:
                return "<";
            case GREATER_EQUALS:
                return ">=";
            case LESS_EQUALS:
                return "<=";
        }

        return "?";
    } // compareRelationToString()

    /**
     * compare: returns the result of comparing two comparable
     */
    public static boolean compare(Comparable leftValue, Comparable rightValue, CompareRelation operator) {

        int compValue = 0;
        try {
            compValue = leftValue.compareTo(rightValue);
        } catch (ClassCastException cce) {
            return false;
        }

        switch (operator) {
            case EQUALS:
                return (compValue == 0);
            case NOT_EQUALS:
                return (compValue != 0);
            case GREATER:
                return (compValue > 0);
            case LESS:
                return (compValue < 0);
            case GREATER_EQUALS:
                return (compValue >= 0);
            case LESS_EQUALS:
                return (compValue <= 0);
            default:
                return false;
        }
    } // compare()

    /**
     * reverse: reverses the comparison relationship
     */
    public static CompareRelation reverse(CompareRelation relationship) {

        switch (relationship) {
            case EQUALS:
                return CompareRelation.EQUALS;
            case NOT_EQUALS:
                return CompareRelation.NOT_EQUALS;
            case GREATER:
                return CompareRelation.LESS_EQUALS;
            case GREATER_EQUALS:
                return CompareRelation.LESS;
            case LESS:
                return CompareRelation.GREATER_EQUALS;
            case LESS_EQUALS:
                return CompareRelation.GREATER;
        }

        return CompareRelation.EQUALS;
    } // reverse()
}
