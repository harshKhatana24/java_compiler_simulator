package com.algorithmavengers.compiler;

/*
1. Keywords
if, else, while, for, return, break, continue,
fn, let, const, struct, enum, match


2. Identifiers Identifiers must:
• Start with a letter or
• Contain only letters, digits, or
• Have a maximum length of 64 characters


3. Numeric Literals Support the following:
• Decimal integers (e.g., 0, 123)
• Binary, octal, and hexadecimal (0b101, 0o755, 0xFF)
• Floating-point literals (3.14, 1e10, 2.5E-3)
Invalid numeric literals must be detected and reported.


4. String Literals
• Double-quoted strings
• Escape sequences: \n, \t, \\, \", \xNN
• Unterminated strings must raise lexical errors


5. Operators and Delimiters
+ - * / %
== != < <= > >=
&& || !
= += -= *= /=
-> :: .
( ) { } [ ]
, ; :


6. Comments ka

*/

import java.util.ArrayList;
import java.util.List;

public class Token_ {


    //keywords define karo - 1
    public static final String[] keywords = {
            "if", "else", "while", "for", "return", "break", "continue",
            "fn", "let", "const", "struct", "enum", "match",
            "int", "float", "string", "bool", "void"   // add these
    };

    //Identifier rule
    public boolean identifier(String idf){

        //1st - lenght check
        //if (idf.length()>64) return false;

        //2nd - jo 1st letter hai vo _ or letter hona chahiye
        if (!(Character.isLetter(idf.charAt(0)) || idf.charAt(0) == '_')) return false;

        //Contain only letters, digits, or _
        for (int i=0;i<idf.length();i++){

            char c = idf.charAt(i);

            if (!(Character.isLetterOrDigit(c) || c == '_')) return false;

        }

        return true;
    }

    //Numeric Literals - support karega - decimal,Binary, octal, hexadecimal
    public boolean numericLiterals(String s) {

        if (s == null || s.isEmpty()) return false;

        //+10, -4 like this handling sign
        //-10 hai -> operator = - & 10 = number
        if (s.charAt(0) == '+' || s.charAt(0) == '-') {
            //
            s = s.substring(1);
            if (s.isEmpty()) return false;
        }

        //binary ke liye
        // start -> 0b/oB -> only contains 0 & 1
        if (s.startsWith("0b") || s.startsWith("0B")) {
            String digits = s.substring(2); //-> binary string start after ob
            if (digits.isEmpty()) return false;
            for (char c : digits.toCharArray()) {
                if (c != '0' && c != '1')
                    return false;
            }return true;
        }

        //octal
        // start -> 0o/oO -> 0-7
        if (s.startsWith("0o") || s.startsWith("0O")) {
            String digits = s.substring(2);
            if (digits.isEmpty()) return false;


            for (char c : digits.toCharArray()) {
                if (c < '0' || c > '7') return false;
            }

            return true;
        }

        //hexadecima;
        //start -> 0x/0X -> 0-9,A,B,C,D,E,F
        if (s.startsWith("0x") || s.startsWith("0X")) {
            String digits = s.substring(2);
            if (digits.isEmpty()) return false;

            for (char c : digits.toCharArray()) {

                //hex - problem
                if (!Character.isDigit(c) &&
                        (Character.toLowerCase(c) < 'a' || Character.toLowerCase(c) > 'f')) {
                    return false;
                }
            }
            return true;
        }

        //Floating Point
        //1.12, .5, 1e10, 2.5E-3
        //java's double parser to validate -> format
        if (s.contains(".") || s.contains("e") || s.contains("E")) {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        //Decimal Integer
        // If none of the above, it must be digits only
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }


        return true;
    }


    //string ko classify
    public boolean isValidStringLiteral(String s) {
        //end & start = "vfvFV"
        if (s.charAt(0)!='"' || s.charAt(s.length()-1)!= '"') return false;


        for (int i=1; i<s.length() - 1; i++) {
            char c=s.charAt(i);


            if (c == '\\'){//-> escape sequence
                if (i + 1 >= s.length() - 1) return false;
                char next = s.charAt(i + 1);

                //all escape sequence
                if (next=='n' || next=='t' || next=='\\' || next=='"') i++;






                    // hexadecimal tu ni hai -> "hello\xGGworld"
                else if (next == 'x') {
                    if (i + 3 >= s.length() - 1) return false;
                    if (Character.digit(s.charAt(i + 2),16)==-1 ||Character.digit(s.charAt(i+3),16) ==-1)
                        return false;

                    i += 3;
                }

                else return false;


            }

            // in between if appear -> invalid
            else if (c == '"') return false;
        }
        return true;
    }



    //list of valid operators ki
    public boolean isOperator(String s) {
        if (s == null || s.isEmpty()) return false;

        String[] operators = {
                "+", "-", "*", "/", "%", "==", "!=", "<", "<=", ">", ">=",
                "&&", "||", "!", "=", "+=", "-=", "*=", "/=", "->", "::", "."};

        for (String op : operators) {
            if (op.equals(s)) return true;
        }

        return false;
    }


    //valid delimiter se compare karenge aur match hoga tu thik
    public boolean isDelimiter(String s) {
        if (s == null|| s.isEmpty()) return false;

        String[] delimiters = {"(", ")", "{", "}", "[", "]", ",", ";", ":"};

        for (String d : delimiters) {
            if (d.equals(s)) return true;
        }
        return false;
    }

    //Token class
    public static class Token {
        public String type;
        public String value;
        public int line;
        public int column;

        Token(String type, String value, int line, int column) {
            this.type = type;
            this.value = value;
            this.line = line;
            this.column = column;
        }

        //line , column no. added
        public String toString() {
            return "<" + type + ", " + value + "> at line " + line + ", column " + column;
        }
    }

    public List<Token> tokenize(String code) {
        int number = 0;
        int i = 0;
        int line = 1;
        int column = 1;



        //parser phase mea list
        List<Token> tokens = new ArrayList<>();



        //length error >64

        boolean flag = false;


        while (i < code.length()) {
            char c = code.charAt(i);

            //space aaya -> next
            if (Character.isWhitespace(c)) {
                if (c == '\n') { line++; column = 1; }
                else column++;
                i++;
                continue;
            }

            //comments ko deal karega
            // '//'
            if (i + 1 < code.length() && code.startsWith("//", i)) {
                while (i < code.length() && code.charAt(i) != '\n') { i++; column++; }
                continue;
            }

            // '/* */'
            if (i + 1 < code.length() && code.startsWith("/*", i)) {
                int start = i;
                int startLine = line, startColumn = column;
                i += 2; column += 2;
                while (i + 1 < code.length() && !code.startsWith("*/", i)) {
                    if (code.charAt(i) == '\n') { line++; column = 1; }
                    else column++;
                    i++;
                }
                if (i + 1 >= code.length()) {
                    Token t = new Token("ERROR", code.substring(start), startLine, startColumn);
                    System.out.println(t);
                    tokens.add(t);
                    break;
                }
                i += 2; column += 2;
                continue;
            }


            // String handling
            if (c == '"') {
                int start = i++;
                int startLine = line, startColumn = column;
                column++;
                boolean closed = false;

                //
                while (i < code.length()) {
                    if (code.charAt(i) == '\\') { i += 2; column += 2; }
                    else if (code.charAt(i) == '"') {
                        i++; column++;
                        closed = true;
                        break;
                    }

                    else {
                        if (code.charAt(i) == '\n') { line++; column = 1; }
                        else column++;
                        i++;
                    }
                }

                //start se end tak string token
                String str = code.substring(start, i);

                if (!closed){
                    Token t = new Token("error", str, startLine, startColumn);
                    System.out.println(t);
                    tokens.add(t);}
                else{
                    Token t = new Token(isValidStringLiteral(str) ? "string" : "error", str, startLine, startColumn);
                    System.out.println(t);
                    tokens.add(t);
                }

                number++;
                continue;
            }

            // Number
            if (Character.isDigit(c)) {
                int start = i;
                int startLine = line, startColumn = column;

                if (i + 1 < code.length() && code.charAt(i) == '0') {
                    char next = code.charAt(i + 1);

                    //binary number, octal, hexadecimal
                    if (next == 'b' || next == 'B' || next == 'o' || next == 'O' || next == 'x' || next == 'X') {
                        i += 2; column += 2;
                        while (i < code.length() && Character.isLetterOrDigit(code.charAt(i))) { i++; column++; }
                        String num = code.substring(start, i);
                        if (next == 'b' || next == 'B'){
                            Token t = new Token(numericLiterals(num) ? "binary number" : "error", num, startLine, startColumn);
                            System.out.println(t);
                            tokens.add(t);
                        } else if (next == 'o' || next == 'O') {
                            Token t = new Token(numericLiterals(num) ? "octal number" : "error", num, startLine, startColumn);
                            System.out.println(t);
                            tokens.add(t);
                        } else if (next == 'x' || next == 'X') {
                            Token t = new Token(numericLiterals(num) ? "hexadecimal number" : "error", num, startLine, startColumn);
                            System.out.println(t);
                            tokens.add(t);
                        }

                        number++;
                        continue;
                    }
                }

                while (i < code.length() && Character.isDigit(code.charAt(i)) ) {
                    i++; column++;
                }

                //agar letter aa jaye tu invalid ke liye
                if (i < code.length() && Character.isLetter(code.charAt(i))){
                    Token t = new Token("error", code.substring(start, i+1), startLine, startColumn);
                    System.out.println(t);
                    tokens.add(t);
                    while(code.charAt(i)!=' ') { i++; column++; }
                    number++;
                    continue;
                }

                //frac. part => .55
                if (i < code.length() && code.charAt(i) == '.' ) {
                    i++; column++;
                    while (i < code.length() && Character.isDigit(code.charAt(i))) { i++; column++; }
                }

                //exponent
                if (i < code.length() && (code.charAt(i) == 'e' || code.charAt(i) == 'E')) {
                    i++; column++;
                    if (i < code.length() && (code.charAt(i) == '+' || code.charAt(i) == '-')) { i++; column++; }
                    while (i < code.length() && Character.isDigit(code.charAt(i))) { i++; column++; }
                }

                String num = code.substring(start, i);
                Token t = new Token(
                        numericLiterals(num) ? "number" : "error", num, startLine, startColumn);
                System.out.println(t);
                tokens.add(t);

                number++;
                continue;
            }

            // Identifier / Keyword

            if (Character.isLetter(c) || c == '_') {
                int start = i;
                int startLine = line, startColumn = column;
                while (i < code.length() &&
                        (Character.isLetterOrDigit(code.charAt(i)) || code.charAt(i) == '_')) {
                    i++; column++;

                    //length error checking
                    if (i-start>64) {
                        flag=true;
                        while (i < code.length() && code.charAt(i)!=' ') i++;
                        break;
                    }
                }

                String word = code.substring(start, i);

                boolean isKeyword = false;
                //keywords ka identification
                for (String k : keywords) {
                    if (k.equals(word)) {
                        Token t = new Token("KEYWORD", word, startLine, startColumn);
                        System.out.println(t);
                        tokens.add(t);

                        isKeyword = true;
                        break;
                    }
                }

                if (!isKeyword) {
                    if (flag==true){
                        Token t = new Token("length error", word, startLine, startColumn);
                        System.out.println(t);
                        tokens.add(t);


                        flag = false;
                    }
                    else if (identifier(word)){
                        Token t = new Token("IDENTIFIER", word, startLine, startColumn);
                        System.out.println(t);
                        tokens.add(t);

                    }
                    else{
                        Token t = new Token("ERROR", word, startLine, startColumn);
                        System.out.println(t);
                        tokens.add(t);
                    }
                }

                number++;
                continue;
            }


            // Multi-character operators
            if (i + 1 < code.length()) {
                String two = code.substring(i, i + 2);
                if (isOperator(two)) {
                    Token t = new Token("OPERATOR", two, line, column);
                    System.out.println(t);
                    tokens.add(t);

                    i += 2;
                    column += 2;
                    number++;
                    continue;
                }
            }

            // Single character operator/delimiter
            String one = String.valueOf(c);
            int startLine = line, startColumn = column;

            if (isOperator(one))
            {
                Token t = new Token("OPERATOR", one, startLine, startColumn);
                System.out.println(t);
                tokens.add(t);
            }
            else if (isDelimiter(one)){


                Token t = new Token("DELIMITER", one, startLine, startColumn);
                System.out.println(t);
                tokens.add(t);
            }
            else {
                Token t = new Token("ERROR", one, startLine, startColumn);
                System.out.println(t);
                tokens.add(t);

            }

            number++;
            i++;
            column++;
        }

        return tokens;
    }

}
