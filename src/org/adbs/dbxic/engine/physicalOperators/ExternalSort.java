package org.adbs.dbxic.engine.physicalOperators;

import org.adbs.dbxic.catalog.Relation;
import org.adbs.dbxic.catalog.Tuple;
import org.adbs.dbxic.storage.Block;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.DBxicException;
import org.adbs.dbxic.utils.FileHandling;
import org.adbs.dbxic.utils.Pair;

import java.io.IOException;
import java.util.*;

/**
 * ExternalSort: External merge-sort algorithm for a relation. Blocking operator!
 * TODO: implement in this class your code for External Merge-sort
 */
public class ExternalSort extends UnaryOperator {
    
    private StorageManager sm;

    private int [] slots; // sort key(s)

    private int num_runs;
    private String[] runTempFiles;

    private String inputFileName;
    private String outputFileName;
    private Iterator<Tuple> outputTuples;
    private List<Tuple> returnList;

    
    /**
     * Constructs a new external sort operator.
     */
    public ExternalSort(PhysicalOperator operator, StorageManager sm, int[] slots, int num_runs) throws DBxicException {
        
        super(operator);
        this.sm = sm;
        this.slots = slots;
        this.num_runs = num_runs;
        try {
            initTempFiles(); // create the temporary files for runs
        }
        catch (DBxicException sme) {
            throw new DBxicException("Error: Could not instantiate external sort", sme);
        }
    } // ExternalSort()
	

    /**
     * initTempFiles: Initialises the temporary files:
     * - one per each run
     * - the output file
     */
    protected void initTempFiles() throws DBxicException {
        runTempFiles = new String[num_runs];
        for (int i=0; i < num_runs; i++) {
            runTempFiles[i] = FileHandling.getTempFileName();
            sm.createFile(runTempFiles[i]);
        }
        inputFileName = FileHandling.getTempFileName();
        sm.createFile(inputFileName);
        outputFileName = FileHandling.getTempFileName();
        sm.createFile(outputFileName);
    } // initTempFiles()

    /**
     * createOutputRelation: Creates an output relation for this external merge-sort operator.
     */
    @Override
    protected Relation createOutputRelation() throws DBxicException {
        return new Relation(getInputOperator().getOutputRelation());
    } // setOutputRelation()


    /**
     * compareTuples: Tells the differences between two tuples according to 'slots'
     * result < 0 -> tuple 1 is smaller
     * result > 0 -> tuple 2 is smaller
     * result = 0 -> tuples are equal
     */
    protected int compareTuples(Tuple tuple1, Tuple tuple2) {
        int res=0, i=0;
        while (res==0 && i<slots.length) {
            res = tuple1.getValue(slots[i]).compareTo(tuple2.getValue(slots[i]));
            i++;
        }
        return res;
    }

    /**
     * findPosition: Finds the position in which the tuple should be inserted in a relation
     */
    protected int findPosition(Relation rel, String fln, Tuple tuple) throws DBxicException {
        Iterator<Tuple> tuples = null;
        try {
            tuples = sm.tuples(rel, fln).iterator();
        } catch (IOException e) {
            throw new DBxicException("Error: Could not access tuples from relation.", e);
        }
        int ituple=0;
        while (tuples.hasNext()) {
            Tuple act_tuple = tuples.next();
            if (compareTuples(tuple, act_tuple) < 0) {
                break;
            }
            ituple++;
        }

        return ituple;
    } // setOutputRelation()


    /**
     * setup: Sets up this external merge-sort operator. Note that this is a blocking operator.
     */
    @Override
    public void setup() throws DBxicException {
        returnList = new ArrayList<Tuple>();
        try {
            // store the input relation in a temporary file
            Relation inRelation = getInputOperator().getOutputRelation();

            boolean done = false;
            while (! done) {
                Tuple tuple = getInputOperator().getNextOutTuple();
                if (tuple != null) {
                    done = tuple.isClosing();
                    if (!done) sm.insertTuple(inRelation, inputFileName, tuple);
                }
            }
            int num_blocks = FileHandling.getNumberOfBlocks(inputFileName);
            int[] num_blocks_per_run = new int[num_runs];
            int eqs = num_blocks/num_runs;
            int rem_num_blocks = num_blocks - eqs*num_runs;
            for (int i=0; i<num_runs; i++) {
                num_blocks_per_run[i] = eqs + (i<rem_num_blocks?1:0);
            }


            /////////////////////////////////////////////////////////
            //
            // TODO: ExternalSort: YOUR CODE GOES HERE
            // Save the whole input relation into runs. Take advantage of the moment for creating ordered runs.
            //
            /////////////////////////////////////////////////////////

            // how many blocks per run?
            // create relations for runs
            // insert tuples in runs in specific positions (ordered)
            
            /////////////////////////////////////////////////////////
            //
            // TODO: ExternalSort: YOUR CODE GOES HERE
            // Save the output ordered in the output file so that we can use later and iterator
            //
            /////////////////////////////////////////////////////////

            // read tuples from set of runs and insert them in output relation

            outputTuples = sm.tuples(getOutputRelation(), outputFileName).iterator();
        }
        catch (DBxicException sme) {
            throw new DBxicException("Error: Could not store and sort intermediate files.", sme);
        } catch (IOException e) {
            throw new DBxicException("Error: Could not access to stored sorted relation.", e);
        }
    } // setup()

    
    /**
     * cleanup: Cleanup after the sort.
     */
    public void cleanup () throws DBxicException {
        try {
            for (int i=0; i < num_runs; i++) {
                sm.deleteFile(runTempFiles[i]);
            }

            sm.deleteFile(inputFileName);
            sm.deleteFile(outputFileName);
        }
        catch (DBxicException sme) {
            throw new DBxicException("Error: Could not clean up final output.", sme);
        }
    } // cleanup()

    /**
     * getNextProcessedTupleList: Return tuples as demanded from processed relation
     */
    @Override
    protected List<Tuple> getNextProcessedTupleList() throws DBxicException {
        try {
            returnList.clear();
            if (outputTuples.hasNext()) returnList.add(outputTuples.next());
            else returnList.add(new Tuple(Tuple.tupleType.CLOSING));
            return returnList;
        }
        catch (Exception sme) {
            throw new DBxicException("Error: Could not read tuples from intermediate file.", sme);
        }
    } // getNextProcessedTupleList()


    /**
     * Operator class abstract interface -- never called.
     */
    @Override
    protected List<Tuple> processTuple(Tuple tuple) throws DBxicException {
        return null;
    }

    @Override
    protected String optToString() {
        StringBuilder res = new StringBuilder("sort by <");
        for (int i=0; i<slots.length; i++) {
            if (i>0) res.append(", ");
            try {
                res.append(getInputOperator().outRelation.getAttribute(slots[i]));
            } catch (DBxicException e) {
                e.printStackTrace();
            }
        }
        res.append(">");
        return res.toString();
    }



    /**
     * Opens a tuple iterator over this set of relations (runs).
     */
    private Iterator<Tuple> tuplesFromRuns(Relation[] lRelations, String[] filenames) throws IOException, DBxicException {
        return new TupleRunsOrderedIteratorWrapper(lRelations, filenames).iterator();
    } // tuples()


    /**
     * TupleRunsOrderedIteratorWrapper: basic iterator over tuples of a set of relations (runs).
     */
    private class TupleRunsOrderedIteratorWrapper implements Iterable<Tuple> {
        private Iterator<Tuple> lTuples[];
        private LinkedList<Pair<Integer,Tuple>> queue;

        /**
         * Constructor: creates a new tuple iterator.
         */
        public TupleRunsOrderedIteratorWrapper(Relation[] lRelations, String[] filenames)
                throws IOException, DBxicException {
            queue = new LinkedList<>();
            lTuples = new Iterator[num_runs];
            for (int i=0; i<num_runs; i++) {
                lTuples[i] = sm.tuples(lRelations[i],filenames[i]).iterator();
                if (lTuples[i].hasNext()) {
                    insertInQueueOrdered(new Pair<Integer, Tuple>(i, lTuples[i].next()));
                }
            }
        } // TupleRunsOrderedIteratorWrapper()
        private void insertInQueueOrdered(Pair<Integer, Tuple> new_element) {
            int i;
            for (i=0; i < queue.size(); i++) {
                if (compareTuples(new_element.second, queue.get(i).second) < 0)
                    break;
            }
            queue.add(i, new_element);
        }
        /**
         * iterator: returns an iterator over tuples in this relation.
         */
        public Iterator<Tuple> iterator() {
            return new Iterator<Tuple>() {
                public boolean hasNext() { return !queue.isEmpty(); }
                public Tuple next() throws NoSuchElementException {
                    Pair<Integer, Tuple> element = queue.poll();

                    if (lTuples[element.first].hasNext()) {
                        Tuple tuple = lTuples[element.first].next();
                        insertInQueueOrdered(new Pair<Integer, Tuple>(element.first, tuple));
                    }
                    return element.second;
                } // next()
                public void remove() throws UnsupportedOperationException {
                    throw new UnsupportedOperationException("Error: tuple removal from iterator not supported.");
                } // remove()
            }; // new Iterator
        } // iterator()
    } // TupleRunsOrderedIteratorWrapper
} // ExternalSort
