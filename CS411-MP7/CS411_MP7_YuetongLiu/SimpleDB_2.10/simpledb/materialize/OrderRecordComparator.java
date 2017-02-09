package simpledb.materialize;

import simpledb.query.*;
import java.util.*;

/**
 * A comparator for scans.
 * @author Yuetong Liu
 */
public class OrderRecordComparator implements Comparator<Scan> {
   private List<String> fields;
   // add desc
   private List<Boolean> descs;
   
   /**
    * Creates a comparator using the specified fields,
    * using the ordering implied by its iterator.
    * @param fields a list of field names
    */
   public OrderRecordComparator(List<String> fields, List<Boolean> desc) {
      this.fields = fields;
      this.descs = desc;
   }
   
   /**
    * Compares the current records of the two specified scans.
    * The sort fields are considered in turn.
    * When a field is encountered for which the records have
    * different values, those values are used as the result
    * of the comparison.
    * If the two records have the same values for all
    * sort fields, then the method returns 0.
    * @param s1 the first scan
    * @param s2 the second scan
    * @return the result of comparing each scan's current record according to the field list
    */
   public int compare(Scan s1, Scan s2) {
      int idx = 0;
      for (String fldname : fields) {
         boolean desc = descs.get(idx);
         Constant val1 = s1.getVal(fldname);
         Constant val2 = s2.getVal(fldname);
         int result = val1.compareTo(val2);
         if(desc)
            result = -result;
         if (result != 0)
            return result;
         idx++;
      }
      return 0;
   }
}
