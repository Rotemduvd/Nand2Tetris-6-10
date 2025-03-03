
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class CodeWriter {
    private final FileWriter output;
    private final String fileName;
    private int labelCount = 0;

    public CodeWriter(String filePath) throws IOException {
        String outputFilePath = filePath.replace(".vm", ".asm");
        output = new FileWriter(outputFilePath);
        fileName = Paths.get(filePath).getFileName().toString().replaceAll("\\.vm$", "");
    }

    public void writeArithmetic(String command) throws IOException {
        output.write("// " + command + "\n");

        if (command.equals("add")) {
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("A=A-1\n");
            output.write("A=A-1\n");
            output.write("D=M\n");
            output.write("A=A+1\n");
            output.write("D=D+M\n");
            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("M=M-1\n");
            output.write("A=M\n");
            output.write("M=D\n");
            output.write("@SP\n");
            output.write("M=M+1\n");
        } else if (command.equals("sub")) {
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("A=A-1\n");
            output.write("A=A-1\n");
            output.write("D=M\n");
            output.write("A=A+1\n");
            output.write("D=D-M\n");
            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("M=M-1\n");
            output.write("A=M\n");
            output.write("M=D\n");
            output.write("@SP\n");
            output.write("M=M+1\n");
        } else if (command.equals("neg")) {
            output.write("@SP\n");
            output.write("A=M-1\n");
            output.write("M=-M\n");
        } else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
            String jumpType = switch (command) {
                case "eq" -> "JEQ";
                case "gt" -> "JGT";
                case "lt" -> "JLT";
                default -> throw new IllegalArgumentException("Invalid command: " + command);
            };

            String trueLabel = "TRUE_" + labelCount;
            String endLabel = "END_" + labelCount;
            labelCount++;

            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("A=M\n");
            output.write("D=M\n");
            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("A=M\n");
            output.write("D=M-D\n");
            output.write("@" + trueLabel + "\n");
            output.write("D;" + jumpType + "\n");
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("M=0\n");
            output.write("@" + endLabel + "\n");
            output.write("0;JMP\n");
            output.write("(" + trueLabel + ")\n");
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("M=-1\n");
            output.write("(" + endLabel + ")\n");
            output.write("@SP\n");
            output.write("M=M+1\n");
        } else if (command.equals("and") || command.equals("or")) {
            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("A=M\n");
            output.write("D=M\n");
            output.write("@SP\n");
            output.write("M=M-1\n");
            output.write("A=M\n");

            String op = command.equals("and") ? "&" : "|";
            output.write("M=D" + op + "M\n");

            output.write("@SP\n");
            output.write("M=M+1\n");
        } else if (command.equals("not")) {
            output.write("@SP\n");
            output.write("A=M-1\n");
            output.write("M=!M\n");
        }
    }

    public void writePushPop(String command, String segment, Integer index) throws IOException {
        output.write("// " + command + " " + segment + " " + index + "\n");

        if (command.equals("C_PUSH")) {
            if (segment.equals("constant")) {
                output.write("@" + index + "\n");
                output.write("D=A\n");
            } else if (segment.equals("static")) {
                output.write("@" + fileName + index + "\n");
                output.write("D=M\n");
            } else if (segment.equals("pointer")) {
                output.write("@" + (index == 0 ? "THIS" : "THAT") + "\n");
                output.write("D=M\n");
            } else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
                output.write("@" + shortenSeg(segment) + "\n");
                output.write("D=M\n");
                output.write("@" + index + "\n");
                output.write("A=D+A\n");
                output.write("D=M\n");
            } else if (segment.equals("temp")) {
                output.write("@" + (5 + index) + "\n");
                output.write("D=M\n");
            }

            // Push the value onto the stack
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("M=D\n");
            output.write("@SP\n");
            output.write("M=M+1\n");

        } else if (command.equals("C_POP")) {
            if (segment.equals("static")) {
                output.write("@" + fileName + index + "\n");
                output.write("D=A\n");
                output.write("@R13\n");
                output.write("M=D\n");
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@R13\n");
                output.write("A=M\n");
                output.write("M=D\n");
            } else if (segment.equals("pointer")) {
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@" + (index == 0 ? "THIS" : "THAT") + "\n");
                output.write("M=D\n");
            } else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@" + shortenSeg(segment) + "\n");
                output.write("A=M\n");
                output.write("D=D+A\n");
                output.write("@R13\n");
                output.write("M=D\n");
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@R13\n");
                output.write("A=M\n");
                output.write("M=D\n");
            } else if (segment.equals("temp")) {
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@5\n");
                output.write("D=D+A\n");
                output.write("@R13\n");
                output.write("M=D\n");
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@R13\n");
                output.write("A=M\n");
                output.write("M=D\n");
            }
        }
    }


    private String shortenSeg(String segment){
        return switch (segment) {
            case "local" -> "LCL";
            case "argument" -> "ARG";
            case "this" -> "THIS";
            case "that" -> "THAT";
            default -> throw new IllegalArgumentException("Invalid segment: " + segment);
        };
    }

    public void close() throws IOException {
        if (output != null) {
            output.close();
        }
    }
}
