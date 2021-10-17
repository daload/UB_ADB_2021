package org.adbs.dbxic.catalog;

import org.adbs.dbxic.engine.core.DBMS;
import org.adbs.dbxic.utils.DBxicException;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Catalog: class that saves all the information about the tables in the catalog.
 */
public class Catalog {

    private String catalogFilename;
    private Map<String, Table> tablesInCatalog;
    private Map<String, String> filenamesOfTabInCat;

    /**
     * Constructor: creates a new catalog
     */
    public Catalog(String filename) {

        catalogFilename = filename;
        tablesInCatalog = new Hashtable<String, Table>();
        filenamesOfTabInCat = new Hashtable<String, String>();
    }


    /**
     * addTable: includes a new table in the catalog
     */
    public void addTable(Table tab) throws DBxicException {
        String tab_name = tab.getName();
        if (!tableExists(tab_name)) {
            tablesInCatalog.put(tab_name, tab);
            String tableFilename = new String(DBMS.base_dir + System.getProperty("file.separator") +
                    "table_" + tab_name + "_" + tab_name.hashCode());
            filenamesOfTabInCat.put(tab_name, tableFilename);
        }
        else
            throw new DBxicException("Error! Table " + tab_name + " already exists.");
    } // addNewTable()


    /**
     * tableExists: checks whether a table is in the catalog.
     */
    protected boolean tableExists(String tableName) {
        return (tablesInCatalog.get(tableName) != null);
    } // tableExists()


    /**
     * loadCatalog: load the catalog from the file
     */
    //@SuppressWarnings("unchecked")
    public void loadCatalog() throws DBxicException {
        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(catalogFilename));
            tablesInCatalog = (Map<String, Table>) istream.readObject();
            filenamesOfTabInCat = (Map<String, String>) istream.readObject();
            istream.close();
        }
        catch (ClassCastException cce) {
            throw new DBxicException("Error! Could not cast catalog from file " + catalogFilename, cce);
        }
        catch (ClassNotFoundException cnfe) {
            throw new DBxicException("Error! When loading the catalog from file " + catalogFilename
                    + ", could not cast to Table type.", cnfe);
        }
        catch (FileNotFoundException fnfe) {
            throw new DBxicException("Error! catalog file " + catalogFilename + " not found.", fnfe);
        }
        catch (IOException ioe) {
            throw new DBxicException("Error! I/O Exception while opening the catalog file " + catalogFilename, ioe);
        }
    } // loadCatalog()


    /**
     * saveCatalog: writes the catalog into the file
     */
    public void saveCatalog() throws DBxicException {

        try {
            ObjectOutputStream ostream = new ObjectOutputStream(new FileOutputStream(catalogFilename));
            ostream.writeObject(tablesInCatalog);
            ostream.writeObject(filenamesOfTabInCat);
            ostream.flush();
            ostream.close();
        }
        catch (IOException ioe) {
            throw new DBxicException("I/O Exception when saving catalog into file " + catalogFilename, ioe);
        }
    } // saveCatalog()


    /**
     * getTableFileName: Returns the name of the file that saves a table.
     */
    public String getTableFileName(String tableName) throws DBxicException {
        String filename = filenamesOfTabInCat.get(tableName);
        if (filename == null)
            throw new DBxicException("Error: There is no table with name " + tableName + " in the catalog");
        else
            return filename;
    } // getTableFileName()


    /**
     * getTable: returns the table with the given name.
     */
    public Table getTable(String tableName) throws DBxicException {
        Table tab = tablesInCatalog.get(tableName);
        if (tab == null)
            throw new DBxicException("Error: There is no table with name " + tableName + " in the catalog");
        else
            return tab;
    } // getTable()


    /**
     * deleteTable: deletes a table from the catalog
     */
    public void deleteTable(String tableName) throws DBxicException {
        if (! tableExists(tableName))
            throw new DBxicException("Error: There is no table with name " + tableName + " in the catalog");
        else {
            tablesInCatalog.remove(tableName);
            filenamesOfTabInCat.remove(tableName);
        }
    } // deleteTable()

    /**
     * getNumberOfTables: returns number of tables in the catalog
     */
    public int getNumberOfTables() {
        return tablesInCatalog.size();
    }// getNumberOfTables()

    /**
     * toString
     */
    @Override
    public String toString() {
        String res = "Catalog (" + getNumberOfTables() + " tables): \n";

        Set<String> tablesNames = tablesInCatalog.keySet();
        for (String tab : tablesNames) {
            res += "\tTable " + tab + " (saved at: " + filenamesOfTabInCat.get(tab) + "): " +
                            tablesInCatalog.get(tab)+ "\n";
        }
        return res;
    } // toString()
} // Catalog
