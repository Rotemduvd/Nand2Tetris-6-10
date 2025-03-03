

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please provide the path to a .vm file or a directory containing .vm files");
            System.exit(1);
        }

        String inputPath = args[0];
        File inputFile = new File(inputPath);

        try {
            if (inputFile.isDirectory()) {
                // Get all .vm files in the directory
                File[] vmFiles = inputFile.listFiles((dir, name) -> name.endsWith(".vm"));
                if (vmFiles == null || vmFiles.length == 0) {
                    System.err.println("No .vm files found in the directory.");
                    return;
                }

                // Create output file name as dir.asm
                String outputFilePath = inputFile.getAbsolutePath() + "/" + inputFile.getName() + ".asm";
                CodeWriter codeWriter = new CodeWriter(outputFilePath);

                for (File vmFile : vmFiles) {
                    Parser parser = new Parser(vmFile.getAbsolutePath());
                    processFile(parser, codeWriter);
                }

                codeWriter.close();
            } else if (inputFile.isFile() && inputPath.endsWith(".vm")) {
                // Single .vm file
                String outputFilePath = inputFile.getAbsolutePath().replace(".vm", ".asm");
                Parser parser = new Parser(inputPath);
                CodeWriter codeWriter = new CodeWriter(outputFilePath);

                processFile(parser, codeWriter);
                codeWriter.close();
            } else {
                System.err.println("Invalid input. Provide a .vm file or a directory containing .vm files.");
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static void processFile(Parser parser, CodeWriter codeWriter) throws IOException {
        while (parser.hasMoreLines()) {
            parser.advance(); // Move to the next command
            String commandType = parser.commandType();

            switch (commandType) {
                case "C_PUSH":
                case "C_POP":
                    String segment = parser.arg1();
                    int index = parser.arg2();
                    codeWriter.writePushPop(commandType, segment, index);
                    break;
                case "C_ARITHMETIC":
                    String arithmeticCommand = parser.arg1();
                    codeWriter.writeArithmetic(arithmeticCommand);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + commandType);
            }
        }
    }
}