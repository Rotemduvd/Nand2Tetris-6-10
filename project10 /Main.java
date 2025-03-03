
import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: JackAnalyzer <input file or directory>");
            return;
        }
        String inputPath = args[0];
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.out.println("Error: Input file or directory does not exist.");
            return;
        }

        if (inputFile.isDirectory()) {
            // Process all .jack files in the directory
            File[] jackFiles = inputFile.listFiles((dir, name) -> name.endsWith(".jack"));
            if (jackFiles != null && jackFiles.length > 0) {
                for (File jackFile : jackFiles) {
                    analyze(jackFile);
                }
            } else {
                System.out.println("No .jack files found in the directory.");
            }
        } else if (inputFile.isFile() && inputFile.getName().endsWith(".jack")) {
            // Process a single .jack file
            analyze(inputFile);
        } else {
            System.out.println("Error: Invalid input. Please provide a .jack file or a directory containing .jack files.");
        }
    }

    private static void analyze(File jackFile) {
        System.out.println("Processing file: " + jackFile.getName());
        String outputFileName = jackFile.getAbsolutePath().replace(".jack", ".xml");

        try (
                JackTokenizer tokenizer = new JackTokenizer(jackFile.getAbsolutePath());
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))
        ) {
            CompilationEngine engine = new CompilationEngine(tokenizer, writer);
            engine.compileClass();
            System.out.println("Processed: " + jackFile.getName() + " -> " + outputFileName);

        } catch (IOException e) {
            System.err.println("Error processing file: " + jackFile.getName());
            e.printStackTrace();
        }
    }
}