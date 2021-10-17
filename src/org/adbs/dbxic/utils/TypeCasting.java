package org.adbs.dbxic.utils;

import java.util.List;

public class TypeCasting {
    /**
     * Package-visible (due to it being embarrassing) method to cast
     * comparables to correct comparable type. Confused? Yeah, you
     * should be.
     */
    public static void castValuesToTypes(List<Comparable> values, List<Class<? extends Comparable>> types) throws DBxicException {

        for (int i = 0; i < values.size(); i++) {
            Comparable c = values.get(i);

            Class<? extends Comparable> type = types.get(i);
            if (type.equals(Byte.class))
                values.set(i, Byte.valueOf((String) c));
            else if (type.equals(Short.class))
                values.set(i, Short.valueOf((String) c));
            else if (type.equals(Integer.class))
                values.set(i, Integer.valueOf((String) c));
            else if (type.equals(Long.class))
                values.set(i, Long.valueOf((String) c));
            else if (type.equals(Float.class))
                values.set(i, Float.valueOf((String) c));
            else if (type.equals(Double.class))
                values.set(i, Double.valueOf((String) c));
            else if (type.equals(Character.class))
                values.set(i, Character.valueOf(((String) c).charAt(0)));
            else if (! type.equals(String.class))
                throw new DBxicException("Unsupported type: " + type + ".");
        }
    }



    public static void castValuesToTypesIfNec(List<Comparable> values, List<Class<? extends Comparable>> types)
            throws DBxicException {

        for (int i = 0; i < values.size(); i++) {
            Comparable c = values.get(i);

            Class<? extends Comparable> type = types.get(i);

            if (!type.equals(c.getClass())) {
                if (type.equals(Byte.class))
                    values.set(i, Byte.valueOf((String) c));
                else if (type.equals(Short.class))
                    values.set(i, Short.valueOf((String) c));
                else if (type.equals(Integer.class))
                    values.set(i, Integer.valueOf((String) c));
                else if (type.equals(Long.class))
                    values.set(i, Long.valueOf((String) c));
                else if (type.equals(Float.class))
                    values.set(i, Float.valueOf((String) c));
                else if (type.equals(Double.class))
                    values.set(i, Double.valueOf((String) c));
                else if (type.equals(Character.class))
                    values.set(i, Character.valueOf(((String) c).charAt(0)));
                else if (!type.equals(String.class))
                    throw new DBxicException("Unsupported type: " + type + ".");
            }
        }
    }

    /**
     * createComparable: Given a type and a value, it creates a comparable object for
     * use in the physical predicates.
     */
    public static Comparable createComparable(Class<?> type, String value) throws DBxicException {
        try {
            if (type.equals(Byte.class)) return Byte.valueOf(value);
            else if (type.equals(Short.class)) return Short.valueOf(value);
            else if (type.equals(Character.class)) {
                if (value.length() != 1) throw new DBxicException("Could not build comparable value.");
                return Character.valueOf(value.charAt(0));
            }
            else if (type.equals(Integer.class)) return Integer.valueOf(value);
            else if (type.equals(Long.class)) return Long.valueOf(value);
            else if (type.equals(Float.class)) return Float.valueOf(value);
            else if (type.equals(Double.class)) return Double.valueOf(value);
            else if (type.equals(String.class)) return new String(value);
            else throw new DBxicException("Could not build comparable value.");
        }
        catch (Exception e) {
            throw new DBxicException("Could not build comparable value.", e);
        }
    } // createComparable()
}
