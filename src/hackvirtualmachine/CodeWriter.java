package hackvirtualmachine;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import hackvirtualmachine.Parser.CommandType;

public class CodeWriter {

    private PrintWriter outputFile;
    private List<String> segmentList;
    private int segmentID;
    private String staticFile;
    private final String PUSH =
            "@SP\n" +
            "AM=M+1\n" +
            "A=A-1\n" +
            "M=";
    private final String POP =
            "@SP\n" +
            "AM=M-1\n" +
            "D=M";

    public CodeWriter(String fileName) {
        try {
            outputFile = new PrintWriter(fileName.substring(0,
                    fileName.lastIndexOf(".")) + ".asm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        segmentList = Arrays.asList("argument", "local", "this", "that",
                "pointer", "temp", "static");
        staticFile = fileName.substring(fileName.lastIndexOf("/") + 1,
                fileName.length() - 2);
    }
    public void writePushPop(CommandType command, String segment, int index) {
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
                } else if (segment.equals("static")) {
                    outputFile.println(
                            "@" + staticFile + index + "\n" +
                            "D=M\n" +
                            PUSH + "D");
                } else if (segmentList.contains(segment)) {

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

                    // Temp and Pointer give exact address to access.
                    if (segmentID == 5 || segmentID == 4) {
                        outputFile.println(
                                "@" + segment + "\n" +
                                "D=M\n" +
                                PUSH + "D");
                    } else if (index == 0) {
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
        }

    }
}
