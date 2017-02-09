package simpledb.query;

import simpledb.record.Schema;
import java.util.*;

public class UnionPlan implements Plan {
   private Plan p1;
   private Plan p2;
   private Schema schema = new Schema();
   
   public UnionPlan(Plan p1, Plan p2) {
      this.p1 = p1;
      this.p2 = p2;
      this.schema = p1.schema();
   }
   
   public Scan open() {
      Scan s1 = p1.open();
      Scan s2 = p2.open();
      return new UnionScan(s1, s2, schema.fields());
   }
   
   public int blocksAccessed() {
      return p1.blocksAccessed() + p2.blocksAccessed();
   }
   
   public int recordsOutput() {
      return p1.recordsOutput() + p2.recordsOutput();
   }
   
   public int distinctValues(String fldname) {
      return p1.distinctValues(fldname) + p2.distinctValues(fldname);
   }
   
   public Schema schema() {
      return schema;
   }
}
