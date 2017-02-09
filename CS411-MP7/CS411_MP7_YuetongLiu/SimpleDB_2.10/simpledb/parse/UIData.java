package simpledb.parse;

import simpledb.query.*;
import simpledb.parse.QueryData;
import java.util.*;

public class UIData {
   private QueryData q1;
   private QueryData q2;
   private int type; //Union: type=1, Intersect: type=2
   
   public UIData(QueryData q1, QueryData q2, int type) {
      this.q1 = q1;
      this.q2 = q2;
      this.type = type;
   }
   
   public QueryData firstQ() {
      return q1;
   }
   
   public QueryData secondQ() {
      return q2;
   }
   
   public int type() {
      return type;
   }
   
   public String toString() {
      String s1 = q1.toString();
      String s2 = q2.toString();
      if (type == 1)
         return s1 + " union " + s2;
      else
         return s1 + " intersect " + s2;
   }
}
