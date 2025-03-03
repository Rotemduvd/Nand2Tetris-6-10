

import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No arguments given");
            System.exit(1);
        }

        File input = new File(args[0]);

        if (!input.exists()) {
            System.out.println("Error: Input not found.");
            System.exit(1);
        }

        try {
            if (input.isFile()) {
                compileFile(input);
            } else if (input.isDirectory()) {
                compileDirectory(input);
            } else {
                System.out.println("Error: Input must be a file or directory.");
            }
        } catch (IOException e) {
            System.out.println("Compilation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void compileFile(File file) throws IOException {
        if (!file.getName().endsWith(".jack")) {
            System.out.println("Skipping: " + file.getName());
            return;
        }

        System.out.println("Now compiling: " + file.getName());
        String outputFileName = file.getAbsolutePath().replace(".jack", ".vm");
        compile(file, new File(outputFileName));
    }

    private static void compileDirectory(File directory) throws IOException {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jack"));
        if (files == null || files.length == 0) {
            System.out.println("No .jack files in: " + directory.getName());
            return;
        }

        for (File file : files) {
            compileFile(file);
        }
    }

    private static void compile(File inputFile, File outputFile) throws IOException {
        JackTokenizer tokenizer = new JackTokenizer(inputFile.getAbsolutePath());
        try (VMWriter vmWriter = new VMWriter(new FileWriter(outputFile))) {
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer, vmWriter);

            // Start the compilation process
            compilationEngine.compileClass();

            System.out.println("Created: " + outputFile.getName());
        } catch (Exception e) {
            System.out.println("Problem with: " + inputFile.getName());
            e.printStackTrace();
        }
    }
}