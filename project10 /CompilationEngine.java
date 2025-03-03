

import java.io.BufferedWriter;
import java.io.IOException;

public class CompilationEngine {

    private JackTokenizer tokenizer;   
    private BufferedWriter writer;     

    public CompilationEngine(JackTokenizer tokenizer, BufferedWriter writer) {
        this.tokenizer = tokenizer;
        this.writer = writer;
    }

    public void compileClass() throws IOException {
        writer.write("<class>\n");

        //advance to the first token
        tokenizer.advance();


        //Prints class
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("class")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'class' keyword");
        }

        //Prints classname
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected class name");
        }

        // Prints '{'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            writeToken("symbol", "{");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '{' symbol");
        }

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

        // Prints '}'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            writeToken("symbol", "}");

        } else {
            throw new IllegalStateException("Expected '}' symbol");
        }

        writer.write("</class>\n");

        if (!tokenizer.hasMoreTokens()) {
            return; // Exit gracefully
        }
    }

    public void compileClassVarDec() throws IOException {
        writer.write("<classVarDec>\n");

        // Print 'static' or 'field'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyword().equals("static") || tokenizer.keyword().equals("field"))) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'static' or 'field' keyword");
        }

        // Print Type
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected type (int, char, boolean, or class name)");
        }

        // Print varName
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected variable name");
        }

        // Print (',' varName)*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            writeToken("symbol", ",");
            tokenizer.advance();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken("identifier", tokenizer.identifier());
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected variable name after ','");
            }
        }

        // Print ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            writeToken("symbol", ";");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ';' at the end of classVarDec");
        }

        writer.write("</classVarDec>\n");
    }

    public void compileSubroutine() throws IOException {
        writer.write("<subroutineDec>\n");

        // Print 'constructor', 'function', or 'method'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyword().equals("constructor") ||
                        tokenizer.keyword().equals("function") ||
                        tokenizer.keyword().equals("method"))) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'constructor', 'function', or 'method'");
        }

        // Print type ('void', 'int', 'char', 'boolean', or class name)
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected return type (void, int, char, boolean, or class name)");
        }

        // Print subroutineName
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected subroutine name");
        }

        // Print '('
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeToken("symbol", "(");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '('");
        }

        // Compile parameterList
        compileParameterList();

        // Print ')'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            writeToken("symbol", ")");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ')'");
        }

        // Compile subroutineBody
        compileSubroutineBody();

        writer.write("</subroutineDec>\n");
    }

    public void compileParameterList() throws IOException {
        writer.write("<parameterList>\n");

        // Close immediately if parameterlist is empty
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            writer.write("</parameterList>\n");
            return;
        }

        // Prints (type varName)
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD || tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            // Prints type
            writeToken(tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? "keyword" : "identifier", tokenizer.keyword());
            tokenizer.advance();

            // Prints varName
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken("identifier", tokenizer.identifier());
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected variable name in parameter list");
            }
        }

        // Handle (',' type varName)*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            // Prints ','
            writeToken("symbol", ",");
            tokenizer.advance();

            // Prints type
            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD || tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken(tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? "keyword" : "identifier", tokenizer.keyword());
                tokenizer.advance();

                // Prints varName
                if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                    writeToken("identifier", tokenizer.identifier());
                    tokenizer.advance();
                } else {
                    throw new IllegalStateException("Expected variable name after ',' in parameter list");
                }
            } else {
                throw new IllegalStateException("Expected type after ',' in parameter list");
            }
        }
        writer.write("</parameterList>\n");
    }

    public void compileSubroutineBody() throws IOException {
        writer.write("<subroutineBody>\n");

        // Print '{' symbol
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            writeToken("symbol", "{");
            tokenizer.advance();
        } else {
            debugCurrentToken();
            throw new IllegalStateException("Expected '{' symbol");
        }

        // Parse varDec*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("var")) {
            compileVarDec();
        }

        // Parse statements
        compileStatements();

        // Print '}' symbol
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            writeToken("symbol", "}");
            tokenizer.advance();
        } else {
            debugCurrentToken();
            throw new IllegalStateException("Expected '}' symbol");
        }

        writer.write("</subroutineBody>\n");
    }

    public void compileVarDec() throws IOException {
        writer.write("<varDec>\n");

        // Print 'var' keyword
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("var")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            debugCurrentToken();
            throw new IllegalStateException("Expected 'var' keyword");
        }

        // Type (int, char, boolean, or className)
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD || tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
                writeToken("keyword", tokenizer.keyword()); // Handle keywords like 'int', 'char', 'boolean'
            } else {
                writeToken("identifier", tokenizer.identifier()); // Handle class names
            }
            tokenizer.advance(); 
        } else {
            debugCurrentToken(); 
            throw new IllegalStateException("Expected type (int, char, boolean, or class name)");
        }

        // First variable name
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            debugCurrentToken();
            throw new IllegalStateException("Expected variable name");
        }

        // Handle ',' varName*
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            writeToken("symbol", ",");
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken("identifier", tokenizer.identifier());
                tokenizer.advance();
            } else {
                debugCurrentToken();
                throw new IllegalStateException("Expected variable name after ','");
            }
        }

        // Print ';' symbol
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            writeToken("symbol", ";");
            tokenizer.advance();
        } else {
            debugCurrentToken();
            throw new IllegalStateException("Expected ';' at the end of varDec");
        }

        writer.write("</varDec>\n");
    }

    public void compileStatements() throws IOException {
        writer.write("<statements>\n");

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

        writer.write("</statements>\n");
    }

    public void compileLet() throws IOException {
        writer.write("<letStatement>\n");

        // Print 'let'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("let")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'let' keyword");
        }

        // varName
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected variable name");
        }

        // Optional array indexing: '[' expression ']'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            writeToken("symbol", "[");
            tokenizer.advance();
            compileExpression(); // Array index expression
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ']') {
                writeToken("symbol", "]");
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected ']'");
            }
        }

        // Print '='
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '=') {
            writeToken("symbol", "=");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '=' symbol");
        }
        
        compileExpression();

        // Print ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            writeToken("symbol", ";");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ';'");
        }

        writer.write("</letStatement>\n");
    }

    public void compileIf() throws IOException {
        writer.write("<ifStatement>\n");

        // Print 'if' keyword
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("if")) {
            writeToken("keyword", "if");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'if' keyword");
        }

        // Print '('
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeToken("symbol", "(");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '(' symbol after 'if'");
        }

        // Compile expression
        compileExpression();

        // Print ')'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            writeToken("symbol", ")");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ')' symbol after expression");
        }

        // Print '{'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            writeToken("symbol", "{");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '{' symbol after ')'");
        }

        // Compile statements
        compileStatements();

        // Print '}'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            writeToken("symbol", "}");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '}' symbol after statements");
        }

        // Check for 'else' keyword
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("else")) {
            // Print 'else' keyword
            writeToken("keyword", "else");
            tokenizer.advance();

            // Print '{'
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
                writeToken("symbol", "{");
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected '{' symbol after 'else'");
            }

            // Compile statements
            compileStatements();

            // Print '}'
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
                writeToken("symbol", "}");
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected '}' symbol after else statements");
            }
        }

        writer.write("</ifStatement>\n");
    }

    public void compileWhile() throws IOException {
        writer.write("<whileStatement>\n");

        // Print 'while'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("while")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'while' keyword");
        }

        // Print '('
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeToken("symbol", "(");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '(' symbol");
        }

        // Compile the condition as an expression
        compileExpression();

        // Print ')'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            writeToken("symbol", ")");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ')' symbol");
        }

        // Print '{'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '{') {
            writeToken("symbol", "{");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '{' symbol");
        }

        // Compile statements inside the while block
        writer.write("<statements>\n");
        while (isStatement()) {
            compileStatement();
        }
        writer.write("</statements>\n");

        // Print '}'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            writeToken("symbol", "}");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '}' symbol");
        }

        writer.write("</whileStatement>\n");
    }

    public void compileDo() throws IOException {
        writer.write("<doStatement>\n");

        // Print 'do' keyword
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("do")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'do' keyword");
        }

        // Print subroutine call (identifier or class/variable name)
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeToken("identifier", tokenizer.identifier());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected identifier");
        }

        // Handle '.' or '('
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            writeToken("symbol", ".");
            tokenizer.advance();

            // Subroutine name
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken("identifier", tokenizer.identifier());
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected subroutine name after '.'");
            }
        }

        // Handle '('
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeToken("symbol", "(");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected '('");
        }

        // Handle expressionList
        compileExpressionList();

        // Handle ')'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            writeToken("symbol", ")");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ')'");
        }

        // Handle ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            writeToken("symbol", ";");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ';' at the end of doStatement");
        }

        writer.write("</doStatement>\n");
    }

    public void compileReturn() throws IOException {
        writer.write("<returnStatement>\n");

        // Print 'return'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword().equals("return")) {
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected 'return' keyword");
        }

        // Optional expression
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression();
        }

        // Print ';'
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            writeToken("symbol", ";");
            tokenizer.advance();
        } else {
            throw new IllegalStateException("Expected ';'");
        }

        writer.write("</returnStatement>\n");
    }

    public void compileExpression() throws IOException {
        writer.write("<expression>\n");

        // Compile the first term
        compileTerm();

        // Check for zero or more binary ops
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && "+-*/&|<>=~".contains("" + tokenizer.symbol())) {
            String symbol = String.valueOf(tokenizer.symbol());
            symbol = switch (symbol) {
                case "<" -> "&lt;";
                case ">" -> "&gt;";
                case "&" -> "&amp;";
                default -> symbol;
            };
            writeToken("symbol", symbol);
            tokenizer.advance();
            compileTerm();
        }

        writer.write("</expression>\n");
    }

    public void compileTerm() throws IOException {
        writer.write("<term>\n");

        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            // Integer constant
            writeToken("integerConstant", String.valueOf(tokenizer.intVal()));
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            // String constant
            writeToken("stringConstant", tokenizer.stringVal());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                ("true".equals(tokenizer.keyword()) || "false".equals(tokenizer.keyword()) ||
                        "null".equals(tokenizer.keyword()) || "this".equals(tokenizer.keyword()))) {
            // Keyword constant
            writeToken("keyword", tokenizer.keyword());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            // Variable, array entry, or subroutine call
            String identifier = tokenizer.identifier();
            tokenizer.advance();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
                // Array entry (varName[expression])
                writeToken("identifier", identifier); // varName
                writeToken("symbol", "[");
                tokenizer.advance();
                compileExpression(); // expression
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ']') {
                    writeToken("symbol", "]");
                    tokenizer.advance();
                } else {
                    throw new IllegalStateException("Expected ']' after array expression");
                }
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                // Subroutine call
                writeToken("identifier", identifier); // subroutine/class name
                compileSubroutineCall();
            } else {
                // Plain variable name
                writeToken("identifier", identifier);
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            // Parenthesized expression
            writeToken("symbol", "(");
            tokenizer.advance();
            compileExpression();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                writeToken("symbol", ")");
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected ')' after parenthesized expression");
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
            // Unary operator
            writeToken("symbol", String.valueOf(tokenizer.symbol()));
            tokenizer.advance();
            compileTerm();
        } else {
            throw new IllegalStateException("Unexpected token in term");
        }

        writer.write("</term>\n");
    }

    public void compileExpressionList() throws IOException {
        writer.write("<expressionList>\n");

        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            // At least one expression
            compileExpression();

            // Handle ',' between expressions
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                writeToken("symbol", ",");
                tokenizer.advance();
                compileExpression();
            }
        }

        writer.write("</expressionList>\n");
    }

    private boolean isStatement() {
        return tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyword().equals("let") || tokenizer.keyword().equals("if") ||
                        tokenizer.keyword().equals("while") || tokenizer.keyword().equals("do") ||
                        tokenizer.keyword().equals("return"));
    }

    private void compileStatement() throws IOException {
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
                throw new IllegalStateException("Unexpected statement keyword");
        }
    }

    public void compileSubroutineCall() throws IOException {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            // Simple subroutine call: subroutineName(expressionList)
            writeToken("symbol", "(");
            tokenizer.advance();
            compileExpressionList();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                writeToken("symbol", ")");
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected ')' after subroutine call");
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            // Class or object method call: className.subroutineName(expressionList)
            writeToken("symbol", ".");
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeToken("identifier", tokenizer.identifier());
                tokenizer.advance();
            } else {
                throw new IllegalStateException("Expected subroutine name after '.'");
            }
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
                writeToken("symbol", "(");
                tokenizer.advance();
                compileExpressionList();
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
                    writeToken("symbol", ")");
                    tokenizer.advance();
                } else {
                    throw new IllegalStateException("Expected ')' after subroutine call");
                }
            } else {
                throw new IllegalStateException("Expected '(' after subroutine name");
            }
        } else {
            throw new IllegalStateException("Invalid subroutine call syntax");
        }
    }

    private void writeToken(String type, String value) throws IOException {
        writer.write("<" + type + "> " + value + " </" + type + ">\n");
    }

    private void debugCurrentToken() {
        System.out.println("Debug - Current Token: " + tokenizer.currToken +
                " | Token Type: " + tokenizer.tokenType());
    }

    public void close() throws IOException {
        writer.close();
    }
}