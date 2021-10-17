package org.adbs.dbxic.engine;

import org.adbs.dbxic.catalog.*;
import org.adbs.dbxic.engine.algebra.*;
import org.adbs.dbxic.engine.core.DBMS;
import org.adbs.dbxic.engine.core.PlanBuilder;
import org.adbs.dbxic.engine.physicalOperators.PhysicalOperator;
import org.adbs.dbxic.storage.BufferManager;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.Logics;

import java.util.ArrayList;
import java.util.List;

public class test_PlanBuilder {

    public static void main(String[] args) {
        try {
            DBMS.base_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            DBMS.temp_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            //String prefix = args[0] + System.getProperty("path.separator");
            // build the catalog
            Catalog catalog = new Catalog(DBMS.base_dir+System.getProperty("file.separator")+"provisional_dbxic.cat");
            // build the storage manager
            StorageManager sm = new StorageManager(catalog, new BufferManager(20));
            // construct a database
            List<Attribute> attributes1 = new ArrayList<Attribute>();
            attributes1.add(new Attribute("key", "table1", Integer.class));
            attributes1.add(new Attribute("value", "table1", String.class));
            List<Attribute> attributes2 = new ArrayList<Attribute>();
            attributes2.add(new Attribute("key", "table2", Integer.class));
            attributes2.add(new Attribute("value", "table2", String.class));
            List<Attribute> attributes3 = new ArrayList<Attribute>();
            attributes3.add(new Attribute("key", "table3", Integer.class));
            attributes3.add(new Attribute("value", "table3", String.class));
            Table table1 = new Table("table1", attributes1);
            System.out.println(table1.getAttribute(0));
            Table table2 = new Table("table2", attributes2);
            Table table3 = new Table("table3", attributes3);
            catalog.addTable(table1);
            catalog.addTable(table2);
            catalog.addTable(table3);
            String filename1 = catalog.getTableFileName("table1");
            sm.createFile(filename1);
            System.out.println(filename1 + " successfully created");
            String filename2 = catalog.getTableFileName("table2");
            sm.createFile(filename2);
            System.out.println(filename2 + " successfully created");
            String filename3 = catalog.getTableFileName("table3");
            sm.createFile(filename3);
            System.out.println(filename3 + " successfully created");
            // populate it
            for (int i = 0; i < 100; i++) {
                List<Comparable> v1 = new ArrayList<Comparable>();
                v1.add(Integer.valueOf(i));
                v1.add(""+((int)v1.get(0)+100));
                Tuple tuple1 = new Tuple(filename1, i, v1);
                List<Comparable> v2 = new ArrayList<Comparable>();
                v2.add(Integer.valueOf(i));
                v2.add(""+((int)v2.get(0)+1000));
                Tuple tuple2 = new Tuple(filename2, i, v2);
                List<Comparable> v3 = new ArrayList<Comparable>();
                v3.add(Integer.valueOf(i));
                v3.add(""+((int)v3.get(0)+10000));
                Tuple tuple3 = new Tuple(filename3, i,v3);
                sm.insertTuple(table1, filename1, tuple1);
                //System.out.println("inserted " + tuple1);
                sm.insertTuple(table2, filename2, tuple2);
                //System.out.println("inserted " + tuple2);
                sm.insertTuple(table3, filename3, tuple3);
                //System.out.println("inserted " + tuple3);
            }
            // build an algebraic evaluation plan
            List<AlgebraicOperator> algebra = new ArrayList<AlgebraicOperator>();
            Join join1 = new Join(new Proposition(Logics.CompareRelation.EQUALS,
                    new Variable("table1", "key"),
                    new Variable("table2", "key")));
            Join join2 = new Join(new Proposition(Logics.CompareRelation.EQUALS,
                    new Variable("table2", "key"),
                    new Variable("table3", "key")));
            Selection sel = new Selection(new Proposition(Logics.CompareRelation.EQUALS,
                    new Variable("table3", "value"),
                    new String("0")));
            List<Variable> pl = new ArrayList<Variable>();
            pl.add(new Variable("table1", "key"));
            pl.add(new Variable("table3", "value"));
            Projection p = new Projection(pl);

            //algebra.add(sel);
            algebra.add(join1);
            algebra.add(join2);
            algebra.add(p);
            System.out.println(algebra);
            // convert it to a physical plan
            PlanBuilder pb = new PlanBuilder(catalog, sm);
            PhysicalOperator physicalOperator = pb.buildPlan(DBMS.temp_dir+System.getProperty("file.separator")+"plan", algebra);
            System.out.println(physicalOperator);
            // evaluate it
            System.out.println("evaluating...");
            System.out.println("results:");
            boolean done = false;
            while (!done) {
                Tuple tuple = physicalOperator.getNextOutTuple();
                done = tuple.isClosing();
                if (!done) System.out.println(tuple.getValues());
            }
            sm.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()
}
