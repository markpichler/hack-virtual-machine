package hackvirtualmachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A Parser that manages the traversal, cleaning, and parsing of each line of
 * a given Hack Virtual Machine code file.  The Hack VM language is based on an
 * implementation of a Stack Machine.
 *
 * @author Mark Pichler
 */
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
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL,
        NO_COMMAND
    }

    /**
     * Creates a new Parser.  Attempts to instantiate a new Scanner, inputFile,
     * with the desired VM file as input.
     *
     * @param fileName path of the VM file to be translated
     */
    public Parser(String fileName) {
        try {
            inputFile = new Scanner(new File(fileName));
        } catch(FileNotFoundException e) {
            System.err.println("Error opening file " + fileName);
            System.exit(0);
        }
    }

    /**
     * Determines if there are more lines to be parsed in the VM code.
     *
     * @return true if more lines, false otherwise
     */
    public boolean hasMoreCommands() {
        if (inputFile.hasNextLine()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parses the current line and advances the parser one line further.
     */
    public void advance() {
        if (hasMoreCommands()) {
            rawLine = inputFile.nextLine();
            parse();
        }
    }

    /**
     * Parses the current line of VM code.  Comments are ignored.  The parsed
     * elements are commands, memory segments, constants, and memory indices.
     * Commands and segments are stored in arg1 while constants and memory
     * indices are stored in arg2.
     */
    private void parse() {
        String[] splitCommand = rawLine.split("//")[0].trim().split(
                " ");
        // Assumes lines have only three unique word counts: 3, 2, and 1.
        if (splitCommand.length == 3) {
            if (splitCommand[0].equals("push")) {
                commandType = CommandType.C_PUSH;
            } else if (splitCommand[0].equals("pop")) {
                commandType = CommandType.C_POP;
            } else if (splitCommand[0].equals("function")) {
                commandType = CommandType.C_FUNCTION;
            } else if (splitCommand[0].equals("call")) {
                commandType = CommandType.C_CALL;
            }
            arg1 = splitCommand[1];
            arg2 = Integer.parseInt(splitCommand[2]);
        } else if (splitCommand.length == 2) {
            if (splitCommand[0].equals("label")) {
                commandType = CommandType.C_LABEL;
            } else if (splitCommand[0].equals("goto")) {
                commandType = CommandType.C_GOTO;
            } else if (splitCommand[0].equals("if-goto")) {
                commandType = CommandType.C_IF;
            }
            arg1 = splitCommand[1];
        } else if (splitCommand[0].length() != 0) {
            if (splitCommand[0].equals("return")) {
                commandType = CommandType.C_RETURN;
            } else {
                commandType = CommandType.C_ARITHMETIC;
            }
            arg1 = splitCommand[0];
        } else {
            commandType = CommandType.NO_COMMAND;
        }
    }

    /**
     * @return current arg1 (commands and memory segments)
     */
    public String getArg1() {
        return arg1;
    }

    /**
     * @return current arg2 (constants and memory indices)
     */
    public int getArg2() {
        return arg2;
    }

    /**
     * @return current command type parsed of VM command
     */
    public CommandType getCommandType() {
        return commandType;
    }
}
