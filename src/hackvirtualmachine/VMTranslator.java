package hackvirtualmachine;

import hackvirtualmachine.Parser.CommandType;

public class VMTranslator {

    private Parser parser;
    private CodeWriter codeWriter;
    private CommandType commandType;
    private String arg1;
    private int arg2;

    public VMTranslator(String fileName) {
        parser = new Parser(fileName);
        codeWriter = new CodeWriter(fileName);
    }

    public void start() {

        while (parser.hasMoreCommands()) {

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
            }
        }
        codeWriter.close();
    }

    public static void main(String[] args) {
        VMTranslator vmTranslator = new VMTranslator("C:/Users/Mark/IdeaProjects/Hack_Virtual_Machine/src/hackvirtualmachine/BasicTest.vm");
        vmTranslator.start();
    }
}
