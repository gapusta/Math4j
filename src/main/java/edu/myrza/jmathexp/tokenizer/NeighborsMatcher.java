package edu.myrza.jmathexp.tokenizer;

import edu.myrza.jmathexp.common.Informator;
import edu.myrza.jmathexp.common.Token;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;

class NeighborsMatcher{

    private LexicalAnalizer lex;
    private String exp;
    private List<Token> output;
    private List<Token> current;
    private List<Token> next;
    private Token lastProcessed;
    private static Map<Token.Type,List<Token.Type>> rightNeighboursAllowedTypes = new HashMap<>();

    public NeighborsMatcher(String exp,Informator informator,Set<String> variables){

        this.lex = new LexicalAnalizer(exp,variables,informator);
        this.exp = exp;
        this.lastProcessed = new Token(Token.Type.START,"[");
        output = new ArrayList<>();
    }

    static {

        List<Token.Type> allowTypesSetOne = asList(Token.Type.OPERAND, Token.Type.VARIABLE, Token.Type.LS_UNARY_OPERATOR, Token.Type.FUNCTION, Token.Type.OPEN_PARENTHESES);
        List<Token.Type> allowTypesSetTwo = asList(Token.Type.RS_UNARY_OPERATOR, Token.Type.BINARY_OPERATOR, Token.Type.CLOSE_PARENTHESES, Token.Type.FUNCTION_ARG_SEPARATOR, Token.Type.END);

        rightNeighboursAllowedTypes.put(Token.Type.START,                  allowTypesSetOne);
        rightNeighboursAllowedTypes.put(Token.Type.LS_UNARY_OPERATOR,      allowTypesSetOne);
        rightNeighboursAllowedTypes.put(Token.Type.BINARY_OPERATOR,        allowTypesSetOne);
        rightNeighboursAllowedTypes.put(Token.Type.OPEN_PARENTHESES,       allowTypesSetOne);
        rightNeighboursAllowedTypes.put(Token.Type.FUNCTION_ARG_SEPARATOR, allowTypesSetOne);

        rightNeighboursAllowedTypes.put(Token.Type.OPERAND,                allowTypesSetTwo);
        rightNeighboursAllowedTypes.put(Token.Type.VARIABLE,               allowTypesSetTwo);
        rightNeighboursAllowedTypes.put(Token.Type.RS_UNARY_OPERATOR,      allowTypesSetTwo);
        rightNeighboursAllowedTypes.put(Token.Type.CLOSE_PARENTHESES,      allowTypesSetTwo);

        rightNeighboursAllowedTypes.put(Token.Type.FUNCTION,               asList(Token.Type.OPEN_PARENTHESES));

    }

    public boolean hasNext(){ return lex.hasNext(); }

    public Token next(){

        //todo get rid of this if by making lexeme analizer return both START and END tokens
        if(lastProcessed.type == Token.Type.START)
            current = lex.next();
        next = lex.next();

        Token result = findSuitableCurrent(lastProcessed,current,next);

        output.add(result);
        lastProcessed = result;
        current = next;
        return result;
    }

    Token findSuitableCurrent(Token lastProcessed,
                              List<Token> currents,
                              List<Token> currentRightNeighborCandidates)
    {

        List<Token> currentCandidates = findSuitableNeighbors(lastProcessed,currents);

        if(currentCandidates.size() <= 0)
            handleNoSuitableNeighbors(lastProcessed,currentRightNeighborCandidates.get(0));
        else
            currentCandidates.sort(comparingInt(t -> t.type.id()));

        for(Token t : currentCandidates)
            if(findSuitableNeighbors(t,currentRightNeighborCandidates).size() > 0)
                return t;

        handleNoSuitableNeighbors(currentCandidates.get(0),currentRightNeighborCandidates.get(0));
        return null;
    }


    List<Token> findSuitableNeighbors(Token token,List<Token> candidates){

        //gets allowed types for t's right neighbor
        List<Token.Type> allowedTypes = rightNeighboursAllowedTypes.get(token.type);

        return candidates.stream()
                         .filter(t -> allowedTypes.contains(t.type))
                         .collect(toList());
    }



    private void handleNoSuitableNeighbors(Token current , Token badNeighbor){

        if(current.type == Token.Type.START)
            throw new RuntimeException("Token [" + badNeighbor.lexeme + "] cannot be at the start....");

        if(badNeighbor.type == Token.Type.END)
            throw new RuntimeException("The expression [" + exp + "] is unfinished...");

        String tokenStr = output.stream().map(t -> "[" + t.lexeme + "]").collect(joining());
        int errorOccurencePosition = tokenStr.length() - output.size()*2;

        String message = "At position " + errorOccurencePosition + "\n" +
                         "These two tokens : [" + current.lexeme + "][" + badNeighbor.lexeme + "] cannot be neighbors....\n" +
                         "prev. tokens : " + tokenStr;

        throw new RuntimeException(message);

    }


}