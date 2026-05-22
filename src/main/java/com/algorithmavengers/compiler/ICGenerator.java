package com.algorithmavengers.compiler;

import java.util.*;

public class ICGenerator {

    private int tempCount = 0;
    private final List<String> instructions = new ArrayList<>();

    private final List<Token_.Token> tokens;
    private int pos = 0;

    public ICGenerator(List<Token_.Token> tokens) {
        this.tokens = tokens;
    }

    // Generate temporary variables: t1, t2, ...
    private String newTemp() {
        return "t" + (++tempCount);
    }

    // Current token
    private Token_.Token current() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return new Token_.Token("EOF", "$", -1, -1);
    }

    // Consume current token
    private Token_.Token consume() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return new Token_.Token("EOF", "$", -1, -1);
    }

    // Match specific token value
    private boolean match(String value) {
        if (current().value.equals(value)) {
            pos++;
            return true;
        }
        return false;
    }

    // ==============================
    // MAIN TAC GENERATION
    // ==============================

    public List<String> generate() {

        while (pos < tokens.size()
                && !current().type.equals("EOF")
                && !current().value.equals("$")) {

            generateStatement();
        }

        System.out.println("\n--- Intermediate Code (Three-Address Code) ---");

        for (String ins : instructions) {
            System.out.println(ins);
        }

        return instructions;
    }

    // ==============================
    // Statement -> id = Expr ;
    // ==============================

    private void generateStatement() {

        if (!current().type.equals("IDENTIFIER")) {
            consume(); // skip invalid token
            return;
        }

        String lhs = consume().value;

        // Expect '='
        if (!match("=")) {
            System.err.println("ICG Error: Expected '=' after identifier");
            synchronize();
            return;
        }

        // Generate RHS expression
        String rhs = generateExpr();

        // Expect ';'
        if (!match(";")) {
            System.err.println("ICG Error: Missing ';'");
            synchronize();
            return;
        }

        instructions.add(lhs + " = " + rhs);
    }

    // ==============================
    // Expr -> Term Expr'
    // ==============================

    private String generateExpr() {

        String left = generateTerm();

        while (current().value.equals("+")
                || current().value.equals("-")) {

            String op = consume().value;

            String right = generateTerm();

            String temp = newTemp();

            instructions.add(temp + " = "
                    + left + " "
                    + op + " "
                    + right);

            left = temp;
        }

        return left;
    }

    // ==============================
    // Term -> Factor Term'
    // ==============================

    private String generateTerm() {

        String left = generateFactor();

        while (current().value.equals("*")
                || current().value.equals("/")) {

            String op = consume().value;

            String right = generateFactor();

            String temp = newTemp();

            instructions.add(temp + " = "
                    + left + " "
                    + op + " "
                    + right);

            left = temp;
        }

        return left;
    }

    // ==============================
    // Factor -> id | num | (Expr)
    // ==============================

    private String generateFactor() {

        Token_.Token t = current();

        // IDENTIFIER or NUMBER
        if (t.type.equals("IDENTIFIER")
                || t.type.toLowerCase().contains("number")) {

            consume();
            return t.value;
        }

        // Parenthesized expression
        if (t.value.equals("(")) {

            consume(); // eat '('

            String result = generateExpr();

            if (!match(")")) {
                System.err.println("ICG Error: Missing ')'");
            }

            return result;
        }

        // Unary minus support
        if (t.value.equals("-")) {

            consume();

            String value = generateFactor();

            String temp = newTemp();

            instructions.add(temp + " = -" + value);

            return temp;
        }

        // Unexpected token
        System.err.println("ICG Error: Unexpected token '" + t.value + "'");

        consume();

        return "?";
    }

    // ==============================
    // Error Recovery
    // ==============================

    private void synchronize() {

        while (pos < tokens.size()
                && !current().value.equals(";")) {
            pos++;
        }

        if (current().value.equals(";")) {
            pos++;
        }
    }

    // ==============================
    // Getter
    // ==============================

    public List<String> getInstructions() {
        return instructions;
    }
}