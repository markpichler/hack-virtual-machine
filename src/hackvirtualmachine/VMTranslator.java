package hackvirtualmachine;

import hackvirtualmachine.Parser.CommandType;

/**
 * Driver program for the Hack Virtual Machine Translator.
 *
 * @author Mark Pichler
 */
public class VMTranslator {

    private Parser parser;
    private CodeWriter codeWriter;
    private CommandType commandType;
    private String arg1;
    private int arg2;
    private String[] fileNames;
    private int fileCount;
    private int currFile;

    /**
     * Initializes the Parser and CodeWriter.
     *
     * @param fileNames array of full path VM file names
     */
    public VMTranslator(String[] fileNames) {
        this.fileNames = fileNames;
        fileCount = fileNames.length;
        currFile = 1;
        parser = new Parser(fileNames[0]);
        codeWriter = new CodeWriter(fileNames[0]);
    }

    /**
     * Starts the translation.  Only call once.  The translated file is created
     * in the same folder as the input VM file after calling this method.
     */
    public void start() {

        while (true) {

            if (!parser.hasMoreCommands() && currFile < fileCount) {
                parser = new Parser(fileNames[currFile]);
                codeWriter.setFileName(fileNames[currFile]);
                currFile++;
            } else if (!parser.hasMoreCommands()) {
                break;
            }

            parser.advance();
            commandType = parser.getCommandType();
            arg1 = parser.getArg1();
            arg2 = parser.getArg2();

            switch (commandType) {
                case C_PUSH:
                case C_POP:
                    codeWriter.writePushPop(commandType, arg1, arg2);
                    break;
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(arg1);
                    break;
                case C_LABEL:
                    codeWriter.writeLabel(arg1);
                    break;
                case C_GOTO:
                    codeWriter.writeGoto(arg1);
                    break;
                case C_IF:
                    codeWriter.writeIf(arg1);
                    break;
                case C_FUNCTION:
                    codeWriter.writeFunction(arg1, arg2);
                    break;
                case C_RETURN:
                    codeWriter.writeReturn();
                    break;
                case C_CALL:
                    codeWriter.writeCall(arg1, arg2);
                    break;
            }
        }
        codeWriter.close();
    }

    // TODO Learn how to write standardized Javadoc for commandline arguments
    /**
     * @param args
     */
    public static void main(String[] args) {
        VMTranslator vmTranslator = new VMTranslator(args);
        vmTranslator.start();
    }
}
