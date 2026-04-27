package com.algorithmavengers.compiler;

// Main.java
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String code = "a = 3 + 4; b = a * 2; c = b + 1;";

        System.out.println("Source: " + code);

        // Phase 1: Lexical Analysis
        Token_ lexer = new Token_();
        List<Token_.Token> tokens = lexer.tokenize(code);
        System.out.println("\n--- Lexical Analysis Done ---");

        // Phase 2: Syntax Analysis
        Parser parser = new Parser(tokens);
        parser.parse();

        // Phase 3: Semantic Analysis
        SemanticAnalyzer semantic = new SemanticAnalyzer();
        boolean ok = semantic.analyze(tokens);
        if (!ok) { System.err.println("Stopping due to semantic errors."); return; }

        // Phase 4: Intermediate Code Generation
        ICGenerator icg = new ICGenerator(tokens);
        List<String> tac = icg.generate();

        // Phase 5: Optimization
        Optimizer opt = new Optimizer();
        List<String> optimized = opt.optimize(tac);

        // Phase 6: Code Generation
        CodeGenerator cg = new CodeGenerator();
        List<String> asm = cg.generate(optimized);
    }
}








































//import javax.swing.*;
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Scanner;
//
//public class Main {
//
//
//    public static void main(String[] args) {
//
//
//
//
//
//
//
//        Token_ lexer = new Token_();
//
////        String code = "int a = 2;";
////
////        // Get tokens - lex phase list return
////        List<Token_.Token> tokens = lexer.tokenize(code);
////
////        System.out.println("\n--- Lexical Analysis Completed ---");
////
////        Token_ lexer = new Token_();
//        // Sample input from Task 9 [cite: 148]
//        String code = "a = + b;";
//
//        List<Token_.Token> tokenList = lexer.tokenize(code);
//        Parser parser = new Parser(tokenList);
//        parser.parse();
//
//
//
////        Main lexer1 = new Main();
////
////        Token_ lexer = new Token_();
//
//
////        String program = """
////
////
////
////    """;
////
////
////        //jo input hai use tokens me tod dega
////        //also calculate the number of token
////        int noOfTokens=lexer.tokenize(program);
////        System.out.println(noOfTokens);
////
////
////
////
////        //input of a file
//
////        if (args.length==0){
////            System.out.println("please provide an input file");
////            return;
////        }
////
////        try{
////            //read entire file as a single string
////            String code = new String(Files.readAllBytes(Paths.get(args[0])));
////
////            Token_ lexer2=new Token_();
////            int count = lexer.tokenize(code);
////
////            System.out.println(count);
////
////        } catch (Exception e) {
////            System.out.println("Error reading file: "+e.getMessage());
////        }
//
//
////        try {
////            JFileChooser chooser = new JFileChooser();
////            int result = chooser.showOpenDialog(null);
////
////            if (result == JFileChooser.APPROVE_OPTION) {
////                File file = chooser.getSelectedFile();
////                String code = new String(Files.readAllBytes(file.toPath()));
////
////                // Token_ lexer = new Token_();
////                // int count = lexer.tokenize(code);
////
////
////                Token_ lexer = new Token_();
////
////                // Get tokens - lex phase list return
////                List<Token_.Token> tokens = lexer.tokenize(code);
////
////                System.out.println("\n--- Lexical Analysis Completed ---");
////
////                Parser parser = new Parser(tokens);
////                parser.parse();
////
////
////
////
////                // System.out.println("\nTotal Tokens: " + count);
////            } else {
////                System.out.println("No file selected.");
////            }
////
////        } catch (Exception e) {
////            System.out.println("Error: " + e.getMessage());
////        }
//
//
//
///*
//        Scanner sc = new Scanner(System.in);
//        String input = sc.nextLine();
//        Token_ lexer = new Token_();
//        int count = lexer.tokenize(input);
//
//*/
//
//    }
//}
//
//
////error -> column number and line number
