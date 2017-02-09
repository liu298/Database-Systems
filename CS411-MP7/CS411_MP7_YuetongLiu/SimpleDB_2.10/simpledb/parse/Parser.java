package simpledb.parse;

import java.util.*;
import simpledb.query.*;
import simpledb.record.Schema;
import simpledb.materialize.*;

/**
 * The SimpleDB parser.
 * @author Edward Sciore
 */
public class Parser {
   private Lexer lex;
   
   public Parser(String s) {
      lex = new Lexer(s);
   }
   
// Methods for parsing predicates, terms, expressions, constants, and fields
   
   public String field() {
      return lex.eatId();
   }
   
   public Constant constant() {
      if (lex.matchStringConstant())
         return new StringConstant(lex.eatStringConstant());
      else
         return new IntConstant(lex.eatIntConstant());
   }
   
   public Expression expression() {
      if (lex.matchId())
         return new FieldNameExpression(field());
      else
         return new ConstantExpression(constant());
   }
   
   // This function is revised
   public Object term() {
      Expression lhs = expression();
      CompOperator Oper = null;
      if (lex.matchKeyword("in")) {
         ArrayList<Term> terms = new ArrayList<Term>();
         lex.eatKeyword("in");
         lex.eatDelim('(');
         Oper = new CompOperator("=");
         Expression setElem = expression();
         terms.add(new Term(lhs, setElem, Oper));
         while (lex.matchDelim(',')) {
            lex.eatDelim(',');
            setElem = expression();
            terms.add(new Term(lhs, setElem, Oper));
         }
         lex.eatDelim(')');
         return terms;
      }
      if (lex.matchDelim('=')){
        Oper = new CompOperator("=");
        lex.eatDelim('=');
      }
      else if (lex.matchDelim('<')){
        lex.eatDelim('<');
        if (lex.matchDelim('=')){
            Oper = new CompOperator("<=");
            lex.eatDelim('=');
        }
        else if (lex.matchDelim('>')){
            Oper = new CompOperator("<>");
            lex.eatDelim('>');
        }
        else
            Oper = new CompOperator("<");
      }
      else {
        lex.eatDelim('>');
        if (lex.matchDelim('=')){
            Oper = new CompOperator(">=");
            lex.eatDelim('=');
        }
        else
            Oper = new CompOperator(">");
      }
      Expression rhs = expression();
      return new Term(lhs, rhs, Oper);
   }
   
   public Predicate predicate() {
      Predicate pred = new Predicate();
      ArrayList<Integer> leftBrac = new ArrayList<Integer>();
      ArrayList<Integer> rightBrac = new ArrayList<Integer>();
      ArrayList<Integer> logicalOper = new ArrayList<Integer>(); // 1:and, 2:or
      
      Integer lb = 0;
      Integer rb = 0;
      while (lex.matchDelim('(')) {
         lex.eatDelim('(');
         lb += 1;
      }
      leftBrac.add(lb);
      Object t = term();
      if (t instanceof Term) {
         Term tm = (Term)t;
         pred.conjoinWith(new Predicate(tm));
         rightBrac.add(new Integer(0));
      } else {
         ArrayList tms = (ArrayList)t;
         leftBrac.set(leftBrac.size()-1,leftBrac.get(leftBrac.size()-1)+1);
         pred.conjoinWith(new Predicate((Term)tms.remove(0)));
         rightBrac.add(new Integer(0));
         for (Object tm : tms) {
            logicalOper.add(new Integer(2));
            leftBrac.add(new Integer(0));
            pred.conjoinWith(new Predicate((Term)tm));
            rightBrac.add(new Integer(0));
         }
         rightBrac.set(rightBrac.size()-1, 1);
      }
      while (lex.matchDelim(')')) {
         lex.eatDelim(')');
         rb += 1;
      }
      rightBrac.set(rightBrac.size()-1,rightBrac.get(rightBrac.size()-1)+rb);
            
      while (lex.matchKeyword("and") || lex.matchKeyword("or")){
         if (lex.matchKeyword("and")){
            logicalOper.add(new Integer(1));
            lex.eatKeyword("and");
         }
         else {
            logicalOper.add(new Integer(2));
            lex.eatKeyword("or");
         }
         lb = new Integer(0);
         rb = new Integer(0);
         while (lex.matchDelim('(')) {
            lex.eatDelim('(');
            lb += 1;
         }
         leftBrac.add(lb);

         t = term();
         if (t instanceof Term) {
            Term tm = (Term)t;
            pred.conjoinWith(new Predicate(tm));
            rightBrac.add(new Integer(0));
         } else {
            ArrayList tms = (ArrayList)t;
            leftBrac.set(leftBrac.size()-1,leftBrac.get(leftBrac.size()-1)+1);
            pred.conjoinWith(new Predicate((Term)tms.remove(0)));
            rightBrac.add(new Integer(0));
            for (Object tm : tms) {
               logicalOper.add(new Integer(2));
               leftBrac.add(new Integer(0));
               pred.conjoinWith(new Predicate((Term)tm));
               rightBrac.add(new Integer(0));
            }
            rightBrac.set(rightBrac.size()-1, 1);
         }         
                  
         while (lex.matchDelim(')')) {
            lex.eatDelim(')');
            rb += 1;
         }
         rightBrac.set(rightBrac.size()-1,rightBrac.get(rightBrac.size()-1)+rb);
      }
      
      boolean hasbrac = false;
      for (Integer k : leftBrac) {
         if (k > 0) {
            hasbrac = true;
            break;
         }
      }
      boolean hasor = false;
      for (Integer k : logicalOper) {
         if (k == 2) {
            hasor = true;
            break;
         }
      }

      if (hasbrac || hasor){
         pred.setComp(leftBrac,rightBrac,logicalOper);
      }
      return pred;
   }
   
// Methods for parsing queries
   
   public QueryData query() {
      lex.eatKeyword("select");
      Collection<String> fields = new ArrayList<String>();
      if (lex.matchDelim('*'))
         lex.eatDelim('*');
      else
         fields = selectList();
      lex.eatKeyword("from");
      Collection<String> tables = tableList();
      Predicate pred = new Predicate();
      if (lex.matchKeyword("where")) {
         lex.eatKeyword("where");
         pred = predicate();
      }
      // for group by
      Collection<String> groupFields = new ArrayList<String>();
      if (lex.matchKeyword("group")){
         lex.eatKeyword("group");
         lex.eatKeyword("by");
         groupFields = groupList();
      }
      // for order by 
      Collection<String> sortFields = new ArrayList<String>();
      if(lex.matchKeyword("order")){
         lex.eatKeyword("order");
         lex.eatKeyword("by");
         sortFields = sortList();
      }
      // for limit
      int limit = -1;
      if (lex.matchKeyword("limit")) {
         lex.eatKeyword("limit");
         limit = lex.eatIntConstant();
      }      
      // for ordinary query
      return new QueryData(fields,tables,pred,limit,groupFields,sortFields);
   }

   private Collection<String> groupList() {
      Collection<String> L = new ArrayList<String>();
      L.add(field());
      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(groupList());
      }
      return L;
   }
   
   public UIData unionIntersect() {
      lex.eatKeyword("select");
      Collection<String> fields1 = new ArrayList<String>();
      if (lex.matchDelim('*'))
         lex.eatDelim('*');
      else
         fields1 = selectList();
      lex.eatKeyword("from");
      Collection<String> tables1 = tableList();
      Predicate pred1 = new Predicate();
      if (lex.matchKeyword("where")) {
         lex.eatKeyword("where");
         pred1 = predicate();
      }
      QueryData q1 = new QueryData(fields1, tables1, pred1);
      int type = 1;
      if (lex.matchKeyword("union"))
         lex.eatKeyword("union");
      else {
         type = 2;
         lex.eatKeyword("intersect");
      } 
      lex.eatKeyword("select");
      Collection<String> fields2 = new ArrayList<String>();
      if (lex.matchDelim('*'))
         lex.eatDelim('*');
      else
         fields2 = selectList();
      lex.eatKeyword("from");
      Collection<String> tables2 = tableList();
      Predicate pred2 = new Predicate();
      if (lex.matchKeyword("where")) {
         lex.eatKeyword("where");
         pred2 = predicate();
      }
      QueryData q2 = new QueryData(fields2, tables2, pred2);
      return new UIData(q1, q2, type);
   }
   
   private Collection<String> selectList() {
      Collection<String> L = new ArrayList<String>();
      if (lex.matchKeyword("count")){
         lex.eatKeyword("count");
         lex.eatDelim('(');
         L.add("countof"+field());
         lex.eatDelim(')');
      }
      else if(lex.matchKeyword("max")){
         lex.eatKeyword("max");
         lex.eatDelim('(');
         L.add("maxof"+field());
         lex.eatDelim(')');
      }
      else if(lex.matchKeyword("min")){
         lex.eatKeyword("min");
         lex.eatDelim('(');
         L.add("minof"+field());
         lex.eatDelim(')');
      }
      else if(lex.matchKeyword("sum")){
         lex.eatKeyword("sum");
         lex.eatDelim('(');
         L.add("sumof"+field());
         lex.eatDelim(')');
      }
      else if(lex.matchKeyword("avg")){
         lex.eatKeyword("avg");
         lex.eatDelim('(');
         L.add("avgof"+field());
         lex.eatDelim(')');
      }      
      else if (lex.matchId())
         L.add(field());

      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(selectList());
      }
      return L;
   }
   
   private Collection<String> sortList() {
      Collection<String> L = new ArrayList<String>();
      L.add(field());
       if (lex.matchKeyword("desc")) {
           lex.eatKeyword("desc");
           L.add("desc");
           if (lex.matchDelim(',')) {
               lex.eatDelim(',');
               L.addAll(sortList());
           }
       }
      else if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(sortList());
      }
      return L;
   }   
   
   private Collection<String> tableList() {
      Collection<String> L = new ArrayList<String>();
      L.add(lex.eatId());
      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(tableList());
      }
      return L;
   }
   
// Methods for parsing the various update commands
   
   public Object updateCmd() {
      if (lex.matchKeyword("insert"))
         return insert();
      else if (lex.matchKeyword("delete"))
         return delete();
      else if (lex.matchKeyword("update"))
         return modify();
      else
         return create();
   }
   
   private Object create() {
      lex.eatKeyword("create");
      if (lex.matchKeyword("table"))
         return createTable();
      else if (lex.matchKeyword("view"))
         return createView();
      else
         return createIndex();
   }
   
// Method for parsing delete commands
   
   public DeleteData delete() {
      lex.eatKeyword("delete");
      lex.eatKeyword("from");
      String tblname = lex.eatId();
      Predicate pred = new Predicate();
      if (lex.matchKeyword("where")) {
         lex.eatKeyword("where");
         pred = predicate();
      }
      return new DeleteData(tblname, pred);
   }
   
// Methods for parsing insert commands
   
   public InsertData insert() {
      lex.eatKeyword("insert");
      lex.eatKeyword("into");
      String tblname = lex.eatId();
      lex.eatDelim('(');
      List<String> flds = fieldList();
      lex.eatDelim(')');
      lex.eatKeyword("values");
      lex.eatDelim('(');
      List<Constant> vals = constList();
      lex.eatDelim(')');
      return new InsertData(tblname, flds, vals);
   }
   
   private List<String> fieldList() {
      List<String> L = new ArrayList<String>();
      L.add(field());
      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(fieldList());
      }
      return L;
   }
   
   private List<Constant> constList() {
      List<Constant> L = new ArrayList<Constant>();
      L.add(constant());
      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         L.addAll(constList());
      }
      return L;
   }
   
// Method for parsing modify commands
   
   public ModifyData modify() {
      lex.eatKeyword("update");
      String tblname = lex.eatId();
      lex.eatKeyword("set");
      String fldname = field();
      lex.eatDelim('=');
      Expression newval = expression();
      Predicate pred = new Predicate();
      if (lex.matchKeyword("where")) {
         lex.eatKeyword("where");
         pred = predicate();
      }
      return new ModifyData(tblname, fldname, newval, pred);
   }
   
// Method for parsing create table commands
   
   public CreateTableData createTable() {
      lex.eatKeyword("table");
      String tblname = lex.eatId();
      lex.eatDelim('(');
      Schema sch = fieldDefs();
      lex.eatDelim(')');
      return new CreateTableData(tblname, sch);
   }
   
   private Schema fieldDefs() {
      Schema schema = fieldDef();
      if (lex.matchDelim(',')) {
         lex.eatDelim(',');
         Schema schema2 = fieldDefs();
         schema.addAll(schema2);
      }
      return schema;
   }
   
   private Schema fieldDef() {
      String fldname = field();
      return fieldType(fldname);
   }
   
   private Schema fieldType(String fldname) {
      Schema schema = new Schema();
      if (lex.matchKeyword("int")) {
         lex.eatKeyword("int");
         schema.addIntField(fldname);
      }
      else {
         lex.eatKeyword("varchar");
         lex.eatDelim('(');
         int strLen = lex.eatIntConstant();
         lex.eatDelim(')');
         schema.addStringField(fldname, strLen);
      }
      return schema;
   }
   
// Method for parsing create view commands
   
   public CreateViewData createView() {
      lex.eatKeyword("view");
      String viewname = lex.eatId();
      lex.eatKeyword("as");
      QueryData qd = query();
      return new CreateViewData(viewname, qd);
   }
   
   
//  Method for parsing create index commands
   
   public CreateIndexData createIndex() {
      lex.eatKeyword("index");
      String idxname = lex.eatId();
      lex.eatKeyword("on");
      String tblname = lex.eatId();
      lex.eatDelim('(');
      String fldname = field();
      lex.eatDelim(')');
      return new CreateIndexData(idxname, tblname, fldname);
   }
}

