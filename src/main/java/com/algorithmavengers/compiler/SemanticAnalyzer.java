package com.algorithmavengers.compiler;

// SemanticAnalyzer.java
import java.util.*;

public class SemanticAnalyzer {

    // Symbol table: variable name -> type (we'll use "int"/"float" inferred from assignment)
    private Map<String, String> symbolTable = new LinkedHashMap<>();
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    // Runs semantic checks on token list
    // Our grammar: id = Expr ; (from Parser)
    // We check: undeclared use, type consistency
//    public boolean analyze(List<Token_.Token> tokens) {
//
//
//
//        // In your analyze() method, inside Pass 1, ADD this block:
//
//// Handle declarations like: int a; float b;
//
//        // Pass 1: collect all LHS assignments (declarations)
//        String[] typeKeywords = {"int", "float", "string", "bool", "void"};
//        for (int i = 0; i < tokens.size() - 1; i++) {
//            Token_.Token t = tokens.get(i);
//            Token_.Token next = tokens.get(i + 1);
//
//            boolean isTypeKeyword = false;
//            for (String tk : typeKeywords) {
//                if (t.value.equals(tk) && t.type.equals("KEYWORD")) {
//                    isTypeKeyword = true;
//                    break;
//                }
//            }
//
//            // int a;  ->  KEYWORD IDENTIFIER DELIMITER
//            if (isTypeKeyword && next.type.equals("IDENTIFIER")) {
//                symbolTable.put(next.value, t.value); // register 'a' as type 'int'
//            }
//        }
//
//
//
//
//
//
//
//        // Pass 1: collect all LHS assignments (declarations)
////        for (int i = 0; i < tokens.size() - 2; i++) {
////            Token_.Token t = tokens.get(i);
////            Token_.Token next = tokens.get(i + 1);
////            if (t.type.equals("IDENTIFIER") && next.type.equals("OPERATOR") && next.value.equals("=")) {
////                String varName = t.value;
////                // Infer type from RHS
////                String type = inferType(tokens, i + 2);
////                symbolTable.put(varName, type);
////            }
////        }
////
//
//        // Pass 2: check all identifiers on RHS are declared
//        for (int i = 0; i < tokens.size(); i++) {
//            Token_.Token t = tokens.get(i);
//            // Skip if it's an LHS assignment target
//            if (t.type.equals("IDENTIFIER")) {
//                boolean isLhs = (i + 1 < tokens.size()
//                        && tokens.get(i+1).type.equals("OPERATOR")
//                        && tokens.get(i+1).value.equals("="));
//                if (!isLhs) {
//                    if (!symbolTable.containsKey(t.value)) {
//                        errors.add("[SEMANTIC ERROR] Undeclared variable '" + t.value
//                                + "' used at line " + t.line + ", col " + t.column);
//                    }
//                }
//            }
//
//            // Division by zero check
//            if (t.type.equals("OPERATOR") && t.value.equals("/")) {
//                if (i + 1 < tokens.size()) {
//                    Token_.Token next = tokens.get(i + 1);
//                    if ((next.type.equals("number") || next.type.equals("decimal"))
//                            && next.value.equals("0")) {
//                        errors.add("[SEMANTIC ERROR] Division by zero at line "
//                                + t.line + ", col " + t.column);
//                    }
//                }
//            }
//        }
//
//        // Print results
//        System.out.println("\n--- Semantic Analysis ---");
//        System.out.println("Symbol Table:");
//        symbolTable.forEach((k, v) -> System.out.println("  " + k + " : " + v));
//
//        if (errors.isEmpty() && warnings.isEmpty()) {
//            System.out.println("[OK] No semantic errors found.");
//        }
//        warnings.forEach(System.out::println);
//        errors.forEach(System.err::println);
//
//        return errors.isEmpty();
//    }










    // At top of class
    private boolean strictMode = false; // set true for Java-style enforcement

    public void setStrictMode(boolean strict) {
        this.strictMode = strict;
    }

    private Set<String> declaredWithType = new HashSet<>();








    public boolean analyze(List<Token_.Token> tokens) {

        // All Java + your custom language keywords to ignore
        Set<String> skipWords = new HashSet<>(Arrays.asList(
                "import", "package", "class", "interface", "enum",
                "public", "private", "protected", "static", "final",
                "void", "new", "this", "super", "return",
                "if", "else", "while", "for", "do", "switch", "case",
                "break", "continue", "try", "catch", "finally", "throw", "throws",
                "int", "float", "double", "boolean", "String", "char",
                "long", "byte", "short", "true", "false", "null",
                "System", "out", "in", "err", "println", "print", "printf",
                "Scanner", "ArrayList", "List", "Map", "HashMap",
                "Arrays", "Math", "Object", "extends", "implements",
                "instanceof", "abstract", "synchronized", "volatile",
                "fn", "let", "const", "struct", "match",
                "java", "util", "lang", "io", "add", "get", "size",
                "Integer", "Double", "Boolean", "Float", "Long"
        ));

        // Pass 1
        for (int i = 0; i < tokens.size() - 2; i++) {
            Token_.Token t = tokens.get(i);
            Token_.Token next = tokens.get(i + 1);

            // int a = ... or int a;
            if (t.type.equals("KEYWORD") && next.type.equals("IDENTIFIER")) {
                symbolTable.put(next.value, t.value);
                declaredWithType.add(next.value); // properly declared with type
            }

            // plain assignment: a = ... (only in lenient mode)
            if (t.type.equals("IDENTIFIER") && next.value.equals("=")) {
                if (!declaredWithType.contains(t.value)) {
                    symbolTable.put(t.value, inferType(tokens, i + 2));
                    // NOT added to declaredWithType — no type keyword seen
                }
            }
        }

        // Pass 2: check undeclared identifiers




        // Pass 2: check undeclared
        for (int i = 0; i < tokens.size(); i++) {
            Token_.Token t = tokens.get(i);

            if (!t.type.equals("IDENTIFIER")) continue;
            if (skipWords.contains(t.value)) continue;
            if (i > 0 && tokens.get(i-1).value.equals(".")) continue;
            if (i+1 < tokens.size() && tokens.get(i+1).value.equals(".")) continue;
            if (i+1 < tokens.size() && tokens.get(i+1).value.equals("(")) continue;
            if (i > 0 && tokens.get(i-1).type.equals("KEYWORD")) continue;

            boolean isLhs = i+1 < tokens.size() && tokens.get(i+1).value.equals("=");

            if (strictMode) {
                // STRICT: variable must be declared with a type before any use
                // declared means it came from "int a" or "int a = ..." pattern
                if (!declaredWithType.contains(t.value)) {
                    if (isLhs) {
                        errors.add("[SEMANTIC ERROR] Variable '" + t.value
                                + "' used without type declaration at line "
                                + t.line + ", col " + t.column
                                + "  (did you mean: int " + t.value + " = ...?)");
                    } else if (!symbolTable.containsKey(t.value)) {
                        errors.add("[SEMANTIC ERROR] Undeclared variable '" + t.value
                                + "' at line " + t.line + ", col " + t.column);
                    }
                }












            } else {
                // LENIENT: assignment counts as declaration
                if (!isLhs && !symbolTable.containsKey(t.value)) {
                    errors.add("[SEMANTIC ERROR] Undeclared variable '" + t.value
                            + "' used at line " + t.line + ", col " + t.column);
                }
            }
        }






        // Division by zero check
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (tokens.get(i).value.equals("/")) {
                Token_.Token next = tokens.get(i + 1);
                if (next.value.equals("0")) {
                    errors.add("[SEMANTIC ERROR] Division by zero at line "
                            + tokens.get(i).line + ", col " + tokens.get(i).column);
                }
            }
        }

        System.out.println("\n--- Semantic Analysis ---");
        System.out.println("Symbol Table:");
        symbolTable.forEach((k, v) -> System.out.println("  " + k + " : " + v));
        if (errors.isEmpty()) System.out.println("[OK] No semantic errors.");
        else errors.forEach(System.err::println);






        // Check for empty assignment: int a =; or a =;
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token_.Token t = tokens.get(i);
            Token_.Token next = tokens.get(i + 1);

            if (t.value.equals("=") && next.value.equals(";")) {
                errors.add("[SYNTAX ERROR] Empty expression after '=' at line "
                        + t.line + ", col " + t.column
                        + " — value is missing.");
            }
        }





        // Check assignment has closing semicolon
        for (int i = 0; i < tokens.size(); i++) {
            Token_.Token t = tokens.get(i);
            // if we see = followed by value but no ; before next statement
            if (t.value.equals("=")) {
                boolean foundSemicolon = false;
                for (int j = i + 1; j < tokens.size(); j++) {
                    if (tokens.get(j).value.equals(";")) { foundSemicolon = true; break; }
                    if (tokens.get(j).value.equals("=")) break; // hit next assignment
                }
                if (!foundSemicolon) {
                    errors.add("[SYNTAX ERROR] Missing ';' after assignment at line "
                            + t.line + ", col " + t.column);
                }
            }
        }








        return errors.isEmpty();
    }


    // Simple type inference: if any float/number with '.' in rhs -> float, else int
    private String inferType(List<Token_.Token> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            Token_.Token t = tokens.get(i);
            if (t.type.equals("DELIMITER") && t.value.equals(";")) break;
            if (t.type.equals("number") && t.value.contains(".")) return "float";
        }
        return "int";
    }

    public Map<String, String> getSymbolTable() { return symbolTable; }
    public List<String> getErrors() { return errors; }
}