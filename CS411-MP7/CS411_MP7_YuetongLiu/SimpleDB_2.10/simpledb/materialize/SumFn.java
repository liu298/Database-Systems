package simpledb.materialize;

import simpledb.query.*;

/**
 * The <i>sum</i> aggregation function.
 * @author Yuetong Liu
 */
public class SumFn implements AggregationFn {
   private String fldname;
   private int sum;
   
   /**
    * Creates a sum aggregation function for the specified field.
    * @param fldname the name of the aggregated field
    */
   public SumFn(String fldname) {
      this.fldname = fldname;
   }
   
   /**
    * Starts a new sum.
    * Since SimpleDB does not support null values,
    * every record will be counted,
    * regardless of the field.
    * The current count is thus set to 1.
    * @see simpledb.materialize.AggregationFn#processFirst(simpledb.query.Scan)
    */
   public void processFirst(Scan s) {
      sum = Integer.parseInt(s.getVal(fldname).toString());
   }
   
   /**
    * Since SimpleDB does not support null values,
    * this method always increments the sum,
    * regardless of the field.
    * @see simpledb.materialize.AggregationFn#processNext(simpledb.query.Scan)
    */
   public void processNext(Scan s) {
      sum = sum + Integer.parseInt(s.getVal(fldname).toString());
   }
   
   /**
    * Returns the field's name, prepended by "sumof".
    * @see simpledb.materialize.AggregationFn#fieldName()
    */
   public String fieldName() {
      return "sumof" + fldname;
   }
   
   /**
    * Returns the current sum.
    * @see simpledb.materialize.AggregationFn#value()
    */
   public Constant value() {
      return new IntConstant(sum);
   }
}
