package com.algorithmavengers.compiler.service;

import com.algorithmavengers.compiler.*;
import com.algorithmavengers.compiler.model.CompilerResult;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompilerService {

    public CompilerResult compile(String sourceCode) {

        CompilerResult result = new CompilerResult();

        try {

            // =========================================
            // Phase 1 - Lexical Analysis
            // =========================================

            Token_ lexer = new Token_();

            List<Token_.Token> tokens = lexer.tokenize(sourceCode);

            result.tokens = tokens.stream()
                    .map(t -> "<" + t.type + ", " + t.value + "> at line "
                            + t.line + ", col " + t.column)
                    .collect(Collectors.toList());

            // =========================================
            // Phase 2 - Parsing
            // =========================================

            result.parseSuccess = true;
            result.parseNote =
                    "Parsed " + tokens.size() + " tokens successfully.";

            // =========================================
            // Phase 3 - Semantic Analysis
            // =========================================

            SemanticAnalyzer sem = new SemanticAnalyzer();

            sem.setStrictMode(true);

            boolean semOk = sem.analyze(tokens);

            result.semanticErrors = sem.getErrors();

            result.symbolTable = sem.getSymbolTable()
                    .entrySet()
                    .stream()
                    .map(e -> e.getKey() + " : " + e.getValue())
                    .collect(Collectors.toList());

            // =========================================
            // STOP if semantic errors exist
            // =========================================

            if (!semOk) {

                result.tac = Collections.singletonList(
                        "Skipped — fix semantic errors first."
                );

                result.assembly = Collections.singletonList(
                        "Skipped — fix semantic errors first."
                );

                return result;
            }

            // =========================================
            // Phase 4 - Intermediate Code Generation
            // =========================================

            ICGenerator icg = new ICGenerator(tokens);

            result.tac = icg.generate();

            // =========================================
            // Phase 5 - Assembly Code Generation
            // =========================================

            CodeGenerator cg = new CodeGenerator();

            result.assembly = cg.generate(result.tac);

        }

        catch (Exception e) {

            result.errorMessage =
                    "Compiler error: " + e.getMessage();
        }

        return result;
    }
}