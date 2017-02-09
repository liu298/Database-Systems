package simpledb.query;

/* CompOperator is the operator used to compare two expressions
 * such as =, > , <, >=, <=, <>
 */ 

public class CompOperator {
    private String Oper;
    
    public CompOperator(String Oper) {
        this.Oper = Oper;
    }
    
    public boolean isTrue(Constant lhsval, Constant rhsval) {
        int comp = lhsval.compareTo(rhsval);
        if (Oper == "=")
            return comp == 0;
        else if (Oper == ">")
            return comp > 0;
        else if (Oper == "<")
            return comp < 0;
        else if (Oper == ">=")
            return comp >= 0;
        else if (Oper == "<=")
            return comp <= 0;
        else if (Oper == "<>")
            return comp != 0;
        else
            System.out.println("Incorrect Operator!");
            return false;
    }
}
