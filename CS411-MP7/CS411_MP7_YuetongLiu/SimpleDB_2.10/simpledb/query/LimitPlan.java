package simpledb.query;

import simpledb.record.Schema;


public class LimitPlan implements Plan {
   private Plan p;
   private int limit;
   
   public LimitPlan(Plan p, int limit) {
      this.p = p;
      this.limit = limit;
   }
   
   public Scan open() {
      Scan s = p.open();
      return new LimitScan(s, limit);
   }
   
   public int blocksAccessed() {
      return p.blocksAccessed();
   }
   
   public int recordsOutput() {
      return p.recordsOutput();
   } 
   
   public int distinctValues(String fldname) {
      return p.distinctValues(fldname);
   }  
   
   public Schema schema() {
      return p.schema();
   }
}
