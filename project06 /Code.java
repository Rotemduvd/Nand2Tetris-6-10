
import java.io.File;
public class Code {

    public static String dest(String parDest){
        if (parDest == null || parDest.isEmpty()) {
            return "000";
        }
        
        String d1 = "0";
        String d2 = "0";
        String d3 = "0";

        if (parDest.contains("A")) {
            d1 = "1";
        }
        if (parDest.contains("D")) {
            d2 = "1";
        }
        if (parDest.contains("M")) {
            d3 = "1";
        }

        return d1 + d2 + d3;
    }

    public static String jump(String parJump) {
        if (parJump == null || parJump.isEmpty()) {
            return "000";
        }

        switch (parJump) {
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            case "JMP":
                return "111";
            default:
                return "000";
        }
    }

    public static String comp(String parComp) {
        if (parComp == null || parComp.isEmpty()) {
            return "0000000"; // Default for invalid or null input
        }

        switch (parComp) {
            case "0":
                return "0101010";
            case "1":
                return "0111111";
            case "-1":
                return "0111010";
            case "D":
                return "0001100";
            case "A":
                return "0110000";
            case "M":
                return "1110000";
            case "!D":
                return "0001101";
            case "!A":
                return "0110001";
            case "!M":
                return "1110001";
            case "-D":
                return "0001111";
            case "-A":
                return "0110011";
            case "-M":
                return "1110011";
            case "D+1":
                return "0011111";
            case "A+1":
                return "0110111";
            case "M+1":
                return "1110111";
            case "D-1":
                return "0001110";
            case "A-1":
                return "0110010";
            case "M-1":
                return "1110010";
            case "D+A":
                return "0000010";
            case "D+M":
                return "1000010";
            case "D-A":
                return "0010011";
            case "D-M":
                return "1010011";
            case "A-D":
                return "0000111";
            case "M-D":
                return "1000111";
            case "D&A":
                return "0000000";
            case "D&M":
                return "1000000";
            case "D|A":
                return "0010101";
            case "D|M":
                return "1010101";
            default:
                return "0000000";
        }
    }

    
}
