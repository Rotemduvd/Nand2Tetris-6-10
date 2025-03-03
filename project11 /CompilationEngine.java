

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
    private String className;
    private int labelCount;

    public CompilationEngine(JackTokenizer tokenizer, VMWriter vmWriter) {
        this.tokenizer = tokenizer;
        this.vmWriter = vmWriter;
        this.symbolTable = new SymbolTable();
    }


    public void compileClass() throws IOException {

        //Skips everything until reaches field or static
        tokenizer.advance();
        tokenizer.advance();
        className = tokenizer.identifier();
        tokenizer.advance();
        tokenizer.advance();

        // Prints classVarDec*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyword().equals("static") || tokenizer.keyword().equals("field"))) {
            compileClassVarDec();
        }

        // Prints subroutineDec*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyword().equals("constructor") || tokenizer.keyword().equals("function") || tokenizer.keyword().equals("method"))) {
            compileSubroutine();
        }

        if (!tokenizer.hasMoreTokens()) {
            return; // Exit gracefully
        }
        //Skips }
        //tokenizer.advance();
    }

    public void compileClassVarDec() throws IOException {
        String kind = tokenizer.keyword(); // 'static' or 'field'
        tokenizer.advance();

        String type;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            type = tokenizer.keyword();
        } else {
            type = tokenizer.identifier();
        }
        tokenizer.advance();

        // Define first var
        symbolTable.define(tokenizer.identifier(), type, kind);
        tokenizer.advance();

        // Add additional vars if any
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            tokenizer.advance();
            symbolTable.define(tokenizer.identifier(), type, kind);
            tokenizer.advance();
        }

        tokenizer.advance();
    }

    public void compileSubroutine() throws IOException {
        symbolTable.reset();
        String subroutineType = tokenizer.keyword(); // 'constructor', 'function', 'method'
        tokenizer.advance();

        tokenizer.advance(); // Return type (e.g., void, int, className)
        String subroutineName = tokenizer.identifier();
        tokenizer.advance(); // Subroutine name

        if (subroutineType.equals("method")) {
            symbolTable.define("this", className, "arg");
        }

        tokenizer.advance(); // '('
        compileParameterList(); // Compiles the parameter list
        tokenizer.advance(); // ')' (move to '{')
        compileSubroutineBody(subroutineName, subroutineType); // Compiles the body
    }

    public void compileParameterList() throws IOException {
        // Close immediately if parameterlist is empty
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            return;
        }

        //Prints the varNames until there's no more
        while (true) {
            // Prints the type (e.g., int, boolean, or class name)
            String type;
            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
                type = tokenizer.keyword();
            } else {
                type = tokenizer.identifier();
            }
            tokenizer.advance();

            // Prints the param name
            String name = tokenizer.identifier();
            tokenizer.advance();

            // Define the parameter in the symbol table with kind 'arg'
            symbolTable.define(name, type, "arg");

            // Check if there's another param
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                tokenizer.advance();
            } else {
                break; // Exit the loop if no more params
            }
        }
    }

    public void compileSubroutineBody(String subroutineName, String subroutineType) throws IOException {
        String vmFunctionName = className + "." + subroutineName;

        // Ensure the next token is '{'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            tokenizer.advance(); // Move past '{'
        } else {
            throw new IllegalStateException("Expected '{' at the start of subroutine body");
        }

        // Compile varDec* and count local variables
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("var")) {
            compileVarDec(); // Define local variables
        }

        // Count local variables AFTER processing all varDec declarations
        int nLocals = symbolTable.varCount("var");

        // Write the function declaration
        vmWriter.writeFunction(vmFunctionName, nLocals);

        // Additional logic for constructors and methods
        if (subroutineType.equals("constructor")) {
            int fieldCount = symbolTable.varCount("field");
            vmWriter.writePush("constant", fieldCount);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        } else if (subroutineType.equals("method")) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        }

        // Compile statements
        compileStatements();

        // Ensure the next token is '}'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.advance(); // Move past '}'
        } else {
            throw new IllegalStateException("Expected '}' at the end of subroutine body");
        }
    }

    public void compileVarDec() throws IOException {
        tokenizer.advance(); // Advance past 'var'

        // Get type
        String type;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            type = tokenizer.keyword(); // 'int', 'char', 'boolean'
        } else {
            type = tokenizer.identifier(); // Class name
        }
        tokenizer.advance();

        // Handle first variable
        String varName = tokenizer.identifier();
        symbolTable.define(varName, type, "var");
        tokenizer.advance(); // Advance past the variable name

        // Handle additional variables separated by ','
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            tokenizer.advance(); // Advance past ','
            varName = tokenizer.identifier();
            symbolTable.define(varName, type, "var");
            tokenizer.advance(); // Advance past the variable name
        }

        // Expect ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            tokenizer.advance(); // Advance past ';'
        } else {
            throw new IllegalStateException("Expected ';' at the end of variable declaration");
        }
    }

    public void compileStatements() throws IOException {
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            switch (tokenizer.keyword()) {
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    return; // Exit when no more statements
            }
        }
    }

    public void compileLet() throws IOException {
        tokenizer.advance(); // 'let'

        String varName = tokenizer.identifier();
        tokenizer.advance();

        boolean isArray = false;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            // Array access: varName[expression]
            isArray = true;
            tokenizer.advance(); // '['
            compileExpression();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ']') {
                tokenizer.advance(); // ']'
            } else {
                throw new IllegalStateException("Expected ']'");
            }



            // Compute the memory location of varName[index]
            vmWriter.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName)); // Push base address
            vmWriter.writeArithmetic("add"); // Add base + index
        }

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '=') {
            tokenizer.advance(); // '='
            compileExpression(); // Compile the right-hand side value
        } else {
            throw new IllegalStateException("Expected '='");
        }

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            tokenizer.advance(); // ';'
        } else {
            throw new IllegalStateException("Expected ';'");
        }

        if (isArray) {
            // Pop the value into the array location
            vmWriter.writePop("temp", 0); // Store the value temporarily
            vmWriter.writePop("pointer", 1); // Set `that` to base + index
            vmWriter.writePush("temp", 0); // Retrieve the value
            vmWriter.writePop("that", 0); // Store the value at that[0]
        } else {
            // Pop the value into the variable
            vmWriter.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
        }
    }

    public void compileIf() throws IOException {
        tokenizer.advance(); // 'if'

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            tokenizer.advance();
            compileExpression();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected ')'");
            }
        }

        vmWriter.writeArithmetic("not");

        String label1 = "L" + labelCount++;
        String label2 = "L" + labelCount++;
        vmWriter.writeIf(label1);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            tokenizer.advance();
            compileStatements();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected '}'");
            }
        }

        vmWriter.writeGoto(label2);
        vmWriter.writeLabel(label1);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("else")) {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
                tokenizer.advance();
                compileStatements();

                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
                    tokenizer.advance();
                } else {
                    throw new IllegalStateException("Expected '}'");
                }
            }
        }

        vmWriter.writeLabel(label2);
    }

    public void compileWhile() throws IOException {
        String label1 = "L" + labelCount++;
        String label2 = "L" + labelCount++;
        vmWriter.writeLabel(label1);

        tokenizer.advance(); // 'while'

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            tokenizer.advance(); // '('
            compileExpression();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                tokenizer.advance(); // ')'
            } else {
                throw new IllegalStateException("Expected ')'");
            }
        }

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(label2);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            tokenizer.advance(); // '{'
            compileStatements();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
                tokenizer.advance(); // '}'
            } else {
                throw new IllegalStateException("Expected '}'");
            }
        }

        vmWriter.writeGoto(label1);
        vmWriter.writeLabel(label2);
    }


    public void compileDo() throws IOException {
        // Parse 'do'
        tokenizer.advance();

        // Parse subroutine or object/class name
        String name = tokenizer.identifier();
        tokenizer.advance();

        int nArgs = 0;
        boolean isInstanceMethod = false;

        // Handle '.' for object or class method
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            tokenizer.advance(); // Parse '.'
            String subroutineName = tokenizer.identifier();
            tokenizer.advance();

            if (symbolTable.kindOf(name).equals("NONE")) {
                // Static method
                name += "." + subroutineName;
            } else {
                // Instance method
                vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name)); // Push object reference
                name = symbolTable.typeOf(name) + "." + subroutineName;
                nArgs++;
            }
        } else {
            // Method of current class
            vmWriter.writePush("pointer", 0); // Push 'this'
            name = className + "." + name;
            nArgs++;
        }

        // Parse '(' and compile arguments
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            tokenizer.advance(); // Parse '('
            nArgs += compileExpressionList();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                tokenizer.advance(); // Parse ')'
            } else {
                throw new IllegalStateException("Expected ')'");
            }
        } else {
            throw new IllegalStateException("Expected '('");
        }

        // Write the subroutine call
        vmWriter.writeCall(name, nArgs);

        // Parse ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ';'");
        }

        // Discard return value
        vmWriter.writePop("temp", 0);
    }


    public void compileReturn() throws IOException {
        tokenizer.advance(); // 'return'

        // Check if it's a value return or void return
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression(); // Compile the expression to return
        } else {
            vmWriter.writePush("constant", 0); // Push 0 for void return
        }

        // Ensure ';' is present
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            tokenizer.advance(); // ';'
        } else {
            throw new IllegalStateException("Expected ';'");
        }

        vmWriter.writeReturn(); // Write the VM return instruction
    }

    public void compileExpression() throws IOException {
        compileTerm(); // Compile the first term

        // Handle binary operations (e.g., +, -, *, /, &, |, etc.)
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                "+-*/&|<>=".indexOf(tokenizer.symbol()) != -1) {
            char operator = tokenizer.symbol();
            tokenizer.advance(); // Advance past the operator
            compileTerm(); // Compile the next term

            // Write the VM command for the operator
            switch (operator) {
                case '+': vmWriter.writeArithmetic("add"); break;
                case '-': vmWriter.writeArithmetic("sub"); break;
                case '*': vmWriter.writeCall("Math.multiply", 2); break;
                case '/': vmWriter.writeCall("Math.divide", 2); break;
                case '&': vmWriter.writeArithmetic("and"); break;
                case '|': vmWriter.writeArithmetic("or"); break;
                case '<': vmWriter.writeArithmetic("lt"); break;
                case '>': vmWriter.writeArithmetic("gt"); break;
                case '=': vmWriter.writeArithmetic("eq"); break;
            }
        }
    }


    public void compileTerm() throws IOException {
        // Handle different types of terms
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            // Integer constant
            vmWriter.writePush("constant", tokenizer.intVal());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            // String constant
            String str = tokenizer.stringVal();
            vmWriter.writePush("constant", str.length());
            vmWriter.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                vmWriter.writePush("constant", (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            // Keyword constants (true, false, null, this)
            switch (tokenizer.keyword()) {
                case "true":
                    vmWriter.writePush("constant", 1);
                    vmWriter.writeArithmetic("neg");
                    break;
                case "false":
                case "null":
                    vmWriter.writePush("constant", 0);
                    break;
                case "this":
                    vmWriter.writePush("pointer", 0);
                    break;
                default:
                    throw new IllegalStateException("Unexpected keyword in term");
            }
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            // Variable, array access, or subroutine call
            String name = tokenizer.identifier();
            tokenizer.advance();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
                // Array access: varName[expression]
                tokenizer.advance(); // '['
                compileExpression(); // Compile the index expression
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ']') {
                    tokenizer.advance(); // ']'
                } else {
                    throw new IllegalStateException("Expected ']'");
                }
                vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
                vmWriter.writeArithmetic("add");
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0);
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                // Subroutine call
                String fullName = name;
                int nArgs = 0;

                if (tokenizer.symbol() == '.') {
                    // Handle `Class.method` or `object.method`
                    tokenizer.advance(); // '.'
                    String subroutineName = tokenizer.identifier();
                    tokenizer.advance();

                    if (symbolTable.kindOf(name).equals("NONE")) {
                        // Static method
                        fullName += "." + subroutineName;
                    } else {
                        // Instance method
                        vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name)); // Push object reference
                        fullName = symbolTable.typeOf(name) + "." + subroutineName;
                        nArgs++;
                    }
                } else {
                    // Current class method
                    vmWriter.writePush("pointer", 0); // Push 'this'
                    fullName = className + "." + fullName;
                    nArgs++;
                }

                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
                    tokenizer.advance(); // '('
                    nArgs += compileExpressionList(); // Compile arguments
                    if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                        tokenizer.advance(); // ')'
                    } else {
                        throw new IllegalStateException("Expected ')'");
                    }
                } else {
                    throw new IllegalStateException("Expected '(' after subroutine name");
                }

                vmWriter.writeCall(fullName, nArgs);
            } else {
                // Variable
                vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            // (exp) or unary operation
            if (tokenizer.symbol() == '(') {
                tokenizer.advance(); // '('
                compileExpression(); // Compile the inner expression
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                    tokenizer.advance(); // ')'
                } else {
                    throw new IllegalStateException("Expected ')'");
                }
            } else if ("-~".indexOf(tokenizer.symbol()) != -1) {
                char unaryOp = tokenizer.symbol();
                tokenizer.advance(); // Unary operator
                compileTerm(); // Compile the term after the unary operator
                if (unaryOp == '-') {
                    vmWriter.writeArithmetic("neg");
                } else if (unaryOp == '~') {
                    vmWriter.writeArithmetic("not");
                }
            }
        } else {
            throw new IllegalStateException("Invalid term");
        }
    }

    private int compileExpressionList() throws IOException {
        int nArgs = 0;

        // Check if the list is empty
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            compileExpression();
            nArgs++; // Count the argument

            // Handle expressions separated by commas
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                tokenizer.advance(); // Skip ,
                compileExpression(); // Compile the next expression
                nArgs++; // Increment the argument count
            }
        }

        return nArgs;
    }



}