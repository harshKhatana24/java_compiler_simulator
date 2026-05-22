//package com.algorithmavengers.compiler;
//
//// Optimizer.java
//import java.util.*;
//import java.util.regex.*;
//
//public class Optimizer {
//
//    public List<String> optimize(List<String> tac) {
//        List<String> result = new ArrayList<>(tac);
//        result = constantFolding(result);
//        result = copyPropagation(result);
//        result = deadCodeElimination(result);
//
//        System.out.println("\n--- Optimized TAC ---");
//        result.forEach(System.out::println);
//        return result;
//    }
//
//    // Constant folding: t1 = 3 + 4 -> t1 = 7
//    private List<String> constantFolding(List<String> tac) {
//        List<String> out = new ArrayList<>();
//        Pattern p = Pattern.compile("(\\w+) = (-?\\d+\\.?\\d*) ([+\\-*/]) (-?\\d+\\.?\\d*)");
//        for (String line : tac) {
//            Matcher m = p.matcher(line);
//            if (m.matches()) {
//                double a = Double.parseDouble(m.group(2));
//                double b = Double.parseDouble(m.group(4));
//                double res = switch (m.group(3)) {
//                    case "+" -> a + b;
//                    case "-" -> a - b;
//                    case "*" -> a * b;
//                    case "/" -> b != 0 ? a / b : Double.NaN;
//                    default  -> Double.NaN;
//                };
//                if (!Double.isNaN(res)) {
//                    String val = (res == Math.floor(res)) ? String.valueOf((long) res) : String.valueOf(res);
//                    out.add(m.group(1) + " = " + val);
//                    continue;
//                }
//            }
//            out.add(line);
//        }
//        return out;
//    }
//
//    // Copy propagation: if t1 = x, replace later uses of t1 with x
//    private List<String> copyPropagation(List<String> tac) {
//        Map<String, String> copies = new HashMap<>();
//        List<String> out = new ArrayList<>();
//        Pattern assign = Pattern.compile("(\\w+) = (\\w+)$");
//        for (String line : tac) {
//            // Replace known copies in RHS
//            for (Map.Entry<String, String> e : copies.entrySet()) {
//                line = line.replaceAll("\\b" + e.getKey() + "\\b", e.getValue());
//            }
//            Matcher m = assign.matcher(line);
//            if (m.matches()) {
//                copies.put(m.group(1), m.group(2));
//            }
//            out.add(line);
//        }
//        return out;
//    }
//
//    // Dead code elimination: remove assignments to temps never used again
//    private List<String> deadCodeElimination(List<String> tac) {
//        // Count uses of each variable
//        Map<String, Integer> useCount = new HashMap<>();
//        Pattern lhsP = Pattern.compile("^(\\w+) =");
//        for (String line : tac) {
//            String rhs = line.contains("=") ? line.substring(line.indexOf('=') + 1) : line;
//            for (String word : rhs.trim().split("[^\\w]+")) {
//                if (!word.isEmpty()) useCount.merge(word, 1, Integer::sum);
//            }
//        }
//        List<String> out = new ArrayList<>();
//        for (String line : tac) {
//            Matcher m = lhsP.matcher(line);
//            if (m.find()) {
//                String lhs = m.group(1);
//                // Only remove temp vars (t1, t2...) that are never used
//                if (lhs.matches("t\\d+") && useCount.getOrDefault(lhs, 0) == 0) {
//                    continue; // dead code, skip
//                }
//            }
//            out.add(line);
//        }
//        return out;
//    }
//}