package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Table;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * RelationScan: to scan an input relation from its primary file.
 */
public class RelationScan extends PhysicalOperator {

    private StorageManager sm;
    private Relation relation;
    private String filename;

    private Iterator<Tuple> tuples;
    private List<Tuple> returnList;
	
    /**
     * Constructor: creates a new relation scan operator
     */
    public RelationScan(StorageManager sm, Relation relation, String filename) throws DBxicException {
        
        super();
        this.sm = sm;
        this.relation = relation;
        this.filename = filename;
    } // RelationScan()

    /**
     * getFileName: returns the filename that is to be scanned.
     */
    public String getFileName() {
        return filename;
    } // getFileName()

    
    /**
     * setup: sets up the relation scan.
     */
    @Override
    protected void setup() throws DBxicException {
        try {
            tuples = sm.tuples(relation, filename).iterator();
            returnList = new ArrayList<Tuple>();
        }
        catch (Exception sme) {
            throw new DBxicException("Could not set up a relation scan.", sme);
        }
    } // setup()

    @Override
    protected void cleanup() throws DBxicException { }


    /**
     * getNextProcessedTupleList: inner method to return the next tuple(s).
     */
    @Override
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        try {
            returnList.clear();
            if (tuples.hasNext()) returnList.add(tuples.next());
            else returnList.add(new Tuple(Tuple.tupleType.CLOSING));
            return returnList;
        }
        catch (Exception sme) {
            throw new DBxicException("Error: Couldn't fetch tuples from a relation scan.", sme);
        }
    } // getNextProcessedTupleList()
    

    /** 
     * createOutputRelation: sets the output relation of this relation scan operation
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        return relation;
    } // createOutputRelation()

    /**
     * optToString:
     */
    protected String optToString() {
        return "scan <" + ((Table)relation).getName() + ">";
    } // toStringSingle()

} // RelationScan
