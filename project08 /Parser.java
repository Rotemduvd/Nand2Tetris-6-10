

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private  List<String> CmdOnly;
    private  int currLineInd;
    public String line;
    private String command;

    // Constructor: Reads the file and initializes commands
    public Parser(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        CmdOnly = new ArrayList<>();
        currLineInd = -1; // Reset current line index

        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine().trim();

            // Skip empty lines and comments
            if (temp.isEmpty() || temp.startsWith("//")) {
                continue;
            }

            // Remove inline comments
            int commentIndex = temp.indexOf("//");
            if (commentIndex != -1) {
                temp = temp.substring(0, commentIndex).trim();
            }

            CmdOnly.add(temp);
        }
        scanner.close();
    }

    // Reset parser to allow reprocessing
    public void reset() {
        currLineInd = -1; // Reset the current line index
        line = null;      // Clear the current line
        command = null; // Clear the current command type
        System.out.println("Parser has been reset.");
    }

    // Checks if there are more lines to process
    public boolean hasMoreLines() {
        return currLineInd < CmdOnly.size() - 1;
    }

    // Advances to the next line and updates the current line
    public void advance() {
        if (hasMoreLines()) {
            currLineInd++;
            line = CmdOnly.get(currLineInd);
            //System.out.println("Processing command: " + line);
        } else {
            throw new IllegalStateException("No more lines to read.");
        }
    }

    public String commandType(){
        if (line.startsWith("push")){
            command = "C_PUSH";
        } else if (line.startsWith("pop")) {
            command = "C_POP";
        } else if (line.startsWith("goto")) {
            command = "C_GOTO";
        } else if (line.startsWith("if")) {
            command = "C_IF";
        } else if (line.startsWith("label")) {
            command = "C_LABEL";
        } else if (line.startsWith("call")) {
            command = "C_CALL";
        } else if (line.startsWith("function")) {
            command = "C_FUNCTION";
        } else if (line.startsWith("return")) {
            command = "C_RETURN";
        }else {
            command = "C_ARITHMETIC";
        }
        return command;
    }

    public String arg1() throws IllegalArgumentException{
        if (command.equals("C_RETURN")) {
            throw new IllegalArgumentException("C_RETURN command not allowed");
        }
        if (command.equals("C_ARITHMETIC")){
            return line;
        } else {
            String[] args = line.split(" ");
            return args[1];
        }
    }

    public Integer arg2() throws IllegalArgumentException{
        if (command.equals("C_PUSH") || command.equals("C_POP") || command.equals("C_FUNCTION") || command.equals("C_CALL")){
            String[] args = line.split(" ");
            return Integer.parseInt(args[2]);
        } else {
            throw new IllegalArgumentException("Invalid command type: " + command);
        }
    }
}