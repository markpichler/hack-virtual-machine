package hackvirtualmachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {

    private String arg1;
    private int arg2;
    private Scanner inputFile;
    private String rawLine;
    private CommandType commandType;

    public enum CommandType {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        NO_COMMAND
    }

    public Parser(String fileName) {
        try {
            inputFile = new Scanner(new File(fileName));
        } catch(FileNotFoundException e) {
            System.err.println("Error opening file " + fileName);
            System.exit(0);
        }
    }

    public boolean hasMoreCommands() {
        if (inputFile.hasNextLine()) {
            return true;
        } else {
            inputFile.close();
            return false;
        }
    }

    public void advance() {
        if (hasMoreCommands()) {
            rawLine = inputFile.nextLine();
            parse();
        }
    }

    private void parse() {
        String[] splitCommand = rawLine.split("//")[0].trim().split(" ");
        // Assumes lines have only three unique word counts: 3, 2, and 1.
        if (splitCommand.length == 3) {
            if (splitCommand[0].equals("push")) {
                commandType = CommandType.C_PUSH;
            } else if (splitCommand[0].equals("pop")) {
                commandType = CommandType.C_POP;
            }
            arg1 = splitCommand[1];
            arg2 = Integer.parseInt(splitCommand[2]);
        } else if (splitCommand.length == 2) {
            // TODO Next Lecture.
        } else if (splitCommand[0].length() != 0) {
            commandType = CommandType.C_ARITHMETIC;
            arg1 = splitCommand[0];
        } else {
            commandType = CommandType.NO_COMMAND;
        }
    }

    public String getArg1() {
        return arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public CommandType getCommandType() {
        return commandType;
    }
}
