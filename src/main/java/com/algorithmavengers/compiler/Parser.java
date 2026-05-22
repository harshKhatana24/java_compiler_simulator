package com.algorithmavengers.compiler;

import java.util.*;

public class Parser {
    private List<Token_.Token> tokens;
    private int currentIndex = 0;
    private Stack<String> stack = new Stack<>();

    private static final Map<String, Map<String, String[]>> parsingTable = new HashMap<>();

    static {
        // Program rules
        Map<String, String[]> progRow = new HashMap<>();
        progRow.put("id", new String[]{"Stmt", "Prog'"});
        parsingTable.put("Prog", progRow);

        Map<String, String[]> progPrimeRow = new HashMap<>();
        progPrimeRow.put("id", new String[]{"Stmt", "Prog'"});
        progPrimeRow.put("$", new String[]{"epsilon"});
        parsingTable.put("Prog'", progPrimeRow);

        // Statement: id = Expr ;
        Map<String, String[]> stmtRow = new HashMap<>();
        stmtRow.put("id", new String[]{"id", "=", "Expr", ";"});
        parsingTable.put("Stmt", stmtRow);

        // Expression
        Map<String, String[]> exprRow = new HashMap<>();
        exprRow.put("id", new String[]{"Term", "Expr'"});
        exprRow.put("num", new String[]{"Term", "Expr'"});
        exprRow.put("(", new String[]{"Term", "Expr'"});
        parsingTable.put("Expr", exprRow);

        Map<String, String[]> exprPrimeRow = new HashMap<>();
        exprPrimeRow.put("+", new String[]{"+", "Term", "Expr'"});
        exprPrimeRow.put("-", new String[]{"-", "Term", "Expr'"});
        exprPrimeRow.put(")", new String[]{"epsilon"});
        exprPrimeRow.put(";", new String[]{"epsilon"});
        parsingTable.put("Expr'", exprPrimeRow);

        // Term
        Map<String, String[]> termRow = new HashMap<>();
        termRow.put("id", new String[]{"Factor", "Term'"});
        termRow.put("num", new String[]{"Factor", "Term'"});
        termRow.put("(", new String[]{"Factor", "Term'"});
        parsingTable.put("Term", termRow);

        Map<String, String[]> termPrimeRow = new HashMap<>();
        termPrimeRow.put("+", new String[]{"epsilon"});
        termPrimeRow.put("*", new String[]{"*", "Factor", "Term'"});
        termPrimeRow.put("/", new String[]{"/", "Factor", "Term'"});
        termPrimeRow.put(")", new String[]{"epsilon"});
        termPrimeRow.put(";", new String[]{"epsilon"});
        parsingTable.put("Term'", termPrimeRow);

        // Factor
        Map<String, String[]> factorRow = new HashMap<>();
        factorRow.put("id", new String[]{"id"});
        factorRow.put("num", new String[]{"num"});
        factorRow.put("(", new String[]{"(", "Expr", ")"});
        parsingTable.put("Factor", factorRow);
    }

    public Parser(List<Token_.Token> tokens) {
        this.tokens = tokens;
    }

    private String getGrammarTerminal(Token_.Token t) {
        if (t.type.equals("IDENTIFIER")) return "id";
        if (t.type.contains("number")) return "num";
        return t.value;
    }

    public void parse() {

        stack.clear();
        currentIndex = 0;

        stack.push("$");
        stack.push("Prog");

        System.out.printf("%-40s %-40s%n", "Stack", "Input");
        System.out.println("-".repeat(80));

        while (!stack.isEmpty()) {

            String X = stack.peek();

            Token_.Token currentToken = (currentIndex < tokens.size())
                    ? tokens.get(currentIndex)
                    : new Token_.Token("EOF", "$", -1, -1);

            String a = getGrammarTerminal(currentToken);

            // 🚨 STRICT CHECK: every statement must start with identifier
            if (X.equals("Stmt")) {
                if (!currentToken.type.equals("IDENTIFIER")) {
                    System.err.println("\n[SYNTAX ERROR] Invalid statement. Expected identifier before '=' but found '"
                            + currentToken.value + "' at line "
                            + currentToken.line + ", col " + currentToken.column);
                    return;
                }
            }

            printStep();

            // ✅ TERMINAL MATCHING
            if (isTerminal(X) || X.equals("$")) {

                if (X.equals(a)) {
                    stack.pop();
                    currentIndex++;
                } else {
                    System.err.println("\n[SYNTAX ERROR] Expected '" + X
                            + "' but found '" + currentToken.value
                            + "' at line " + currentToken.line
                            + ", col " + currentToken.column);
                    return;
                }

            } else {

                // ✅ NON-TERMINAL EXPANSION
                if (parsingTable.containsKey(X) && parsingTable.get(X).containsKey(a)) {

                    stack.pop();
                    String[] production = parsingTable.get(X).get(a);

                    if (!production[0].equals("epsilon")) {
                        for (int i = production.length - 1; i >= 0; i--) {
                            stack.push(production[i]);
                        }
                    }

                } else {
                    System.err.println("\n[SYNTAX ERROR] Unexpected token '" + currentToken.value
                            + "' at line " + currentToken.line
                            + ", col " + currentToken.column);
                    return;
                }
            }
        }

        // 🚨 FINAL CHECK (THIS FIXES YOUR BUG)
        if (currentIndex != tokens.size()) {
            Token_.Token t = tokens.get(currentIndex);
            System.err.println("\n[SYNTAX ERROR] Unexpected token '" + t.value
                    + "' at line " + t.line + ", col " + t.column);
            return;
        }

        System.out.println("\n[SUCCESS] All statements parsed correctly.");
    }

    private boolean isTerminal(String s) {
        return !parsingTable.containsKey(s) && !s.equals("epsilon");
    }

    private void printStep() {
        StringBuilder inputStr = new StringBuilder();
        int limit = Math.min(currentIndex + 5, tokens.size());
        for (int i = currentIndex; i < limit; i++) {
            inputStr.append(tokens.get(i).value).append(" ");
        }
        if (tokens.size() > limit) inputStr.append("...");
        System.out.printf("%-40s %-40s%n", stack.toString(), inputStr.toString());
    }
}