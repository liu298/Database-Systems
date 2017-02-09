package simpledb.query;

import java.util.*;

public class IntersectScan implements Scan {
   private Scan s_more;
   private Scan s_less;
   private Collection<String> fieldlist;
   private Set<String> set = new HashSet<String>();
   
   public IntersectScan(Scan s_more, Scan s_less, Collection<String> fieldlist) {
      this.s_more = s_more;
      this.s_less = s_less;
      this.fieldlist = fieldlist;
      while (s_less.next()) {
         String record = new String();
         for (String fld : fieldlist)
            record = record + s_less.getVal(fld).toString();
            if (!set.contains(record)){
               set.add(record);
            }
      }
      System.out.println("IntersectScan constructed");
   }
   
   public void beforeFirst() {
      s_more.beforeFirst();
   }
   
   public boolean next() {
      while (s_more.next()) {
         String record = new String();
         for (String fld : fieldlist)
            record = record + s_more.getVal(fld).toString();
         if (set.contains(record)){
            set.remove(record);
            return true;
         }
      }
      return false;
   }
   
   public void close() {
      s_more.close();
      s_less.close();
   }
   
   public Constant getVal(String fldname) {
      if (hasField(fldname))
         return s_more.getVal(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public int getInt(String fldname) {
      if (hasField(fldname))
         return s_more.getInt(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public String getString(String fldname) {
      if (hasField(fldname))
         return s_more.getString(fldname);
      else
         throw new RuntimeException("field " + fldname + " not found.");
   }
   
   public boolean hasField(String fldname) {
      return fieldlist.contains(fldname);
   }
}
