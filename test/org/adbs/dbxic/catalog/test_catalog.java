package org.adbs.dbxic.catalog;

import org.adbs.dbxic.engine.core.DBMS;

import java.util.ArrayList;

public class test_catalog {
    public static void main (String args[]) {
        try {
            DBMS.base_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            DBMS.temp_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            String filename = DBMS.temp_dir+System.getProperty("file.separator")+"catalog_test.txt";

            // create three new table entries
            Table tc1 = new Table("students", new ArrayList<Attribute>());
            Table tc2 = new Table("subjects", new ArrayList<Attribute>());
            Table tc3 = new Table("trials", new ArrayList<Attribute>());

            // create the catalog
            Catalog catalog = new Catalog(filename);

            // add info to the catalog
            catalog.addTable(tc1);
            catalog.addTable(tc2);
            catalog.addTable(tc3);

            // print out the catalog
            System.out.println(catalog);

            // write it out
            System.out.println("Flushing catalog.");
            catalog.saveCatalog();

            // read it back in
            System.out.println("Reading catalog.");
            catalog.loadCatalog();

            // print it out again
            System.out.println(catalog);
        }
        catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()
}
