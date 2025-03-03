
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java VMTranslator source");
            System.exit(1);
        }

        String inputPath = args[0];
        File inputFile = new File(inputPath);

        try {
            CodeWriter codeWriter;

            if (inputFile.isDirectory()) {
                // Handle directory
                File[] vmFiles = inputFile.listFiles((dir, name) -> name.endsWith(".vm"));
                if (vmFiles == null || vmFiles.length == 0) {
                    throw new IllegalArgumentException("No .vm files found in the directory.");
                }

                // Determine output file name based on folder name
                String outputFilePath = inputPath.endsWith("/") ? inputPath : inputPath + "/";
                outputFilePath += new File(inputPath).getName() + ".asm";
                codeWriter = new CodeWriter(outputFilePath);

                // Check if Sys.vm exists in the directory
                boolean hasSysInit = containsSys(vmFiles);

                // Generate bootstrap code only for directories
                if (hasSysInit) {
                    codeWriter.writeBootstrap();
                }

                // Process each .vm file
                for (File vmFile : vmFiles) {
                    Parser parser = new Parser(vmFile.getPath());
                    codeWriter.setFileName(vmFile.getName());

                    while (parser.hasMoreLines()) {
                        parser.advance();
                        processCommand(parser, codeWriter);
                    }
                }
            } else {
                // Handle single .vm file
                if (!inputFile.getName().endsWith(".vm")) {
                    throw new IllegalArgumentException("Input must be a .vm file or a directory.");
                }

                String outputFilePath = inputPath.replace(".vm", ".asm");
                codeWriter = new CodeWriter(outputFilePath);
                Parser parser = new Parser(inputPath);

                // Single file: No bootstrap code is needed
                while (parser.hasMoreLines()) {
                    parser.advance();
                    processCommand(parser, codeWriter);
                }
            }

            // Close the writer
            codeWriter.close();
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static void processCommand(Parser parser, CodeWriter codeWriter) throws IOException {
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
            case "C_LABEL":
                String label = parser.arg1();
                codeWriter.writeLabel(label);
                break;
            case "C_GOTO":
                String gotoLabel = parser.arg1();
                codeWriter.writeGoto(gotoLabel);
                break;
            case "C_IF":
                String ifLabel = parser.arg1();
                codeWriter.writeIf(ifLabel);
                break;
            case "C_FUNCTION":
                String functionName = parser.arg1();
                int nArgs = parser.arg2();
                codeWriter.writeFunction(functionName, nArgs);
                break;
            case "C_CALL":
                String calledFunction = parser.arg1();
                int nArgsForCall = parser.arg2();
                codeWriter.writeCall(calledFunction, nArgsForCall);
                break;
            case "C_RETURN":
                codeWriter.writeReturn();
                break;
            default:
                throw new IllegalArgumentException("Unknown command type: " + commandType);
        }
    }

    private static boolean containsSys(File[] vmFiles) {
        for (File vmFile : vmFiles) {
            if (vmFile.getName().equals("Sys.vm")) {
                return true;
            }
        }
        return false;
    }
}