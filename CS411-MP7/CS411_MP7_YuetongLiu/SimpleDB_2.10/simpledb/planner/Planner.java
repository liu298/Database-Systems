package simpledb.planner;

import simpledb.tx.Transaction;
import simpledb.server.SimpleDB;
import simpledb.metadata.*;
import simpledb.parse.*;
import simpledb.query.*;
import java.util.*;
import simpledb.index.planner.*;

/**
 * The object that executes SQL statements.
 * @author sciore
 */
public class Planner {
   private QueryPlanner qplanner;
   private UpdatePlanner uplanner;
   private UIPlanner uiplanner;
   
   public Planner(QueryPlanner qplanner, UpdatePlanner uplanner,
                  UIPlanner uiplanner) {
      this.qplanner = qplanner;
      this.uplanner = uplanner;
      this.uiplanner = uiplanner;
   }
   
   private boolean hasIdx(String tbl, Transaction tx) {
      boolean hasIdx = false;
      Map<String,IndexInfo> indexes = SimpleDB.mdMgr().getIndexInfo(tbl, tx);
      if (indexes.size() > 0)
         hasIdx = true;
      return hasIdx;
   }
   
   private boolean predHasIdx(QueryData data, Transaction tx){
      Collection<String> tables = data.tables();
      Collection<String> idxFields = new ArrayList<String>();
      for (String tbl:tables) {
         Map<String,IndexInfo> idxs = SimpleDB.mdMgr().getIndexInfo(tbl, tx);
         for (String key:idxs.keySet())
            idxFields.add(key);
      }
      Predicate pred = data.pred();
      boolean predHasIdx = false;
      for (String idxField:idxFields) {
         if (pred.equatesWithConstant(idxField) != null || 
                   pred.equatesWithField(idxField) != null){
            predHasIdx = true;
            break;
         }
      }
      return predHasIdx;
   }
   
   /**
    * Creates a plan for an SQL select statement, using the supplied planner.
    * @param qry the SQL query string
    * @param tx the transaction
    * @return the scan corresponding to the query plan
    */
   public Plan createQueryPlan(String qry, Transaction tx) {
      Parser parser = new Parser(qry);
      String lowerQry = qry.toLowerCase();
      if (lowerQry.contains("union") || lowerQry.contains("intersect")) {
         UIData data = parser.unionIntersect();
         return uiplanner.createUIPlan(data, tx);
      }
      else {
         QueryData data = parser.query();
         if (predHasIdx(data, tx))
            qplanner = new IndexQueryPlanner();
         return qplanner.createPlan(data, tx);
      }
   }
   
   /**
    * Executes an SQL insert, delete, modify, or
    * create statement.
    * The method dispatches to the appropriate method of the
    * supplied update planner,
    * depending on what the parser returns.
    * @param cmd the SQL update string
    * @param tx the transaction
    * @return an integer denoting the number of affected records
    */
   public int executeUpdate(String cmd, Transaction tx) {
      Parser parser = new Parser(cmd);
      Object obj = parser.updateCmd();
         
      if (obj instanceof InsertData) {
         InsertData data = (InsertData)obj;
         if (hasIdx(data.tableName(), tx))
            uplanner = new IndexUpdatePlanner();
         return uplanner.executeInsert(data, tx);
      }
      else if (obj instanceof DeleteData) {
         DeleteData data = (DeleteData)obj;
         if (hasIdx(data.tableName(), tx))
            uplanner = new IndexUpdatePlanner();
         return uplanner.executeDelete(data, tx);
      }
      else if (obj instanceof ModifyData) {
         ModifyData data = (ModifyData)obj;
         if (hasIdx(data.tableName(), tx))
            uplanner = new IndexUpdatePlanner();
         return uplanner.executeModify(data, tx);
      }
      else if (obj instanceof CreateTableData)
         return uplanner.executeCreateTable((CreateTableData)obj, tx);
      else if (obj instanceof CreateViewData)
         return uplanner.executeCreateView((CreateViewData)obj, tx);
      else if (obj instanceof CreateIndexData)
         return uplanner.executeCreateIndex((CreateIndexData)obj, tx);
      else
         return 0;
   }
}
