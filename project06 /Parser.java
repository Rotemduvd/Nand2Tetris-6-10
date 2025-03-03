
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private static List<String> instrOnly;
    private static int currLineInd = -1;
    public static String line;
    static String instruction;

    // Constructor: Reads the file and initializes instructions
    public Parser(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        instrOnly = new ArrayList<>();
        currLineInd = -1; // Reset current line index

        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine().trim();

            if (temp.isEmpty() || temp.startsWith("//")) {
                continue;
            }

            temp = temp.replaceAll("\\s+", ""); // Remove all whitespaces
            instrOnly.add(temp);
        }
        scanner.close();
    }

    // Reset parser to allow reprocessing
    public static void reset() {
        currLineInd = -1; // Reset the current line index
        line = null;      // Clear the current line
        instruction = null; // Clear the current instruction type
        System.out.println("Parser has been reset.");
    }

    // Checks if there are more lines to process
    public static boolean hasMoreLines() {
        //System.out.println("Current index: " + currLineInd + ", Total instructions: " + instrOnly.size());
        return currLineInd < instrOnly.size() - 1;
    }

    // Advances to the next line and updates the current line
    public static void advance() {
        if (hasMoreLines()) {
            currLineInd++;
            line = instrOnly.get(currLineInd);
        } else {
            throw new IllegalStateException("No more lines to read.");
        }
    }

    // Determines the instruction type
    public static String instructionType() {
        if (line.startsWith("@")) {
            instruction = "A_INSTRUCTION";
        } else if (line.startsWith("(")) {
            instruction = "L_INSTRUCTION";
        } else {
            instruction = "C_INSTRUCTION";
        }
        return instruction;
    }

    // Extracts the symbol from an A or L instruction
    public static String symbol() throws IllegalArgumentException {
        if (instruction.equals("A_INSTRUCTION")) {
            return line.substring(1); // Remove '@'
        } else if (instruction.equals("L_INSTRUCTION")) {
            return line.substring(1, line.length() - 1); // Remove '(' and ')'
        } else {
            throw new IllegalArgumentException("Invalid instruction type: " + instruction);
        }
    }

    // Extracts the dest part of a C instruction
    public static String dest() throws IllegalArgumentException {
        if (instruction.equals("C_INSTRUCTION")) {
            int eq = line.indexOf("=");
            if (eq != -1) {
                return line.substring(0, eq);
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("Invalid instruction type: " + instruction);
        }
    }

    // Extracts the comp part of a C instruction
    public static String comp() throws IllegalArgumentException {
        if (instruction.equals("C_INSTRUCTION")) {
            int eq = line.indexOf("=");
            int jmp = line.indexOf(";");

            if (eq == -1 && jmp == -1) {
                return line;
            } else if (eq == -1) {
                return line.substring(0, jmp);
            } else if (jmp == -1) {
                return line.substring(eq + 1);
            } else {
                return line.substring(eq + 1, jmp);
            }
        } else {
            throw new IllegalArgumentException("Invalid instruction type: " + instruction);
        }
    }

    // Extracts the jump part of a C instruction
    public static String jump() throws IllegalArgumentException {
        if (instruction.equals("C_INSTRUCTION")) {
            int jmp = line.indexOf(";");
            if (jmp == -1) {
                return null;
            } else {
                return line.substring(jmp + 1);
            }
        } else {
            throw new IllegalArgumentException("Invalid instruction type: " + instruction);
        }
    }
}