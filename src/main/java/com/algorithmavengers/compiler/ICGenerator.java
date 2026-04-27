package com.algorithmavengers.compiler;

// ICGenerator.java
import java.util.*;

public class ICGenerator {

    private int tempCount = 0;
    private List<String> instructions = new ArrayList<>();
    private List<Token_.Token> tokens;
    private int pos = 0;

    public ICGenerator(List<Token_.Token> tokens) {
        this.tokens = tokens;
    }

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private Token_.Token current() {
        return pos < tokens.size() ? tokens.get(pos) : new Token_.Token("EOF", "$", -1, -1);
    }

    private Token_.Token consume() {
        return tokens.get(pos++);
    }

    private boolean match(String value) {
        if (pos < tokens.size() && tokens.get(pos).value.equals(value)) {
            pos++;
            return true;
        }
        return false;
    }

    // Generate TAC for all statements
    public List<String> generate() {
        while (pos < tokens.size() && !current().value.equals("$")) {
            generateStatement();
        }
        System.out.println("\n--- Intermediate Code (Three-Address Code) ---");
        instructions.forEach(System.out::println);
        return instructions;
    }

    // Stmt -> id = Expr ;
    private void generateStatement() {
        if (current().type.equals("IDENTIFIER")) {
            String lhs = consume().value;  // id
            if (!match("=")) { pos++; return; } // skip malformed
            String rhs = generateExpr();
            match(";");
            instructions.add(lhs + " = " + rhs);
        } else {
            pos++; // skip unknown tokens
        }
    }

    // Expr -> Term Expr'
    private String generateExpr() {
        String left = generateTerm();
        return generateExprPrime(left);
    }

    // Expr' -> + Term Expr' | epsilon
    private String generateExprPrime(String left) {
        if (pos < tokens.size() && current().value.equals("+")) {
            consume(); // eat +
            String right = generateTerm();
            String temp = newTemp();
            instructions.add(temp + " = " + left + " + " + right);
            return generateExprPrime(temp);
        }
        if (pos < tokens.size() && current().value.equals("-")) {
            consume();
            String right = generateTerm();
            String temp = newTemp();
            instructions.add(temp + " = " + left + " - " + right);
            return generateExprPrime(temp);
        }
        return left;
    }

    // Term -> Factor Term'
    private String generateTerm() {
        String left = generateFactor();
        return generateTermPrime(left);
    }

    // Term' -> * Factor Term' | / Factor Term' | epsilon
    private String generateTermPrime(String left) {
        if (pos < tokens.size() && current().value.equals("*")) {
            consume();
            String right = generateFactor();
            String temp = newTemp();
            instructions.add(temp + " = " + left + " * " + right);
            return generateTermPrime(temp);
        }
        if (pos < tokens.size() && current().value.equals("/")) {
            consume();
            String right = generateFactor();
            String temp = newTemp();
            instructions.add(temp + " = " + left + " / " + right);
            return generateTermPrime(temp);
        }
        return left;
    }

    // Factor -> id | num | ( Expr )
    private String generateFactor() {
        Token_.Token t = current();
        if (t.type.equals("IDENTIFIER") || t.type.contains("number")) {
            consume();
            return t.value;
        }
        if (t.value.equals("(")) {
            consume(); // eat (
            String result = generateExpr();
            match(")");
            return result;
        }
        consume(); // skip unexpected
        return "?";
    }

    public List<String> getInstructions() { return instructions; }
}