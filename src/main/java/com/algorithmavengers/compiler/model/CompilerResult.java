package com.algorithmavengers.compiler.model;

import java.util.List;
import java.util.List;
import java.util.Map;

public class CompilerResult {
    public List<String> tokens;
    public boolean parseSuccess;
    public String parseNote;
    public List<String> semanticErrors;
    public List<String> symbolTable;   // add this
    public List<String> tac;
    public List<String> optimizedTac;
    public List<String> assembly;
    public String errorMessage;
}