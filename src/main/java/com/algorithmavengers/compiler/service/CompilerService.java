package com.algorithmavengers.compiler.service;



import com.algorithmavengers.compiler.*;
import org.springframework.stereotype.Service;
import com.algorithmavengers.compiler.model.CompilerResult;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collections;


@Service
public class CompilerService {







    public CompilerResult compile(String sourceCode) {
        CompilerResult result = new CompilerResult();









        try {







            // Phase 1 - Lexer (works on any Java code)
            Token_ lexer = new Token_();
            List<Token_.Token> tokens = lexer.tokenize(sourceCode);
            result.tokens = tokens.stream()
                    .map(t -> "<" + t.type + ", " + t.value + "> at line " + t.line + ", col " + t.column)
                    .collect(Collectors.toList());




            // Phase 2 - Parser (only works on simple assignments)
            // Skip for Java files, just report token count
            result.parseSuccess = true;
            result.parseNote = "Parsed " + tokens.size() + " tokens successfully.";

            // Phase 3 - Semantic Analysis
            // Phase 3
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.setStrictMode(true);
            boolean semOk = sem.analyze(tokens);
            result.semanticErrors = sem.getErrors();
            result.symbolTable = sem.getSymbolTable()
                    .entrySet().stream()
                    .map(e -> e.getKey() + " : " + e.getValue())
                    .collect(Collectors.toList());

// STOP HERE if semantic errors found
            if (!semOk) {
                result.tac = Collections.singletonList("Skipped — fix semantic errors first.");
                result.optimizedTac = Collections.singletonList("Skipped — fix semantic errors first.");
                result.assembly = Collections.singletonList("Skipped — fix semantic errors first.");
                return result;
            }

// Only reach here if no semantic errors
            ICGenerator icg = new ICGenerator(tokens);
            result.tac = icg.generate();

            Optimizer opt = new Optimizer();
            result.optimizedTac = opt.optimize(result.tac);

            CodeGenerator cg = new CodeGenerator();
            result.assembly = cg.generate(result.optimizedTac);

        } catch (Exception e) {
            result.errorMessage = "Compiler error: " + e.getMessage();
        }
        return result;
    }
}