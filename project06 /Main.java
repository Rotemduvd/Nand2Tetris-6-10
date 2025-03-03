import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            String filePath = args[0];
            Parser parser = new Parser(filePath);
            SymbolTable symbolTable = new SymbolTable();

            frstPass(parser,symbolTable);

            Parser sndPars = new Parser(filePath);
            //System.out.println(filePath);
            sndPass(sndPars,symbolTable,filePath);


        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void frstPass(Parser parser, SymbolTable symbolTable) {
        int lineNumber = 0;

        while (parser.hasMoreLines()) {
            parser.advance();
            String instructionType = parser.instructionType();

            if (instructionType.equals("L_INSTRUCTION")) {
                String label = parser.symbol();
                if (!symbolTable.contains(label)) {
                    symbolTable.addEntry(label, lineNumber);
                   // System.out.println("Added label: " + label + " with address: " + lineNumber); // Debugging line
                }
            } else {
                lineNumber++;
            }
        }
    }

    public static void sndPass(Parser parser, SymbolTable symbolTable, String filePath) throws IOException {
        int instrIndex = 16;
        String binaryLine;
        String outputFilePath = filePath.replace(".asm", ".hack");
        FileWriter output = new FileWriter(outputFilePath);

        while (parser.hasMoreLines()) {
            parser.advance();
            String instructionType = parser.instructionType();

            if (instructionType.equals("A_INSTRUCTION")) {
                String symbol = parser.symbol();
                int address;

                if (symbol.matches("\\d+")) {
                    address = Integer.parseInt(symbol);
                } else if (symbolTable.contains(symbol)) {
                    address = symbolTable.getAddress(symbol);
                } else {
                    address = instrIndex;
                    symbolTable.addEntry(symbol, instrIndex);
                    instrIndex++;
                }

                binaryLine = String.format("%16s", Integer.toBinaryString(address)).replace(' ', '0');
                output.write(binaryLine + "\n");
            }

            if (instructionType.equals("C_INSTRUCTION")) {
                String dest = Code.dest(parser.dest());
                String comp = Code.comp(parser.comp());
                String jump = Code.jump(parser.jump());
                binaryLine = "111" + comp + dest + jump;
                output.write(binaryLine + "\n");
            }
        }

        output.close();
    }
    public static String to16bin(Integer symbol){
        return String.format("%16s", Integer.toBinaryString(symbol)).replace(' ', '0');
    }
}