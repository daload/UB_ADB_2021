package org.adbs.dbxic.engine.core;

import org.adbs.dbxic.catalog.*;
import org.adbs.dbxic.engine.algebra.*;
import org.adbs.dbxic.engine.physicalOperators.*;
import org.adbs.dbxic.storage.StorageManager;
import org.adbs.dbxic.utils.*;
import org.adbs.dbxic.utils.Logics.CompareRelation;

import java.util.*;

/**
 * PlanBuilder: Given a collection of algebraic operators, constructs
 * an evaluation plan that can be carried out by the engine.
 */
public class PlanBuilder {
	
    private Catalog catalog;
    private StorageManager sm;

    /**
     * Constructor: creates a new instance of the plan builder, given the catalog and the storage manager.
     */
    public PlanBuilder(Catalog catalog, StorageManager sm){
        this.catalog = catalog;
        this.sm = sm;
    } // PlanBuilder()

    
    /**
     * buildPlan: it builds the evaluation plan given a collection of algebraic operators and a filename.
     * It returns the root of the plan.
     *
     * It works as follows:
     * 1) Decompose the operators into scans, projections, selections and joins.
     * 2) Drop all unnecessary fields to reduce tuple width, then impose selections, enumerate joins,
     *    and impose any projection lists and sort orders.
     */
    public PhysicalOperator buildPlan(String filename, List<AlgebraicOperator> operators) throws DBxicException {
        // 1) Decompose the operators into scans, projections, selections and joins
        // get the relevant table names
        Set<String> tables = getTables(operators);
        // get the projections
        List<Projection> projections = getProjections(operators);
        // get the selections
        List<Selection> selections = getSelections(operators);
        // get the joins
        List<Join> joins = getJoins(operators);
	    // get the sorts
        List<Sort> sorts = getSorts(operators);
        // and the groupings
        List<Group> groups = getGroups(operators);


        // 2) Now, we can start building the plan
        // 2.1) Build the projections that can be carried out from the beginning
        List<Projection> init_projections = buildInitialProjections(operators, tables);
        // 2.2) Build the relation scans
        List<PhysicalOperator> curLOps = buildScans(tables);
        // 2.3) Place the initial projections right after the relation scans
        curLOps = pipeInitialProjectionsToScan(init_projections, curLOps);
        // 2.4) Append the relevant selections over each scan
        curLOps = pipeSelections(selections, curLOps);
        // 2.5) order the joins and cartesian products in a tree
        curLOps = cascadeJoins(joins, curLOps);
        // perform sanity check and impose the final projections
        if (curLOps.size() != 1) throw new DBxicException("Error: Multiple branches after join enumeration.");

        // 2.6) establish the root of the plan tree
        PhysicalOperator root_operator = curLOps.get(0);
        // 2.7) append the final projections, if any
        if (!projections.isEmpty()) root_operator = pipeFinalProjections(projections, root_operator);
        // 2.8) append the final sorts, if any
        if (!sorts.isEmpty()) root_operator = pipeSorts(sorts, root_operator);
        // 2.9) append the final groups, if any
        if (!groups.isEmpty()) root_operator = pipeGroups(groups, root_operator);

        System.out.println(" *** Built execution plan: ***");
        System.out.println(root_operator.toString());
        // Prepare the root as a pipeline previous to return it
        try {
            root_operator = new Pipe(root_operator, sm, filename);
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't build final sink operator (" + ee.getMessage() + ").", ee);
        }
	
        return root_operator;
    } // buildPlan()
	
    /**
     * getTables: returns a set with the names of all tables participating in the given a physical operator.
     */
    protected Set<String> getTables(PhysicalOperator physicalOperator) throws DBxicException {
        try {
            Set<String> tables = new LinkedHashSet<String>();
            Relation rel = physicalOperator.getOutputRelation();
            for (Attribute attribute : rel) {
                String table = attribute.getTable();
                tables.add(table);
            }
            return tables;
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't obtain information about the schema.", ee);
        }
    } // getTables()


    /**
     * getTables: returns a set with the names of all tables participating in the given a list of algebraic operators.
     */
    protected Set<String> getTables(List<AlgebraicOperator> operators) {
        Set<String> inTables = new LinkedHashSet<String>();
        for (AlgebraicOperator alg : operators) {
            inTables.addAll(alg.getTables());
        }
        return inTables;
    } // getTables()




    /**
     * getProjections: returns the projections found in the given list of algebraic operators
     */
    protected List<Projection> getProjections(List<AlgebraicOperator> operators) {
        List<Projection> v = new ArrayList<Projection>();
        for (AlgebraicOperator alg : operators)
            if (alg instanceof Projection) v.add((Projection) alg);
        return v;
    } // getProjections()


    /**
     * getSelections: returns the selections found in the given list of algebraic operators
     */
    protected List<Selection> getSelections(List<AlgebraicOperator> operators) {
        List<Selection> v = new ArrayList<Selection>();
        for (AlgebraicOperator alg : operators)
            if (alg instanceof Selection) v.add((Selection) alg);
        return v;
    } // getSelections()


    /**
     * getJoins: returns the joins found in the given list of algebraic operators
     */
    protected List<Join> getJoins(List<AlgebraicOperator> operators) {
        List<Join> v = new ArrayList<Join>();
        for (AlgebraicOperator alg : operators)
            if (alg instanceof Join) v.add((Join) alg);
        return v;
    } // getJoins()

    /**
     * getSorts: returns the sorts found in the given list of algebraic operators
     */
    protected List<Sort> getSorts(List<AlgebraicOperator> operators) {
        List<Sort> v = new ArrayList<Sort>();
        for (AlgebraicOperator alg : operators)
            if (alg instanceof Sort) v.add((Sort) alg);

        return v;
    } // getSorts()

    /**
     * getGroups: returns the groups found in the given list of algebraic operators
     */
    protected List<Group> getGroups(List<AlgebraicOperator> operators) {
        List<Group> v = new ArrayList<Group>();
        for (AlgebraicOperator alg : operators)
            if (alg instanceof Group) v.add((Group) alg);

        return v;
    } // getGroups()



    /**
     * buildInitialProjections: returns the list of projections that can be carried out from the very beginning,
     * given a list of algebraic operators, for a specific set of tables. That is, all the attributes that
     * won't be output and are not required for any operation
     */
    protected List<Projection> buildInitialProjections(List<AlgebraicOperator> operators,
                                                       Set<String> tables) throws DBxicException {
        
        List<Projection> initProjections = new ArrayList<Projection>();
        
        for (String table : tables) {
            Set<Variable> listVars = getAttributes(table, operators); // this are the variables that we will need
            // We won't add an initial projection if, after all, it won't help to reduce the no. attributes
            if (listVars.size() < catalog.getTable(table).getNumberOfAttributes()) {
                initProjections.add(new Projection(new ArrayList<Variable>(listVars)));
            }
        }

        return initProjections;
    } // getInitialProjections()

    
    /**
     * Given a table name and a list of algebraic operators, 
     * return a projection list of all accessed attributes.
     */
    protected Set<Variable> getAttributes(String table, List<? extends AlgebraicOperator> operators) {

        Set<Variable> outList = new LinkedHashSet<Variable>();

        for (AlgebraicOperator alg : operators) {
            List<Variable> vars = alg.getVariables();
            for (Variable var : vars) {
                String pTable = var.getTable();
                if (table.equals(pTable)) outList.add(var);
            }
        }
        
        return outList;
    } // getAttributes()

    
    /**
     * buildScans: returns a list of physical scan operators given a set of tables.
     */
    protected List<PhysicalOperator> buildScans(Set<String> tables) throws DBxicException {
        try {
            List<PhysicalOperator> scans = new ArrayList<PhysicalOperator>();
            for (String table : tables) {
                Table schema = catalog.getTable(table);
                String filename = catalog.getTableFileName(table);
                RelationScan rs = new RelationScan(sm, schema, filename);
                scans.add(rs);
            }
            return scans;
        }
        catch (NoSuchElementException nste) {
            throw new DBxicException("Error! Couldn't obtain schema information.", nste);
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error! Couldn't instantiate relation scans.", ee);
        }
    } // buildScans()
	
    /**
     * pipeInitialProjectionsToScan: Imposes the initial projections over the scan operations, that is, it creates
     * a new operator that carries out the corresponding projection immediately after the scan
     */
    protected List<PhysicalOperator> pipeInitialProjectionsToScan(List<Projection> iProjections, List<PhysicalOperator> scans)
            throws DBxicException {
        try {
            List<PhysicalOperator> branches = new ArrayList<PhysicalOperator>();
            for (PhysicalOperator op : scans) {
                RelationScan rs = (RelationScan) op;
                Projection ip = findRelevantInitialProjection(iProjections, rs);
                if (ip != null) {  // if there is a relevant initial projection, introduce it in the pipeline
                    op = new Project(rs, listOfVariablesToArrayOfSlots(ip.projectionAttrs(), rs.getOutputRelation()));
                }
                branches.add(op);
            }
            return branches;
        }
        catch (DBxicException ee) {
            DBxicException pbe = new DBxicException("Error: Couldn't instantiate initial projections");
            pbe.setStackTrace(ee.getStackTrace());
            throw pbe;
        }		
    } // pipeInitialProjectionsToScan()

    
    /**
     * findRelevantInitialProjection: given a list of projections and a scan, returns the relevant projection,
     * that is, the one that uses the same table as the scan
     */
    protected Projection findRelevantInitialProjection(List<Projection> ips, RelationScan rs) throws DBxicException {
        
        try {
            String scanTable = rs.getOutputRelation().getAttribute(0).getTable();
            for (Projection ip : ips) {
                String projTable = ip.getVariables().get(0).getTable();
                if (projTable.equals(scanTable)) return ip;
            }
            return null;
        }
        catch (DBxicException ee) {
            throw new DBxicException("Initial projections could not be imposed.", ee);
        }
    } // findRelevantInitialProjection()

    
    /**
     * pipeSelections: Imposes the selections over execution branches.
     */
    protected List<PhysicalOperator> pipeSelections(List<Selection> selections, List<? extends PhysicalOperator> sourceOps)
            throws DBxicException {
        try {
            List<PhysicalOperator> res = new ArrayList<PhysicalOperator>();
            
            for (PhysicalOperator op : sourceOps) {
                String oTable = op.getOutputRelation().getAttribute(0).getTable();
                List<Selection> rSelections = findRelevantSelections(selections, oTable);
                if (rSelections.size() != 0) {
                    // if there are relevant selection(s), introduce them in cascade in the pipeline
                    PhysicalOperator top = op;
                    for (Selection sIt : rSelections) {
                        Select ps = new Select(top, propositionToPredicate(sIt.getProposition(), top));
                        res.add(ps);
                        top = ps;
                    }
                }
                else { // if not, just introduce the unmodified operator
                    res.add(op);
                }
            }
            return res;
        }
        catch (DBxicException ee) {
            throw new DBxicException("Could not instantiate selections.", ee);
        }
    } // pipeSelections()

    
    /**
     * findRelevantSelections: given a list of selections and a table name, identifies which are the
     * relevant selections on that table, that is, the ones that use that table.
     */
    protected List<Selection> findRelevantSelections(List<Selection> selections, String table) {
        List<Selection> rs = new ArrayList<Selection>();
        for (Selection s : selections) {
            String sTable = (String) s.getTables().toArray()[0];
            if (table.equals(sTable)) rs.add(s);
        }
        return rs;
    } // findRelevantSelections()

    
    /**
     * listOfVariablesToArrayOfSlots: converts a list of variables for a given relation into an array of slots.
     */
    protected int [] listOfVariablesToArrayOfSlots(Iterable<Variable> pl, Relation rel) throws DBxicException {
        try {
            List<Integer> slots = new ArrayList<Integer>();
            for (Variable v : pl) {
                int slot = findVariableInRelation(v,rel);
                if (slot >= 0) slots.add(slot);
            }

            int [] arrSlots = new int[slots.size()];
            int i = 0;
            for (Integer in : slots) arrSlots[i++] = in.intValue();
            return arrSlots;
        }
        catch (ClassCastException cce) {
            throw new DBxicException("Error: Couldn't cast attributes.", cce);
        }
    } // listOfVariablesToArrayOfSlots()

    /**
     * findVariableInRelation: identifies the slot related to a given variable in the given relation
     */
    protected int findVariableInRelation(Variable var, Relation rel) {
        int i = 0;
        for (Attribute a : rel) {
            if (var.getTable().equals(a.getTable()) && var.getAttribute().equals(a.getName())) return i;
            i++;
        }
        return -1;
    }
    
    /**
     * propositionToPredicate: Converts a logical proposition to a physical predicate that
     * works on the output relation of the given operator
     */
    protected Predicate propositionToPredicate(Proposition q, PhysicalOperator op) throws DBxicException {
        try {
            Relation rel = op.getOutputRelation();
            if (q.rightIsValue()) { // if the proposition is of type [Variable Op Value]
                TupleSlotPointer tsp = createSlotPointer(q.getLeftVariable(), rel);
                Comparable c = TypeCasting.createComparable(tsp.getType(), q.getRightValue());
                return new AtomicCondition(tsp, c, q.getRelationship());
            }
            else if (q.rightIsVariable()) { // if the proposition is of type [Variable1 Op Variable2]
                TupleSlotPointer leftTsp = createSlotPointer(q.getLeftVariable(), rel);
                TupleSlotPointer rightTsp = createSlotPointer(q.getRightVariable(), rel);
                return new AtomicCondition(leftTsp, rightTsp, q.getRelationship());
            }
            else {
                AtomicCondition cnd = new AtomicCondition();
                cnd.setAsTrue();
                return cnd;
            }
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't obtain schema information.", ee);
        }
    } // propositionToPredicate()
    

    /**
     * propositionToPredicate: Converts a logical proposition to a physical predicate that works on the output
     * relation of two different input operators (used for joins).
     *
    protected Predicate propositionToPredicate(Proposition q, PhysicalOperator left, PhysicalOperator right)
            throws DBxicException {
        
        try {
            Relation leftRel = left.getOutputRelation();
            Relation rightRel = right.getOutputRelation();
            // this conversion only makes sense if the proposition is of type [Variable1 Op Variable2] (for joins)
            if (q.rightIsVariable()) {
                TupleSlotPointer leftTsp = createSlotPointer(q.getLeftVariable(), leftRel);
                TupleSlotPointer rightTsp = createSlotPointer(q.getRightVariable(), rightRel);
                return new AtomicCondition(leftTsp, rightTsp, Logics.reverse(q.getRelationship())); // todo: not sure, this needs reversion
            }
            else {
                AtomicCondition cnd = new AtomicCondition();
                cnd.setAsTrue();
                return cnd;
            }
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't obtain schema information.", ee);
        }
    } // propositionToPredicate()*/


    /**
     * createSlotPointer: creates a slot pointer for a variable in a relation
     */
    protected TupleSlotPointer createSlotPointer(Variable var, Relation r) {
        int slot = findVariableInRelation(var, r);
        TupleSlotPointer tsp = null;
        if (slot >= 0)
            tsp = new TupleSlotPointer(null, slot, r.getAttribute(slot).getType());
        return tsp;
    } // createSlotPointer()

    
    /**
     * enumerateJoins: recursive method that creates a tree of joins from a collection of sub-plans
     * and a list of algebraic joins.
     */
    protected List<PhysicalOperator> cascadeJoins(List<Join> joins, List<PhysicalOperator> branches) throws DBxicException {
        try {
            if (branches.size() == 1 && joins.size() == 0) return branches;
            else if (branches.size() == 1 && joins.size() != 0)
                throw new DBxicException("Error: Couldn't enumerate joins");

            // pick a join proposition with the corresponding pair of operators (sub-plans)
            Triplet<PhysicalOperator, PhysicalOperator, List<Join>> triplet = pickPair(joins, branches);
            // remove the two picked operators (sub-plans) from the list
            branches.remove(triplet.first);
            branches.remove(triplet.second);
            // creates the join operator that will combine these two
            BasicJoinOperator pj = null;
            if (triplet.third == null) { // the new join operator is a cartesian product
                pj = new CartesianProduct(triplet.first, triplet.second, sm);
            }
            else { // create a new join operator
                pj = createJoin(triplet.first, triplet.second, triplet.third);
            }
            branches.add(pj);

            return cascadeJoins(joins, branches);
        }
        catch (DBxicException ee) {
            throw new DBxicException("Could not enumerate joins.", ee);
        }
    } // enumerateJoins()


    /**
     * pickPair: picks two sub-plans to join from a collection of algebraic joins and a collection of
     * operations (sub-plans). Returns a triplet consisting of two operators (sub-plans) and
     * the algebraic join(s). If this last element is null, a Cartesian product is to be built.
     */
    protected Triplet<PhysicalOperator, PhysicalOperator, List<Join>>
           pickPair (List<Join> joins, List<PhysicalOperator> sourceOps) throws DBxicException {
        
        for (int i = 0; i < sourceOps.size(); i++) {
            PhysicalOperator left = sourceOps.get(i);
            for (int j = i; j < sourceOps.size(); j++) {
                PhysicalOperator right = (PhysicalOperator) sourceOps.get(j);
                List<Join> filteredLJoins = filterJoinsToOperators(left, right, joins);
                if (!filteredLJoins.isEmpty())
                    return new Triplet<PhysicalOperator, PhysicalOperator, List<Join>>(left, right, filteredLJoins);
            }
        }
		
        return new Triplet<PhysicalOperator, PhysicalOperator, List<Join>>(sourceOps.get(0), sourceOps.get(1), null);
    } // pickPair()

    
    /**
     * filterJoinsToOperators: given two operators (sub-plans) and a collection of joins,
     * return all joins over the operators
     */
    protected List<Join> filterJoinsToOperators(PhysicalOperator leftOp,
                                                PhysicalOperator rightOp, List<Join> joins) throws DBxicException {
        List<Join> outLJoins = new ArrayList<Join>();
        // find the tables in each sub-plan
        Set<String> leftTables = getTables(leftOp);
        Set<String> rightTables = getTables(rightOp);
        
        for (Join join : joins) {
            Proposition p = join.getProposition();
            // get the two tables of the proposition
            String leftTable = p.getLeftVariable().getTable();
            String rightTable = p.getRightVariable().getTable();
            if ((leftTables.contains(leftTable) && rightTables.contains(rightTable)) ||
                    (leftTables.contains(rightTable) && rightTables.contains(leftTable))) {
                outLJoins.add(join);
            }
        }
        joins.removeAll(outLJoins);
        return outLJoins;
    } // filterJoinsToOperators()

    
    /**
     * createJoin: Given two input operators and a collection of algebraic joins,
     * returns a physical join to evaluate them.
     * TODO: Merge Join: work here to be allowed to use Merge Join
     */
    protected BasicJoinOperator createJoin(PhysicalOperator leftOp, PhysicalOperator rightOp, List<Join> joins)
            throws DBxicException {
        try {
            Predicate pred = null;
            boolean useFastJoin = false;
            
            if (joins.size() == 1) {
                Join join = joins.get(0);
                // single join, single predicate
                pred = createJoinPredicate(leftOp, rightOp, join);
                if (isMergeable(join)) useFastJoin = true;
            }
            else {
                // build a conjunction of all relevant predicates
                List<Predicate> preds = new ArrayList<Predicate>();
                for (Join join : joins) preds.add(createJoinPredicate(leftOp, rightOp, join));
                pred = new JointOfPredicates(preds, JointOfPredicates.JunctionType.AND);
            }

            // comment the next line and uncomment the rest when you have implemented your solution for Merge Join
            return new NestedLoopsJoin(leftOp, rightOp, sm, pred);
            /*if (! useFastJoin) {
                return new NestedLoopsJoin(leftOp, rightOp, sm, pred);
            }
            else {
                // a fast join algorithm (e.g., Merge Join) can be used, so let's build the plan for it
                Join join = joins.get(0);

                Relation leftRel = leftOp.getOutputRelation();
                Relation rightRel = rightOp.getOutputRelation();
                Variable leftVar = join.getProposition().getLeftVariable();
                Variable rightVar = join.getProposition().getRightVariable();
                TupleSlotPointer leftTsp = createSlotPointer(leftVar, leftRel);
                TupleSlotPointer rightTsp = createSlotPointer(rightVar, rightRel);
                int [] leftSlots = new int[1];
                int [] rightSlots = new int[1];

                if (leftTsp == null) {
                    // Could not find the left join input in the left-hand side. Maybe it is the other way around?
                    leftTsp = createSlotPointer(rightVar, leftRel);
                    rightTsp = createSlotPointer(leftVar, rightRel);

                    if (leftTsp == null) { // still null, something must be wrong
                        throw new DBxicException("Error: Could not build merge join");
                    }
                }
                leftSlots[0] = leftTsp.getSlot();
                rightSlots[0] = rightTsp.getSlot();

                int num_runs = sm.getNumberOfBlocksInBuffer() / 2;
                num_runs = 5; //TODO: This is just a test. We should use something more intelligent... num_runs > 10 ? num_runs : 10

                // We need to provide sorted input relations to MergeJoin
                ExternalSort orderedLeftOp = new ExternalSort(leftOp, sm, leftSlots, num_runs);
                ExternalSort orderedRightOp = new ExternalSort(rightOp, sm, rightSlots, num_runs);
                // create the Merge Join operation with the sorting operations pipelined
                return new MergeJoin(orderedLeftOp, orderedRightOp, sm, leftSlots[0], rightSlots[0],
                        createJoinPredicate(orderedLeftOp, orderedRightOp, join));

            }*/
        }
        catch (DBxicException ee) {
            throw new DBxicException("Could not instantiate physical join", ee);
        }
    } // createJoin()


    /**
     * isMergeable: Checks whether sort-merge can be used or not.
     */
    protected boolean isMergeable (Join join) {
        return join.getProposition().getRelationship() == CompareRelation.EQUALS;
    } // isMergeable()

    
    /**
     * createJoinPredicate: creates a single join operator from two input sources and a join proposition.
     */
    protected Predicate createJoinPredicate(PhysicalOperator left, PhysicalOperator right, Join join) throws DBxicException {
        try {
            Proposition p = join.getProposition();
            Variable leftVar = p.getLeftVariable();
            Variable rightVar = p.getRightVariable();
            CompareRelation rel = p.getRelationship();
            TupleSlotPointer leftTsp = createSlotPointer(leftVar, left.getOutputRelation());
            TupleSlotPointer rightTsp = createSlotPointer(rightVar, right.getOutputRelation());
            if (leftTsp == null && rightTsp == null) {
                // failed instantiation, variable not found in relation
                // we need to reverse the proposition (condition) so that variable-relation are matched
                return createJoinPredicate(left, right, new Join(new Proposition(Logics.reverse(rel), rightVar, leftVar)));
            }
            else return new AtomicCondition(leftTsp, rightTsp, rel);
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't obtain schema information.", ee);
        }
    } // createJoinPredicate()

    
    /**
     * imposeFinalProjections: pipe the remaining projection operations on the root of the tree
     */
    protected PhysicalOperator pipeFinalProjections(List<Projection> projections, PhysicalOperator physicalOperator) throws DBxicException {
        try {
            List<Variable> pvl = createSingleProjectionList(projections);
            int [] slots = listOfVariablesToArrayOfSlots(pvl, physicalOperator.getOutputRelation());
            return new Project(physicalOperator, slots);
        }
        catch (DBxicException ee) {
            throw new DBxicException("Could not instantiate final projection.", ee);
        }
    } // imposeFinalProjections()


    /**
     * createSingleProjectionList: creates a single projection list from a list of them
     */
    protected List<Variable> createSingleProjectionList(List<Projection> projections) {
        List<Variable> pl = projections.get(0).getVariables();
        for (int i = 1; i < projections.size(); i++) {
            Projection p = projections.get(i);
            pl.addAll(p.getVariables());
        }
        return pl;
    } // createSingleProjectionList()

    /**
     * pipeSorts: pipe the final sort operator on the root operator
     * TODO: ExternalSort: work here to be allowed to use External Merge Sort
     */
    protected PhysicalOperator pipeSorts(List<Sort> sorts, PhysicalOperator physicalOperator) throws DBxicException {
        // comment the next line and uncomment the rest when you have implemented your solution for External Sort
        return physicalOperator;
        /*try {
            if (sorts.size() > 1) {
                throw new DBxicException("Error: Only one sort clause allowed.");
            }
            else if (sorts.size() == 1) {                
                Sort sort = sorts.get(0);
                int [] slots = listOfVariablesToArrayOfSlots(sort.getSortList(), physicalOperator.getOutputRelation());

                int num_runs = sm.getNumberOfBlocksInBuffer() / 2;
                num_runs = 5; //TODO: This is just a test. We should use something more intelligent... num_runs > 10 ? num_runs : 10
                return new ExternalSort(physicalOperator, sm, slots, num_runs);
            }
            else {
                return physicalOperator;
            }
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't instantiate final sort.", ee);
        }*/
    } // pipeSorts

    /**
     * pipeGroups: pipe the final grouping operator on the root operator
     */
    protected PhysicalOperator pipeGroups(List<Group> groups, PhysicalOperator physicalOperator) throws DBxicException {
        return physicalOperator; // not fully implemented yet
        /*try {
            if (groups.size() > 1) {
                throw new DBxicException("Error: Only one group clause allowed.");
            }
            else if (groups.size() == 1) {                
                Group group = groups.get(0);
                List<Variable> sl = group.getVariables();
                Relation relation = physicalOperator.getOutputRelation();
                int [] slots = listOfVariablesToArrayOfSlots(sl, relation);

                ///////////////////////////////////////////
                //
                // A possible implementation would be something like this, plus the aggregation functions (not implemented)
                int num_runs = sm.getNumberOfBlocksInBuffer() / 2;
                num_runs = 5; //TODO: This is just a test. We should use something more intelligent... num_runs > 10 ? num_runs : 10
                return new ExternalSort(physicalOperator, sm, slots, num_runs);
                ///////////////////////////////////////////
            }
            else {
                return physicalOperator;
            }
        }
        catch (DBxicException ee) {
            throw new DBxicException("Error: Couldn't instantiate grouping.", ee);
        }*/
    }

} // PlanBuilder
