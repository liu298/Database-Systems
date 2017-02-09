package simpledb.index.planner;

import simpledb.tx.Transaction;
import simpledb.query.*;
import simpledb.parse.*;
import simpledb.server.SimpleDB;
import java.util.*;
import simpledb.index.*;
import simpledb.index.query.*;
import simpledb.planner.*;
import simpledb.metadata.IndexInfo;


public class IndexQueryPlanner implements QueryPlanner {
   public Plan createPlan(QueryData data, Transaction tx) {
      //step1: create a tableplan for each table 
      System.out.println("IndexQueryPlanner is called");
      ArrayList<Plan> tablePlans = new ArrayList<Plan>();
      Collection<String> tblNames = data.tables();
      ArrayList<String> tableNames = new ArrayList<String>(tblNames);

      for (String tblname : tableNames){
         String viewdef = SimpleDB.mdMgr().getViewDef(tblname, tx);
         if (viewdef != null)
            tablePlans.add(SimpleDB.planner().createQueryPlan(viewdef, tx));
         else
            tablePlans.add(new TablePlan(tblname, tx));
      }

      Predicate joinPred = new Predicate();
      for (int i=0; i<tablePlans.size()-1; i++){
         for (int j=i+1; j<tablePlans.size(); j++) {
            Predicate foo = data.pred().joinPred(tablePlans.get(i).schema(),
                                                 tablePlans.get(j).schema());
            if (foo != null)
               joinPred.conjoinWith(foo);
         }
      }

      boolean hasJoined = false;
      if (joinPred.size() > 0){
         ArrayList<String> idxFields = new ArrayList<String>();
         ArrayList<IndexInfo> idxInfos = new ArrayList<IndexInfo>();
         Map<String,IndexInfo> idxs = new HashMap<String,IndexInfo>();
         for (String tbl : tableNames) {
            idxs = SimpleDB.mdMgr().getIndexInfo(tbl, tx);
            for (String key:idxs.keySet()) {
               idxFields.add(key);
               idxInfos.add(idxs.get(key));
            }
         }
         
         Plan p = tablePlans.get(0); // This is just for initialization
         for (String idxField : idxFields) {
            String eqlField = joinPred.equatesWithField(idxField);
            if (eqlField != null) {
               Plan idxPlan = tablePlans.get(0); // just for initialization
               Plan eqlPlan = tablePlans.get(0); // just for initialization
               for (Plan tblPlan : tablePlans) {
                  if (tblPlan.schema().hasField(idxField))
                     idxPlan = tblPlan;
                  if (tblPlan.schema().hasField(eqlField))
                     eqlPlan = tblPlan;
               }
               tablePlans.remove(idxPlan);
               System.out.println("remove a plan, remaining tablePlan "+tablePlans.size());
               tablePlans.remove(eqlPlan);
               System.out.println("remove a plan, remaining tablePlan "+tablePlans.size());
               
               IndexInfo ii = idxInfos.get(idxFields.indexOf(idxField));
               p = new IndexJoinPlan(eqlPlan, idxPlan, ii, eqlField, tx);
               hasJoined = true;
               System.out.println("Index Join executed");
               
               if (tablePlans.size() > 0) {
                  for (Plan tblPlan : tablePlans) {
                     p = new ProductPlan(p, tblPlan);
                  }
               }
               break;
            }
         }
         if (hasJoined){
            p = new SelectPlan(p, data.pred());
            p = new ProjectPlan(p, data.fields());
            return p;
         }
      }

      // if the program goes to here, then index join is not executed
      Plan p = tablePlans.remove(0);
      String tbl = tableNames.remove(0);
      Predicate selectPred = data.pred().selectPred(p.schema());

      if (selectPred != null){
         int nPred = selectPred.size();
         Map<String,IndexInfo> idxs = SimpleDB.mdMgr().getIndexInfo(tbl, tx);
         for (String key : idxs.keySet()) {
            Constant c = selectPred.equatesWithConstant(key);
            if (c != null) {
               IndexInfo ii = idxs.get(key);
               p = new IndexSelectPlan(p, ii, c, tx);
               nPred -= 1;
               break;
            }
         }

         if (nPred > 0)
            p = new SelectPlan(p, selectPred);      
      }

      for (int i=0; i<tablePlans.size(); i++) {
         Plan p2 = tablePlans.get(i);
         String tbl2 = tableNames.get(i);
         selectPred = data.pred().selectPred(p2.schema());

         if (selectPred != null){
            int nPred = selectPred.size();
            Map<String,IndexInfo> idxs = SimpleDB.mdMgr().getIndexInfo(tbl2, tx);
            for (String key : idxs.keySet()) {
               Constant c = selectPred.equatesWithConstant(key);
               if (c != null) {
                  IndexInfo ii = idxs.get(key);
                  p2 = new IndexSelectPlan(p2, ii, c, tx);
                  nPred -= 1;
                  break;
               }
            }

            if (nPred > 0)
               p2 = new SelectPlan(p2, selectPred);         
         }

         p = new ProductPlan(p, p2);
      }
      
      System.out.println("Index Selection executed");
      
      p = new SelectPlan(p, joinPred);
      p = new ProjectPlan(p, data.fields());
      
      return p;
   }
   
}

