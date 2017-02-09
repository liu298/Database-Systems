package simpledb.parse;

import simpledb.query.*;
import java.util.*;
import simpledb.materialize.*;

/**
 * Data for the SQL <i>select</i> statement.
 * @author Edward Sciore
 */
public class QueryData {
   private Collection<String> fields;
   private Collection<String> tables;
   private Predicate pred;
   private int limit = -1; 
   private Collection<String> groupFields = new ArrayList<String>();
   private Collection<String> sortFields = new ArrayList<String>();
   /**
    * Saves the field and table list and predicate.
    */
   // constructor  
   public QueryData(Collection<String> fields, Collection<String> tables, Predicate pred){
      this.fields = fields;
      this.tables = tables;
      this.pred = pred;
   }   
     
   public QueryData(Collection<String> fields, Collection<String> tables, Predicate pred, 
                    int limit,Collection<String> groupFields, Collection<String> sortFields){
      this.fields = fields;
      this.tables = tables;
      this.pred = pred;
      this.limit = limit;
      this.groupFields = groupFields;
      this.sortFields = sortFields;
   }
   
   // extracted aggregationFn and aggregation fields if necessary
    public  Collection<AggregationFn> aggFns(){
      Collection<AggregationFn> aggFns = new ArrayList<AggregationFn>();
      for (String field : fields) {
        if (field.matches("^countof.*$")) {
          String fldname = field.substring(field.indexOf("f")+1);
          aggFns.add(new CountFn(fldname));
        }
        else if (field.matches("^max.*$")) {
          String fldname = field.substring(field.indexOf("f")+1);
          aggFns.add(new MaxFn(fldname));
        }
        else if (field.matches("^minof.*$")) {
          String fldname = field.substring(field.indexOf("f")+1);
          aggFns.add(new MinFn(fldname));
        }
        else if (field.matches("^sumof.*$")) {
          String fldname = field.substring(field.indexOf("f")+1);
          aggFns.add(new SumFn(fldname));
        }
        else if (field.matches("^avgof.*$")) {
          String fldname = field.substring(field.indexOf("f")+1);
          aggFns.add(new AvgFn(fldname));
        }
      }
      return aggFns;
    }
   
   /**
    * Returns the fields mentioned in the select clause.
    * @return a collection of field names
    */
   public Collection<String> fields() {
      return fields;
   }
   
   /**
    * Returns the tables mentioned in the from clause.
    * @return a collection of table names
    */
   public Collection<String> tables() {
      return tables;
   }
   
   /**
    * Returns the predicate that describes which
    * records should be in the output table.
    * @return the query predicate
    */
   public Predicate pred() {
      return pred;
   }
   
   public int limit() {
      return limit;
   }
   
   public Collection<String> groupFields() {
      return groupFields;
   }
    public Collection<String> DescSortFields() {
      return sortFields;
   }
   public Collection<String> sortFields() {
      Collection<String> newSortFields = new ArrayList<String>();
      for (String fldname: sortFields){
         if (fldname != "desc")
             newSortFields.add(fldname);
      }
      return newSortFields;
   }

   public Collection<Boolean> desc(){
      Collection<Boolean> desc = new ArrayList<Boolean>();
      int idx = 0;
      for (String fldname: sortFields){
         idx = Math.min(idx+1,sortFields.size()-1);
         if (fldname != "desc" && sortFields.toArray()[idx] == "desc")
            desc.add(true);
         else if (fldname != "desc")
            desc.add(false);
      }
      return desc;
   }
   
   public String toString() {
      String result = "select ";
      if (fields.size() == 0)
         result += " * "; 
      else {
         for (String fldname : fields)
            result += fldname + ", ";
         result = result.substring(0, result.length()-2); //remove final comma
      }
      result += " from ";
      for (String tblname : tables)
         result += tblname + ", ";
      result = result.substring(0, result.length()-2); //remove final comma
      String predstring = pred.toString();
      if (!predstring.equals(""))
         result += " where " + predstring;

       // change for group by
       if(!groupFields.isEmpty()){
           result += " group by ";
           for (String fldname : groupFields)
               result += fldname + ", ";
           result = result.substring(0, result.length()-2); //remove final comma
       }

       // change for sort by
       if(!sortFields.isEmpty()){
           result += " order by ";
           int idx = 0;
           for (String fldname : sortFields){
               idx = Math.min(idx+1,sortFields.size()-1);
               if (fldname != "desc" && sortFields.toArray()[idx] == "desc")
                   result += fldname + " ";
               else
                   result += fldname + ", ";
           }
           result = result.substring(0, result.length()-2); //remove final comma
       }

      if (limit > 0)
         result += " limit " + limit;

      return result;
   }
}
