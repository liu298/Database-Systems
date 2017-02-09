package simpledb.materialize;

import simpledb.query.*;

/**
 * The <i>avg</i> aggregation function.
 * @author Yuetong Liu
 */
public class AvgFn implements AggregationFn {
   private String fldname;
   private int sum;
   private int count;
   /**
    * Creates a sum aggregation function for the specified field.
    * @param fldname the name of the aggregated field
    */
   public AvgFn(String fldname) {
      this.fldname = fldname;
   }
   
   /**
    * Starts a new avg.
    * Since SimpleDB does not support null values,
    * every record will be counted,
    * regardless of the field.
    * @see simpledb.materialize.AggregationFn#processFirst(simpledb.query.Scan)
    */
   public void processFirst(Scan s) {
    sum = Integer.parseInt(s.getVal(fldname).toString());
    count = 1;
   }
   
   /**
    * Since SimpleDB does not support null values,
    * this method always increments the sum,
    * regardless of the field.
    * @see simpledb.materialize.AggregationFn#processNext(simpledb.query.Scan)
    */
   public void processNext(Scan s) {
    sum = sum + Integer.parseInt(s.getVal(fldname).toString());
    count++;
   }
   
   /**
    * Returns the field's name, prepended by "sumof".
    * @see simpledb.materialize.AggregationFn#fieldName()
    */
   public String fieldName() {
      return "avgof" + fldname;
   }
   
   /**
    * Returns the current avg.
    * Got from sum / count
    * @see simpledb.materialize.AggregationFn#value()
    */
   public Constant value() {
      return new IntConstant(sum/count);
   }
}
