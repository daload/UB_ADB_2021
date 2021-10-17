package org.adbs.dbxic.engine;

import org.adbs.dbxic.utils.Pair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class InflateRelation {

    private String tablename;
    private ArrayList<Pair<String,String>> lAttrs;
    private Random rand;

    public InflateRelation(String tablename, ArrayList<Pair<String,String>> lAttrs) throws Exception {
        this.rand = new Random();
        this.tablename = tablename;
        this.lAttrs = lAttrs;
    } // inflateRelation()

    private String randString(int length) {
        int leftLimit = 97, rightLimit = 122; // letters 'a' and 'z'
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int) (rand.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    public void generate(int n_tuples, PrintStream outStream) {
        System.out.println("drop table " + tablename + ";");
        String tab_creation = "create table " + tablename + " ( id integer";
        for (Pair<String,String> p: lAttrs) {
            tab_creation += ", " + p.first + " ";
            if (p.second.startsWith("limit_")){
                tab_creation += "integer";
            } else {
                tab_creation += p.second;
            }
        }
        tab_creation += ");";

        outStream.println(tab_creation);

        // generate values
        for (int i=0; i < n_tuples ; i++) {
            String tuple_creation = "insert into " + tablename + " values ("+i; // id

            for (Pair<String,String> att: this.lAttrs) {
                if (att.second.equals("string")) {
                    tuple_creation += ", '"+this.randString(7)+"'";
                } else if (att.second.equals("integer")) {
                    tuple_creation += ", "+ rand.nextInt(10000);
                } else {
                    String[] parts = att.second.split("_");
                    int leftLimit = Integer.parseInt(parts[1]);
                    int rightLimit = Integer.parseInt(parts[2]);
                    int randLimitedInt = rand.nextInt(rightLimit - leftLimit) + leftLimit ;
                    tuple_creation += ", "+randLimitedInt;
                }
            }

            tuple_creation += ");";
            outStream.println(tuple_creation);

        }
        outStream.println("exit;");
    } // generate()



    public static void main (String [] args) {

        InflateRelation irgen = null;

        PrintStream outStream = System.out; //new PrintStream(outputStream);

        String tablename = "teacher";
        int num_tuples=100;
        ArrayList<Pair<String,String>> lAttrs = new ArrayList<>();
        lAttrs.add(new Pair<>("name","string"));
        lAttrs.add(new Pair<>("phd_year","integer"));
        lAttrs.add(new Pair<>("born_year","integer"));
        lAttrs.add(new Pair<>("department_id","limit_0_15")); // from 0 to 14 (right is exclusive)*/
        /*String tablename = "student";
        int num_tuples=1000;
        ArrayList<Pair<String,String>> lAttrs = new ArrayList<>();
        lAttrs.add(new Pair<>("name","string"));
        lAttrs.add(new Pair<>("birth_year","integer"));
        lAttrs.add(new Pair<>("advisor","limit_0_50")); // from 0 to 49 (right is exclusive)*/
        /*String tablename = "department";
        int num_tuples=15;
        ArrayList<Pair<String,String>> lAttrs = new ArrayList<>();
        lAttrs.add(new Pair<>("name","string"));
        lAttrs.add(new Pair<>("place","string"));*/
        try {
            irgen = new InflateRelation(tablename, lAttrs);
            irgen.generate(num_tuples, outStream);
        }
        catch (Exception e) {
            System.err.println("Error: Could not generate the sql data.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        outStream.close();
    } // main()
}
