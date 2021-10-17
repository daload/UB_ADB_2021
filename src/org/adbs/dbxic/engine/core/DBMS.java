package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.catalog.Catalog;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.engine.parser.XicQLParser;
import org.adbs.dbxic.storage.BufferManager;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.engine.physicalOperators.Pipe;
import org.adbs.dbxic.engine.physicalOperators.MessageThroughPipe;
import org.adbs.dbxic.utils.ArgsParser;
import org.adbs.dbxic.utils.DBxicException;

import java.io.*;
import java.util.Properties;

/**
 * Database: Manage the DB
 */
public class DBMS {

    public static String base_dir;
    public static String temp_dir;

    private String PROMPT = "xicql > ";

    protected Catalog catalog;
    protected StorageManager storManager;
    private XicQLParser XicQLparser = null;

    /**
     * Constructor: empty.
     */
    public DBMS(){
    }

    /**
     * init: initializes the DBMS given an options parser object
     */

    public void init(ArgsParser ap) throws DBxicException {
        // get the name of the properties file
        String propertiesFileName = ap.getOption("--properties", "dbxic.props");

        try {
            System.out.println("Starting up DBxic...");
            Properties db_properties = new Properties();
            FileInputStream fis = new FileInputStream(propertiesFileName);
            db_properties.load(fis);
            fis.close();
            base_dir = db_properties.getProperty("dbxic.base.dir",
                    System.getProperty("user.dir") + System.getProperty("file.separator") + "db").trim();
            temp_dir = db_properties.getProperty("dbxic.temp.dir",
                    base_dir + System.getProperty("file.separator") + "tmp").trim();


            String catalogFile = base_dir + System.getProperty("file.separator") + "catalog";
            catalog = new Catalog(catalogFile);
            if ((new File(catalogFile)).exists()) catalog.loadCatalog();
            else catalog.saveCatalog();

            int bufferSize = Integer.parseInt(db_properties.getProperty("dbxic.buffersize", "50").trim());

            storManager = new StorageManager(catalog, new BufferManager(bufferSize));

            System.out.println("Well done! DBxic is up!");
            System.out.println("Base directory: " + base_dir);
            System.out.println("Directory for tmp data: " + temp_dir);
            //System.out.println("Buffer pool size: " + bufferSize + " pages");
            System.out.print(PROMPT);
        }
        catch (Exception e) {
            throw new DBxicException("The current DB couldn't be initialized.", e);
        }
    } // DBMS()

    public void run() throws DBxicException, IOException {
        BufferedReader readFromKeyboard = new BufferedReader( new InputStreamReader(System.in) );
        boolean done = false;
        while (! done) {
            // Read a new statement
            String input = "";
            boolean mult_line = true;
            while (mult_line){
                input += " " + readFromKeyboard.readLine();
                input = input.trim();
                mult_line = !input.endsWith(";");
            }
            // multi-line statement as a single statement
            input = input.substring(0, input.length() - 1).trim();

            done = input.equals("exit");
            // if not exit order, run statement
            if (!done) {
                Pipe pipe = executeStatement(input);

                // show results
                for (Tuple tuple : pipe.tuples())
                    System.out.println(tuple.toStringFormatted());

                System.out.print(PROMPT);
            }
        }
    }

    /**
     * executeStatement: Executes a statement
     */
    public Pipe executeStatement(String statement) throws DBxicException {
        if (XicQLparser == null) {
            XicQLparser = new XicQLParser(new ByteArrayInputStream(statement.getBytes()));
            XicQLparser.setCatalog(catalog);
        }
        else {
            XicQLParser.ReInit(new ByteArrayInputStream(statement.getBytes()));
        }

        try {
            Statement st = XicQLParser.Start();
            return st.execute(this);
        }
        catch (Exception e) {
            MessageThroughPipe ms = new MessageThroughPipe(e.getMessage());
            return ms;
        }
    } // runQuery()


    /**
     * close: Shuts down the DBxic DBMS.
     */
    public void close() throws Exception {

        try {
            // save the catalog
            catalog.saveCatalog();
            storManager.close();
        }
        catch (DBxicException re) {
            throw new Exception("Error: Couldn't shut down the DBxic DBMS", re);
        }
    } // shutDown()


    /**
     * Run the DBMS
     */
    public static void main (String [] args) {
        try {
            // parse arguments
            ArgsParser ap = new ArgsParser(args);
            if (ap.hasOption("--help")) {
                System.err.println("Usage: java org.adbs.dbxic.core.DBMS --properties <properties-file> [--init]");
                System.exit(0);
            }

            // construct the db instance
            DBMS db_instance = new DBMS();
            db_instance.init(ap);
            db_instance.run();
            db_instance.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    } // main()

} // Database

