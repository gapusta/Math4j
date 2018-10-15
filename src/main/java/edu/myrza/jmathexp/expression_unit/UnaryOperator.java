package edu.myrza.jmathexp.expression_unit;

public class UnaryOperator implements ExpressionUnit{

    private UnaryAction impl;
    private int precedence;
    private String name;

    private static ExpUnitType type;
    private static boolean isLeftAssociative;
    private static int argc;

    static {
        type = ExpUnitType.UNARY_OPERATOR;
        argc = 1;
        isLeftAssociative = true;
    }

    public UnaryOperator(String name, int precedence, UnaryAction impl){

        this.impl = impl;
        this.name = name;
        this.precedence = precedence;

    }

    public double evaluate(double... args) {

        if(args == null) throw new IllegalArgumentException("varargs can't be null : " + toString());
        if(args.length != getArgc()) throw new IllegalArgumentException("unary operator can't have more or less than one arg : " + toString());

        return impl.execute(args[0]);

    }

    public int getArgc(){
        return argc;
    }

    public boolean isLeftAssociative(){
        return isLeftAssociative;
    }

    public int getPrecedence(){
        return precedence;
    }

    public ExpUnitType getType(){
        return type;
    }

    @Override
    public String toString() {
        return "UnaryOperator{" +
                "name='" + name + '\'' +
                '}';
    }
}