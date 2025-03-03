

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter implements AutoCloseable{

    private FileWriter writer;

    public VMWriter(FileWriter writer) {
        this.writer = writer;
    }

    public void writePush(String segment, int index) throws IOException {
        writer.write("push " + segment + " " + index + "\n");
    }

    public void writePop(String segment, int index) throws IOException {
        writer.write("pop " + segment + " " + index + "\n");
    }

    public void writeArithmetic(String command) throws IOException {
        writer.write(command + "\n");
    }

    public void writeLabel(String label) throws IOException {
        writer.write("label " + label + "\n");
    }

    public void writeGoto(String label) throws IOException {
        writer.write("goto " + label + "\n");
    }

    public void writeIf(String label) throws IOException {
        writer.write("if-goto " + label + "\n");
    }

    public void writeCall(String name, int nArgs) throws IOException {
        writer.write("call " + name + " " + nArgs + "\n");
    }

    public void writeFunction(String name, int nVars) throws IOException {
        writer.write("function " + name + " " + nVars + "\n");
    }

    public void writeReturn() throws IOException {
        writer.write("return\n");
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
