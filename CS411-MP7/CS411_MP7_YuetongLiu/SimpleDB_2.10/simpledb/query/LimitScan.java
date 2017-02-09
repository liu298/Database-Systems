package simpledb.query;

import simpledb.record.Schema;

public class LimitScan implements Scan {
   private Scan s;
   private int limit;
   
   public LimitScan(Scan s, int limit) {
      this.s = s;
      this.limit = limit;
   }
   
   public void beforeFirst() {
      s.beforeFirst();
   }
   
   public boolean next() {
      if (limit > 0) {
         limit -= 1;
         return s.next();
      } else {
         return false;
      }
   }
   
   public void close() {
      s.close();
   }
   
   public Constant getVal(String fldname) {
      if (hasField(fldname))
         return s.getVal(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public int getInt(String fldname) {
      if (hasField(fldname))
         return s.getInt(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public String getString(String fldname) {
      if (hasField(fldname))
         return s.getString(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public boolean hasField(String fldname) {
      return s.hasField(fldname);
   }
}
