package hackvirtualmachine;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import hackvirtualmachine.Parser.CommandType;

/**
 * A CodeWriter that translates parsed Hack VM code to Hack assembly code and
 * writes the translated code to a file.
 *
 * @author Mark Pichler
 */
public class CodeWriter {

    // TODO Look into how StringBuilder can possibly speed things up

    private PrintWriter outputFile;
    private List<String> segmentList;
    private int segmentID;
    private String staticFile;
    private int logicalCount;
    private final String PUSH =
            "@SP\n" +
            "AM=M+1\n" +
            "A=A-1\n" +
            "M=";
    private final String POP =
            "@SP\n" +
            "AM=M-1\n" +
            "D=M\n" +
            "@R13\n" +
            "M=D\n";
    private final String GRAB_TEMP =
            "@R13\n" +
            "D=M\n";
    private final String BINARY_OP =
            "@SP\n" +
            "AM=M-1\n" +
            "D=M\n" +
            "A=A-1\n" +
            "M=M";
    private final String UNARY_OP =
            "@SP\n" +
            "A=M-1\n" +
            "M=";

    /**
     * Creates a new CodeWriter.  Initializes a new PrintWriter (outputFile)
     * and parses the name of the file from its full path and extension.
     * Assumes the output file and location are the same as the original VM
     * code file.
     *
     * @param fileName full path of output file
     */
    public CodeWriter(String fileName) {
        // Assumes NON-Windows filepath format
        try {
            outputFile = new PrintWriter(fileName.substring(0,
                    fileName.lastIndexOf(".")) + ".asm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        logicalCount = 0;
        segmentList = Arrays.asList("argument", "local", "this", "that",
                "pointer", "temp", "static");
        staticFile = fileName.substring(fileName.lastIndexOf("/") + 1,
                fileName.length() - 2);
    }

    /**
     * Appends an infinite loop at the end of the ASM file to prevent NOP
     * slides.  Closes the PrintWriter.
     */
    public void close() {
        outputFile.println(
                "(END)\n" +
                "@END\n" +
                "0;JMP");
        outputFile.close();
    }

    /**
     * Manages the translation of Push and Pop commands.
     *
     * @param command type of command to be translated
     * @param segment memory segment to be operated on
     * @param index memory offset of segment
     */
    public void writePushPop(CommandType command, String segment, int index) {

        // TODO Refactor how branch is determined
        segmentID = segmentList.indexOf(segment);

        switch (segmentID) {
            case 0:
                segment = "ARG";
                break;
            case 1:
                segment = "LCL";
                break;
            case 2:
                segment = "THIS";
                break;
            case 3:
                segment = "THAT";
                break;
            case 4:
                segment = index == 0 ? "THIS" : "THAT";
                break;
            case 5:
                segment = "R" + (5 + index);
        }

        switch (command) {
            case C_PUSH:
                if (segment.equals("constant")) {
                    if (index > -1 && index < 1) {
                        outputFile.println(PUSH + index);
                    } else {
                        outputFile.println(
                                "@" + index + "\n" +
                                "D=A\n" +
                                PUSH + "D");
                    }
                // TODO Combine Static with Temp and Pointer by pre-processing
                //      static's segment string.
                } else if (segment.equals("static")) {
                    outputFile.println(
                            "@" + staticFile + index + "\n" +
                            "D=M\n" +
                            PUSH + "D");

                // Temp and Pointer give exact address to access.
                } else if (segmentID == 5 || segmentID == 4) {
                    outputFile.println(
                            "@" + segment + "\n" +
                            "D=M\n" +
                            PUSH + "D");
                } else {
                    if (index == 0) {
                        outputFile.println(
                                "@" + segment + "\n" +
                                "A=M\n" +
                                "D=M\n" +
                                PUSH + "D");
                    } else if (index == 1) {
                        outputFile.println(
                                "@" + segment + "\n" +
                                "A=M+1\n" +
                                "D=M\n" +
                                PUSH + "D");
                    } else {
                        outputFile.println(
                                "@" + index + "\n" +
                                "D=A\n" +
                                "@" + segment + "\n" +
                                "A=M+D\n" +
                                "D=M\n" +
                                PUSH + "D");
                    }
                }
                break;
            case C_POP:
                // TODO Combine Static with Temp and Pointer by pre-processing
                //      static's segment string.
                if (segment.equals("static")) {
                    outputFile.println(
                            POP +
                            GRAB_TEMP +
                            "@" + staticFile + index + "\n" +
                            "M=D");
                // Temp and Pointer give exact address to access.
                } else if (segmentID == 5 || segmentID == 4) {
                    outputFile.println(
                            POP +
                            GRAB_TEMP +
                            "@" + segment + "\n" +
                            "M=D");
                } else {
                    if (index == 0) {
                        outputFile.println(
                                POP +
                                GRAB_TEMP +
                                "@" + segment + "\n" +
                                "A=M\n" +
                                "M=D");
                    } else if (index == 1) {
                        outputFile.println(
                                POP +
                                GRAB_TEMP +
                                "@" + segment + "\n" +
                                "A=M+1\n" +
                                "M=D");
                    } else {
                        outputFile.println(
                                POP +
                                "@" + index + "\n" +
                                "D=A\n" +
                                "@" + segment + "\n" +
                                "D=M+D\n" +
                                "@R14\n" +
                                "M=D\n" +
                                GRAB_TEMP +
                                "@R14\n" +
                                "A=M\n" +
                                "M=D");
                    }
                }
                break;
        }
    }

    /**
     * Manages the translation of Hack VM arithmetic and logical commands.
     *
     * @param command VM arithmetic or logical command to be translated
     */
    public void writeArithmetic(String command) {
        command = command.toUpperCase();
        switch (command) {
            case "ADD":
                outputFile.println(BINARY_OP + "+D");
                break;
            case "SUB":
                outputFile.println(BINARY_OP + "-D");
                break;
            case "AND":
                outputFile.println(BINARY_OP + "&D");
                break;
            case "OR":
                outputFile.println(BINARY_OP + "|D");
                break;
            case "NEG":
                outputFile.println(UNARY_OP + "-M");
                break;
            case "NOT":
                outputFile.println(UNARY_OP + "!M");
                break;
            // TODO TURN INTO SUB ROUTINE. TOO MUCH REPEATED CODE
            default:
                outputFile.println(
                        "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@" + command + logicalCount + "\n" +
                        "D;J" + command + "\n" +
                        "D=0\n" +
                        "@END" + logicalCount + "\n" +
                        "0;JMP\n" +
                        "(" + command + logicalCount + ")\n" +
                        "D=-1\n" +
                        "(END" + logicalCount + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=D");
                logicalCount++;
                break;
        }
    }

    /**
     * Manages the translation of Hack VM label commands.
     *
     * @param label name of label to be translated
     */
    public void writeLabel(String label) {
        outputFile.println("(" + label + ")");
    }

    /**
     * Manages the translation of Hack VM goto commands.
     *
     * @param label name of label to go to
     */
    public void writeGoto(String label) {
        outputFile.println(
                "@" + label + "\n" +
                "0;JMP"
        );
    }

    /**
     * Manages the translation of Hack VM if-goto commands.
     *
     * @param label name of label to go to if condition is met
     */
    public void writeIf(String label) {
        outputFile.println(
                "@SP\n" +
                "D=M\n" +
                "@" + label + "\n" +
                "D;JNE");
    }

    /**
     * Manages the translation of Hack VM function commands.  This task entails
     * creating a unique function label, pushing numVars 0's onto the stack,
     * and setting the LCL pointer to the beginning of that sequence of zeros.
     *
     * @param functionName name of function
     * @param numVars number of arguments the function can receive
     */
    public void writeFunction(String functionName, int numVars) {
        writeLabel(functionName);
        outputFile.println(
                "@SP\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D"
        );
        for (int i = 1; i <= numVars; i++) {
            writePushPop(CommandType.C_PUSH, "constant", 0);
        }
    }

    /**
     * Manages the translation of Hack VM return commands.  This task entails
     * popping the most recent value to ARG[0] (the return value), resetting
     * the LCL, ARG, THIS, and THAT pointers to their values in the prior stack
     * frame, and writing a jump to the address just after the call command in
     * the caller function.
     */
    public void writeReturn() {
        // Write return value to ARG[0]
        writePushPop(CommandType.C_POP, "argument", 0);
        // Set SP to ARG[1]
        outputFile.println(
                "@ARG\n" +
                "D=M+1\n" +
                "@SP\n" +
                "M=D"
        );
        // Reset THAT pointer and decrement LCL
        outputFile.println(
                "@LCL\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@THAT\n" +
                "M=D"
        );
        // Reset THIS pointer and decrement LCL
        outputFile.println(
                "@LCL\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@THIS\n" +
                "M=D"
        );
        // Reset ARG pointer and decrement LCL
        outputFile.println(
                "@LCL\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@ARG\n" +
                "M=D"
        );
        // Save return address to temp R13 before resetting LCL
        outputFile.println(
                "@LCL\n" +
                "A=M-1\n" +
                "A=A-1\n" +
                "D=M\n" +
                "@R13\n" +
                "M=D"
        );
        // Reset LCL pointer
        outputFile.println(
                "@LCL\n" +
                "A=M-1\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D"
        );
        // Write a jump command to go back to the return address
        outputFile.println(
                "@R13\n" +
                "A=M\n" +
                "0;JMP"
        );
    }

    /**
     * Manages the translation of Hack VM call commands.  This task entails
     * saving return address (the address right after the call command was
     * made), the values of LCL, ARG, THIS, and THAT, and setting the ARG
     * pointer to the SP memory address before the call was made minus
     * numArgs.
     *
     * @param functionName name of function to call
     * @param numArgs number of arguments to pass to function
     */
    public void writeCall(String functionName, int numArgs) {
        // Save the return address and increment SP
        outputFile.println(
                "@RETURN" + logicalCount + "\n" +
                "D=A\n" +
                PUSH + "D"
        );
        // Save current LCL address and increment SP
        outputFile.println(
                "@LCL\n" +
                "D=M\n" +
                PUSH + "D"
        );
        // Save current ARG address and increment SP
        outputFile.println(
                "@ARG\n" +
                "D=M\n" +
                PUSH + "D"
        );
        // Save current THIS address and increment SP
        outputFile.println(
                "@THIS\n" +
                "D=M\n" +
                PUSH + "D"
        );
        // Save current THAT address and increment SP
        outputFile.println(
                "@THAT\n" +
                "D=M\n" +
                PUSH + "D"
        );

        // Set ARG pointer to SP - (5 + numArgs)
        outputFile.println(
                "@" + (5 + numArgs) + "\n" +
                "D=A\n" +
                "@SP\n" +
                "D=M-D\n" +
                "@ARG\n" +
                "M=D"
        );

        // Write goto function
        writeGoto(functionName);

        // Write return address label
        writeLabel("RETURN" + logicalCount);
        logicalCount++;
    }
}
