package simpledb.query;

import java.util.*;

public class UnionScan implements Scan {
   private Scan s1;
   private Scan s2;
   private Collection<String> fieldlist;
   private Scan s;
   private Set<String> set;
   private boolean isS1;
   
   public UnionScan(Scan s1, Scan s2, Collection<String> fieldlist) {
      this.s1 = s1;
      this.s2 = s2;
      this.fieldlist = fieldlist;
      this.isS1 = true;
      this.s = s1;
      this.set = new HashSet<String>();
      System.out.println("UnionScan constructed");
   }
   
   public void beforeFirst() {
      s = s1;
      s.beforeFirst();
      this.isS1 = true;
   }
   
   public boolean next() {
      boolean nex = s.next();
      if (isS1 && !nex) {
         this.isS1 = false;
         s = s2;
         nex = s.next();
      }
      if (nex) {
         String record = new String();
         for (String fld : fieldlist)
            record = record + s.getVal(fld).toString();
         if (set.contains(record))
            return this.next();
         else
            set.add(record);
      }
      return nex;
   }
   
   public void close() {
      s1.close();
      s2.close();
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
      return fieldlist.contains(fldname);
   }
   
}
