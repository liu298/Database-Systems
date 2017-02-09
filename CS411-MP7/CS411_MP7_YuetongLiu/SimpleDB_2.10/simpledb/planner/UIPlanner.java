package simpledb.planner;

import simpledb.tx.Transaction;
import simpledb.query.*;
import simpledb.parse.*;
import simpledb.parse.UIData;
import simpledb.server.SimpleDB;
import simpledb.metadata.*;
import java.util.*;
import simpledb.planner.BasicQueryPlanner;

public class UIPlanner {
   public Plan createUIPlan(UIData data, Transaction tx) {
      BasicQueryPlanner bqp = new BasicQueryPlanner();
      Plan p1 = bqp.createPlan(data.firstQ(), tx);
      Plan p2 = bqp.createPlan(data.secondQ(), tx);
      if (data.type() == 1)
         return new UnionPlan(p1, p2);
      else
         return new IntersectPlan(p1, p2);
   }
}
