package org.adbs.dbxic.storage;

import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.engine.core.DBMS;

import java.util.ArrayList;
import java.util.List;

public class test_RelationIntroduction {

    public static void main (String [] args) {
        try {
            DBMS.base_dir  = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            DBMS.temp_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            String filename = DBMS.temp_dir+System.getProperty("file.separator")+"relation_test.txt";

            StorageManager sm = new StorageManager(null, new BufferManager(100));

            List<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(new Attribute("integer", null, Integer.class));
            attributes.add(new Attribute("string", null, String.class));
            Relation relation = new Relation(attributes);

            sm.createFile(filename);

            for (int i = 0; i < 30; i++) {
                List<Comparable> v = new ArrayList<Comparable>();
                v.add(Integer.valueOf(i));
                v.add(new String("bla"));
                Tuple tuple = new Tuple(filename, i, v);
                System.out.println("inserting: " + tuple);
                sm.insertTuple(relation, filename, tuple);
            }

            System.out.println("Tuples successfully inserted.");
            System.out.println("Opening tuple cursor...");

            for (Tuple tuple : sm.tuples(relation, filename))
                System.out.println("read: " + tuple);

            sm.close();
        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()
}
