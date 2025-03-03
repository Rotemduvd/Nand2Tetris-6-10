

import java.io.*;
import java.util.*;

public class JackTokenizer implements AutoCloseable {
    private BufferedReader reader;
    private String currLine;
    String currToken;
    private Queue<String> tokenQueue = new LinkedList<>();



    public JackTokenizer(String inputFile) {
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            currLine = null;
            currToken = null;
        } catch (IOException e) {
            throw new RuntimeException("Error opening file: " + inputFile, e);
        }
    }

    public boolean hasMoreTokens() {
        try {
            // If the queue is not empty, tokens are available
            if (!tokenQueue.isEmpty()) {
                return true;
            }

            // Read lines until valid tokens are found
            while ((currLine = reader.readLine()) != null) {
                currLine = currLine.trim();

                // Skip single-line comments
                if (currLine.startsWith("//")) {
                    continue; // Ignore the line and move to the next
                }

                // Handle multi-line comments
                if (currLine.startsWith("/*")) {
                    while (currLine != null && !currLine.endsWith("*/")) {
                        currLine = reader.readLine(); // Skip until the end of the multi-line comment
                    }
                    continue; // Skip the line containing the closing "*/"
                }

                // Tokenize the remaining line if it has valid content
                tokenizeLine(currLine);
                if (!tokenQueue.isEmpty()) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // No more tokens
    }

    public void advance() {
        if (!hasMoreTokens()) {
            throw new IllegalStateException("No more tokens available");
        }

        // Retrieve the next token from the queue
        currToken = tokenQueue.poll();
    }

    private void tokenizeLine(String line) {
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // Ignore inline comments
            if (line.startsWith("//", i)) {
                break; // Stop processing the line
            }

            // Handle symbols
            if ("{}()[].,;+-*/&|<>=~:".indexOf(ch) != -1) {
                tokenQueue.add(String.valueOf(ch));
                i++;
                continue;
            }

            // Handle string constants
            if (ch == '"') {
                int start = i + 1;
                i++;
                while (i < line.length() && line.charAt(i) != '"') {
                    i++;
                }
                if (i < line.length()) {
                    tokenQueue.add("\"" + line.substring(start, i) + "\"");
                    i++;
                }
                continue;
            }

            // Handle identifiers and keywords
            if (Character.isLetter(ch) || ch == '_') {
                int start = i;
                while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                    i++;
                }
                tokenQueue.add(line.substring(start, i));
                continue;
            }

            // Handle integer constants
            if (Character.isDigit(ch)) {
                int start = i;
                while (i < line.length() && Character.isDigit(line.charAt(i))) {
                    i++;
                }
                tokenQueue.add(line.substring(start, i));
                continue;
            }

            // Unrecognized character (error handling)
            throw new IllegalStateException("Unexpected character: " + ch + " at position " + i + " in line: " + line);
        }
    }

    public TokenType tokenType() {
        if ( "{}()[].,;+-*/&|<>=~".contains(currToken)){
            return TokenType.SYMBOL;
        }
        else if (isKeyword(currToken)) {
            return TokenType.KEYWORD;
        }
        else if (currToken.startsWith("\"") && currToken.endsWith("\"") && currToken.length() > 1){
            return TokenType.STRING_CONST;
        }
        else if (currToken.matches("\\d+") && Integer.parseInt(currToken) >= 0 && Integer.parseInt(currToken) <= 32767) {
            return TokenType.INT_CONST;
        }
        else {
            return TokenType.IDENTIFIER;
        }
    }

    public String keyword() {
        if (tokenType() != TokenType.KEYWORD) {
            throw new IllegalStateException("Current token is not a keyword");
        }
        return currToken; // Return the token as it appears (in lowercase)
    }

    public char symbol() {
        if (tokenType() != TokenType.SYMBOL) {
            throw new IllegalStateException("Current token is not a symbol");
        }

        // Switch on the current token
        return switch (currToken) {
            case "{" -> '{';
            case "}" -> '}';
            case "(" -> '(';
            case ")" -> ')';
            case "[" -> '[';
            case "]" -> ']';
            case "." -> '.';
            case "," -> ',';
            case ";" -> ';';
            case "+" -> '+';
            case "-" -> '-';
            case "*" -> '*';
            case "/" -> '/';
            case "&" -> '&';
            case "|" -> '|';
            case "<" -> '<';
            case ">" -> '>';
            case "=" -> '=';
            case "~" -> '~';
            default -> throw new IllegalStateException("Unexpected symbol: " + currToken);
        };
    }

    public String identifier() {
        if (tokenType() != TokenType.IDENTIFIER) {
            throw new IllegalStateException("Current token is not an identifier");
        }
        return currToken;
    }

    public int intVal() {
        if (tokenType() != TokenType.INT_CONST) {
            throw new IllegalStateException("Current token is not an integer constant");
        }
        return Integer.parseInt(currToken);
    }

    public String stringVal() {
        if (tokenType() != TokenType.STRING_CONST) {
            throw new IllegalStateException("Current token is not a string constant");
        }
        // Remove the opening and closing quotes and return the string
        return currToken.substring(1, currToken.length() - 1);
    }

    public enum TokenType {
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST
    }

    public boolean isKeyword(String input) {
        String keywords = "\\b(class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)\\b";
        return input.matches(".*" + keywords + ".*");
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}