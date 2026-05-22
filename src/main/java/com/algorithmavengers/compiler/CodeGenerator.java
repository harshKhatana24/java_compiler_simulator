package com.algorithmavengers.compiler;

import java.util.*;
import java.util.regex.*;

public class CodeGenerator {

    private final List<String> assembly = new ArrayList<>();

    // Stores variables for .data section
    private final Set<String> dataVars = new LinkedHashSet<>();

    public List<String> generate(List<String> optimizedTac) {

        assembly.clear();
        dataVars.clear();

        List<String> body = new ArrayList<>();

        // TAC patterns
        Pattern threeAddr =
                Pattern.compile("(\\w+) = (\\w+) ([+\\-*/]) (\\w+)");

        Pattern copyAssign =
                Pattern.compile("(\\w+) = (-?\\w+|-?\\d+)");

        // =========================================
        // PROCESS TAC
        // =========================================

        for (String instr : optimizedTac) {

            Matcher m3 = threeAddr.matcher(instr);
            Matcher mc = copyAssign.matcher(instr);

            // THREE ADDRESS CODE
            if (m3.matches()) {

                String dest = m3.group(1);
                String src1 = m3.group(2);
                String op   = m3.group(3);
                String src2 = m3.group(4);

                trackVar(dest);
                trackVar(src1);
                trackVar(src2);

                body.add("    MOV EAX, " + operand(src1));

                switch (op) {

                    case "+" ->
                            body.add("    ADD EAX, " + operand(src2));

                    case "-" ->
                            body.add("    SUB EAX, " + operand(src2));

                    case "*" ->
                            body.add("    IMUL EAX, " + operand(src2));

                    case "/" -> {
                        body.add("    CDQ");
                        body.add("    MOV EBX, " + operand(src2));
                        body.add("    IDIV EBX");
                    }
                }

                // store result
                body.add("    MOV " + operand(dest) + ", EAX");
                body.add("");
            }

            // SIMPLE ASSIGNMENT
            else if (mc.matches()) {

                String dest = mc.group(1);
                String src  = mc.group(2);

                // IMPORTANT FIX:
                // destination must be variable only
                if (!dest.matches("[a-zA-Z_]\\w*")) {
                    continue;
                }

                trackVar(dest);
                trackVar(src);

                body.add("    MOV EAX, " + operand(src));
                body.add("    MOV " + operand(dest) + ", EAX");
                body.add("");
            }
        }

        // =========================================
        // DATA SECTION
        // =========================================

        assembly.add("section .data");

        for (String v : dataVars) {
            assembly.add("    " + v + " DD 0");
        }

        assembly.add("");

        // =========================================
        // TEXT SECTION
        // =========================================

        assembly.add("section .text");
        assembly.add("global _start");
        assembly.add("");

        assembly.add("_start:");

        assembly.addAll(body);

        // =========================================
        // EXIT
        // =========================================

        assembly.add("    ; exit");
        assembly.add("    MOV EAX, 1");
        assembly.add("    MOV EBX, 0");
        assembly.add("    INT 80h");

        System.out.println("\n--- Generated Assembly Code ---");

        for (String line : assembly) {
            System.out.println(line);
        }

        return assembly;
    }

    // =========================================
    // VARIABLE TRACKING
    // =========================================

    private void trackVar(String s) {

        // store only identifiers
        if (s.matches("[a-zA-Z_]\\w*")) {
            dataVars.add(s);
        }
    }

    // =========================================
    // FORMAT OPERANDS
    // Variables -> [x]
    // Constants -> 5
    // =========================================

    private String operand(String s) {

        // numeric constant
        if (s.matches("-?\\d+")) {
            return s;
        }

        // variable / temp
        return "[" + s + "]";
    }

    public List<String> getAssembly() {
        return assembly;
    }
}