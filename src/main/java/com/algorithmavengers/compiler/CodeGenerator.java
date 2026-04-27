package com.algorithmavengers.compiler;

// CodeGenerator.java
import java.util.*;
import java.util.regex.*;

public class CodeGenerator {

    private List<String> assembly = new ArrayList<>();
    private Set<String> dataVars = new LinkedHashSet<>(); // for .data section
    private int labelCount = 0;

    public List<String> generate(List<String> optimizedTac) {
        assembly.add("section .data");

        List<String> body = new ArrayList<>();
        Pattern threeAddr = Pattern.compile("(\\w+) = (\\w+) ([+\\-*/]) (\\w+)");
        Pattern copyAssign = Pattern.compile("(\\w+) = (\\w+|\\d+\\.?\\d*)");

        for (String instr : optimizedTac) {
            Matcher m3 = threeAddr.matcher(instr);
            Matcher mc = copyAssign.matcher(instr);

            if (m3.matches()) {
                String dest = m3.group(1);
                String src1 = m3.group(2);
                String op   = m3.group(3);
                String src2 = m3.group(4);

                trackVar(dest); trackVar(src1); trackVar(src2);

                body.add("  MOV EAX, " + src1);
                body.add(switch (op) {
                    case "+" -> "  ADD EAX, " + src2;
                    case "-" -> "  SUB EAX, " + src2;
                    case "*" -> "  IMUL EAX, " + src2;
                    case "/" -> {
                        body.add("  CDQ");
                        body.add("  MOV EBX, " + src2);
                        yield "  IDIV EBX";
                    }
                    default -> "  ; unknown op " + op;
                });
                body.add("  MOV " + dest + ", EAX");
                body.add("");

            } else if (mc.matches()) {
                String dest = mc.group(1);
                String src  = mc.group(2);
                trackVar(dest);
                body.add("  MOV EAX, " + src);
                body.add("  MOV " + dest + ", EAX");
                body.add("");
            }
        }

        // Build .data declarations
        for (String v : dataVars) {
            assembly.add("  " + v + " DD 0");
        }
        assembly.add("");
        assembly.add("section .text");
        assembly.add("global _start");
        assembly.add("_start:");
        assembly.addAll(body);
        assembly.add("  ; program end");

        System.out.println("\n--- Generated Assembly Code ---");
        assembly.forEach(System.out::println);
        return assembly;
    }

    private void trackVar(String s) {
        if (s.matches("[a-zA-Z_]\\w*")) dataVars.add(s);
    }

    public List<String> getAssembly() { return assembly; }
}