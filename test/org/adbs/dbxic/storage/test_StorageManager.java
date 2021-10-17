package org.adbs.dbxic.storage;

import org.adbs.dbxic.catalog.Attribute;
import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.engine.core.DBMS;
import org.adbs.dbxic.utils.FileHandling;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class test_StorageManager {
    public static void main (String[] args) {
        try {
            DBMS.base_dir  = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            DBMS.temp_dir = System.getProperty("user.dir")+System.getProperty("file.separator")+"db";
            String filename = DBMS.temp_dir+System.getProperty("file.separator")+ "sm_test.txt";

            List<Attribute> attrs = new ArrayList<Attribute>();
            attrs.add(new Attribute("character", null, Character.class));
            attrs.add(new Attribute("byte", null, Byte.class));
            attrs.add(new Attribute("short", null, Short.class));
            attrs.add(new Attribute("integer", null, Integer.class));
            attrs.add(new Attribute("long", null, Long.class));
            attrs.add(new Attribute("float", null, Float.class));
            attrs.add(new Attribute("double", null, Double.class));
            attrs.add(new Attribute("string", null, String.class));
            Relation rel = new Relation(attrs);

            List<Comparable> v = new ArrayList<Comparable>();
            v.add( Character.valueOf('a'));
            v.add( Byte.valueOf((byte) 26));
            v.add( Short.valueOf((short) 312));
            v.add( Integer.valueOf(2048));
            v.add( Long.valueOf(34567));
            v.add( Float.valueOf((float)12.3));
            v.add( Double.valueOf(25.6));
            v.add(new String("bla bla"));

            Block p1 = new Block(rel, filename, 0);
            for (int i = 0; i < 10; i++) {
                Tuple t = new Tuple(filename, i, v);
                p1.addTuple(t);
            }
            System.out.println(p1);
            Block p2 = new Block(rel, filename, 1);
            for (int i = 0; i < 20; i++) {
                Tuple t = new Tuple(filename, i, v);
                p2.addTuple(t);
            }
            System.out.println(p2);
            Block p3 = new Block(rel, filename, 2);
            for (int i = 0; i < 30; i++) {
                Tuple t = new Tuple(filename, i, v);
                p3.addTuple(t);
            }
            System.out.println(p3);

            System.out.println("Writing blocks");
            RandomAccessFile dfb = new RandomAccessFile(filename, FileHandling.READ_WRITE);

            StorageManager sm = new StorageManager(null, new BufferManager(2));
            sm.writeBlock(p1);
            sm.writeBlock(p2);
            sm.writeBlock(p3);

            System.out.println("Reading blocks");
            Block r1 = sm.readBlock(rel, p1.getFileName(), p1.getBlockNumber());
            Block r2 = sm.readBlock(rel, p2.getFileName(), p2.getBlockNumber());
            Block r3 = sm.readBlock(rel, p3.getFileName(), p3.getBlockNumber());

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);

            sm.close();
        }
        catch (Exception e) {
            System.err.println("Exception e: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()
}
