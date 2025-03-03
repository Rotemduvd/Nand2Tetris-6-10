
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class CodeWriter {
    private final FileWriter output;
    private String fileName;
    private int labelCount = 0;
    private int callLabelCount = 0;


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
            //output.write("@SP\n");
           // output.write("M=M+1\n");
        }
    }

    public void writePushPop(String command, String segment, int index) throws IOException {
        output.write("// " + command + " " + segment + " " + index + "\n");

        if (command.equals("C_PUSH")) {
            if (segment.equals("constant")) {
                // Push constant value
                output.write("@" + index + "\n");
                output.write("D=A\n");
            } else if (segment.equals("static")) {
                // Push static variable
                output.write("@" + fileName + "." + index + "\n");
                output.write("D=M\n");
            } else if (segment.equals("pointer")) {
                // Push pointer (THIS/THAT)
                String target = (index == 0) ? "THIS" : "THAT";
                output.write("@" + target + "\n");
                output.write("D=M\n");
            } else if (segment.equals("temp")) {
                // Push temp (R5-R12)
                output.write("@" + (5 + index) + "\n");
                output.write("D=M\n");
            } else if (segment.equals("argument")) {
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@ARG\n");
                output.write("A=M\n");  // Dereference ARG
                output.write("D=D+A\n");  // Add offset
                output.write("A=D\n");  // Final address
                output.write("D=M\n");  // Get value at address

            } else {
                // Push local, argument, this, or that
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@" + shortenSeg(segment) + "\n");
                output.write("A=M\n");     // Dereference base pointer
                output.write("D=D+A\n");   // Add offset
                output.write("A=D\n");     // Get the target address
                output.write("D=M\n");     // Read value at target address
            }
            commonPush();

        } else if (command.equals("C_POP")) {
            if (segment.equals("static")) {
                // Pop to static variable
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@" + fileName + "." + index + "\n");
                output.write("M=D\n");
            } else if (segment.equals("pointer")) {
                // Pop to pointer (THIS/THAT)
                String target = (index == 0) ? "THIS" : "THAT";
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@" + target + "\n");
                output.write("M=D\n");
            } else if (segment.equals("temp")) {
                // Pop to temp (R5-R12)
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@" + (5 + index) + "\n");
                output.write("M=D\n");
            } else if (segment.equals("that")) {
                // Pop to `that` with pointer updates
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@THAT\n");
                output.write("A=M\n");
                output.write("D=D+A\n");
                output.write("@THAT\n");
                output.write("M=D\n"); // Update `THAT` pointer
                output.write("@SP\n");
                output.write("M=M-1\n");
                output.write("A=M\n");
                output.write("D=M\n");
                output.write("@THAT\n");
                output.write("A=M\n");
                output.write("M=D\n");
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@THAT\n");
                output.write("A=M\n");
                output.write("D=A-D\n");
                output.write("@THAT\n");
                output.write("M=D\n"); // Restore `THAT`
            } else if (segment.equals("argument")) {
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@ARG\n");
                output.write("A=M\n");  // Dereference ARG
                output.write("D=D+A\n");  // Add offset
                output.write("@R13\n");
                output.write("M=D\n");  // Store address in R13
                output.write("@SP\n");
                output.write("M=M-1\n");  // Decrement stack pointer
                output.write("A=M\n");
                output.write("D=M\n");  // Get value from stack
                output.write("@R13\n");
                output.write("A=M\n");
                output.write("M=D\n");  // Store value at ARG + index
            } else {
                // Pop to local, argument, this, or that
                output.write("@" + index + "\n");
                output.write("D=A\n");
                output.write("@" + shortenSeg(segment) + "\n");
                output.write("A=M\n");     // Dereference base pointer
                output.write("D=D+A\n");   // Add offset
                output.write("@R13\n");
                output.write("M=D\n");     // Store target address in R13
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

    void writeLabel(String label) throws IOException {
        output.write("("+ label + ")\n");
    }

    void writeGoto(String label) throws IOException {
        output.write("@" + label + "\n");
        output.write("0;JMP\n");
    }

    void writeIf(String label) throws IOException {
        output.write("@SP\n");
        output.write("M=M-1\n");
        output.write("A=M\n");
        output.write("D=M\n");
        output.write("@" + label + "\n");
        output.write("D;JNE\n");
    }

    public void writeFunction(String function, Integer nArgs) throws IOException {
        //function entry point
        output.write("(" + function + ")\n");

        for (int i = 0; i < nArgs; i++) {
            output.write("@SP\n");
            output.write("A=M\n");
            output.write("M=0\n");
            output.write("@SP\n");
            output.write("M=M+1\n");
        }
    }

    void writeCall(String function, Integer nArgs) throws IOException {
        String returnAddress = "RETURN_" + callLabelCount++;

        // push return address
        output.write("// push return address\n");
        output.write("@" + returnAddress + "\n");
        output.write("D=A\n");
        commonPush();

        // Push LCL
        output.write("@LCL\n");
        output.write("D=M\n");
        commonPush();

        // Push ARG
        output.write("@ARG\n");
        output.write("D=M\n");
        commonPush();

        // Push THIS
        output.write("@THIS\n");
        output.write("D=M\n");
        commonPush();

        // Push THAT
        output.write("@THAT\n");
        output.write("D=M\n");
        commonPush();


        // Reposition ARG
        output.write("// ARG = SP - 5 - nArgs\n");
        output.write("@SP\n");
        output.write("D=M\n");
        output.write("@5\n");
        output.write("D=D-A\n");
        output.write("@" + nArgs + "\n");
        output.write("D=D-A\n");
        output.write("@ARG\n");
        output.write("M=D\n");

        // Reposition LCL
        output.write("// LCL = SP\n");
        output.write("@SP\n");
        output.write("D=M\n");
        output.write("@LCL\n");
        output.write("M=D\n");

        // Goto function
        output.write("// goto " + function + "\n");
        output.write("@" + function + "\n");
        output.write("0;JMP\n");

        // Declare return address label
        output.write("(" + returnAddress + ")\n");
    }

    void writeReturn() throws IOException {
            output.write("// return\n");

            // FRAME = LCL
            output.write("@LCL\n");
            output.write("D=M\n");
            output.write("@FRAME\n");
            output.write("M=D\n");

            // RET = *(FRAME - 5)
            output.write("@5\n");
            output.write("A=D-A\n"); // A = FRAME - 5
            output.write("D=M\n");   // D = RET
            output.write("@RET\n");
            output.write("M=D\n");

            // *ARG = pop()
            output.write("@SP\n");
            output.write("AM=M-1\n"); // SP--
            output.write("D=M\n");    // D = pop()
            output.write("@ARG\n");
            output.write("A=M\n");
            output.write("M=D\n");    // *ARG = return value

            // SP = ARG + 1
            output.write("@ARG\n");
            output.write("D=M+1\n");  // D = ARG + 1
            output.write("@SP\n");
            output.write("M=D\n");    // SP = ARG + 1

            // Restore THAT = *(FRAME - 1)
            output.write("@FRAME\n");
            output.write("AM=M-1\n"); // FRAME = FRAME - 1
            output.write("D=M\n");
            output.write("@THAT\n");
            output.write("M=D\n");    // THAT = *(FRAME - 1)

            // Restore THIS = *(FRAME - 2)
            output.write("@FRAME\n");
            output.write("AM=M-1\n"); // FRAME = FRAME - 2
            output.write("D=M\n");
            output.write("@THIS\n");
            output.write("M=D\n");    // THIS = *(FRAME - 2)

            // Restore ARG = *(FRAME - 3)
            output.write("@FRAME\n");
            output.write("AM=M-1\n"); // FRAME = FRAME - 3
            output.write("D=M\n");
            output.write("@ARG\n");
            output.write("M=D\n");    // ARG = *(FRAME - 3)

            // Restore LCL = *(FRAME - 4)
            output.write("@FRAME\n");
            output.write("AM=M-1\n"); // FRAME = FRAME - 4
            output.write("D=M\n");
            output.write("@LCL\n");
            output.write("M=D\n");    // LCL = *(FRAME - 4)

            // Goto RET
            output.write("@RET\n");
            output.write("A=M\n");
            output.write("0;JMP\n");  // goto RET
        }


    private void commonPush() throws IOException {
        output.write("@SP\n");
        output.write("A=M\n");
        output.write("M=D\n");
        output.write("@SP\n");
        output.write("M=M+1\n");
    }

    public void close() throws IOException {
        if (output != null) {
            output.close();
        }
    }

    public void setFileName(String filePath) {
        fileName = Paths.get(filePath).getFileName().toString().replaceAll("\\.vm$", "");
    }

    public void writeBootstrap() throws IOException {
        output.write("// Bootstrap code\n");

        // SP = 256
        output.write("@256\n");
        output.write("D=A\n");
        output.write("@SP\n");
        output.write("M=D\n");

        // Call Sys.init
        writeCall("Sys.init", 0);
    }
}
