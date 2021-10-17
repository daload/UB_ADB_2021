package org.adbs.dbxic.operators;

import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.engine.core.DBMS;
import org.adbs.dbxic.engine.physicalOperators.RelationScan;
import org.adbs.dbxic.storage.BufferManager;
import org.adbs.dbxic.storage.StorageManager;

import java.util.ArrayList;
import java.util.List;

public class test_RelationScan {
    /**
     * Run first storage/test_RelationIntroduction
     */
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

            RelationScan rs = new RelationScan(sm, relation, filename);
            boolean done = false;
            while (! done) {
                Tuple tuple = rs.getNextOutTuple();
                done = tuple.isClosing();
                if (!done) System.out.println(tuple);
            }
            sm.close();
        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()
}
