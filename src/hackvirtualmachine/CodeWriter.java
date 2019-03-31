package hackvirtualmachine;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hackvirtualmachine.Parser.CommandType;

public class CodeWriter {

    private PrintWriter outputFile;
    private List<String> output;
    private List<String> segmentList;
    private int segmentID;
    private final String PUSH =
            "@SP\n" +
            "AM=M+1\n" +
            "A=A-1\n" +
            "M=";

    public CodeWriter(String fileName) {
        try {
            outputFile = new PrintWriter(fileName + ".asm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        output = new ArrayList<>();
        segmentList = Arrays.asList("argument", "local", "this", "that",
                "pointer", "temp");
    }
    public void writePushPop(CommandType command, String segment, int index) {
        switch (command) {
            case C_PUSH:
                if (segment.equals("constant")) {
                    if (index < -1 || index > 1) {
                        output.add(PUSH + index);
                    } else {
                        output.add(
                                "@" + index + "\n" +
                                "D=A\n" +
                                PUSH + "D");
                    }
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
                    if (index == 0) {
                        output.add(
                                "@" + segment + "\n" +
                                "A=M\n" +
                                "D=M\n" +
                                PUSH + "D");
                    } else if (index == 1) {
                        output.add(
                                "@" + segment + "\n" +
                                "A=M+1\n" +
                                "D=M\n" +
                                PUSH + "D");
                    } else {
                        output.add(
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
