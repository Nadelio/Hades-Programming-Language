package src.compiler;

import java.util.HashMap;

import src.Main;
import src.parser.ASTC;
import src.parser.BinaryCommand;
import src.parser.Command;
import src.parser.LoopCommand;
import src.parser.Token;
import src.parser.UnaryCommand;

public class Compiler {
    private static final String[] bytecode = {
        "0", // INCV : 0
        "1", // DECV : 1
        "2", // INCP : 2
        "3", // DECP : 3
        "4", // LOOP [ : 4
        "5", // ] : 5
        "6", // WTV : 6
        "7", // IN : 7
        "8", // PUSH : 8
        "9", // POP : 9
        "10", // CDP : 10
        "11", // CALL : 11
        "12", // RDV : 12
        "13", // SYS : 13
        "14", // HLT : 14
        "15", // CLB : 15
        "16", // JLB : 16
        "17", // DLB : 17
        "18", // RDP : 18
        "19", // SET : 19
        "20", // Move Up (unused) : 20
        "21", // Move Down (unused) : 21
        "22", // MOV : 22
        "23", // INT : 23
        "24", // NOP : 24
        "25", // WRITE : 25
        "26", // OUT : 26
    };

    private static final String[] bytecodePrefixes = {
        "F", // functions
        "L", // labels
        "C", // comparisons
        "N", // numbers
    };

    private ASTC ast;
    private int position = -1;
    private int readPosition = 0;
    private Command cmd;
    private HashMap<String, Integer> dependencies = new HashMap<String, Integer>();
    private int dependencyCounter = 0;
    private HashMap<String, Integer> labels = new HashMap<String, Integer>();
    private int labelCounter = 0;

    public Compiler(ASTC astc) {
        this.ast = astc;
        this.cmd = null;
        this.readCommand();
    }

    public String compile(){
        String compiledCode = "";
        while(this.cmd.getKind() != Token.TokenType.END){
            String compiledCommand = this.compileCommand(this.cmd);
            compiledCode += compiledCommand;
            this.readCommand();
        }
        compiledCode += bytecode[14]; // HLT
        return compiledCode;
    }

    private void readCommand(){
        this.peekCommand();
        this.position = this.readPosition;
        this.readPosition++;
    }

    private void peekCommand(){
        if (this.readPosition >= this.ast.getTree().length) {
            this.cmd = new Command(Token.TokenType.END);
        } else {
            this.cmd = this.ast.getTree()[this.readPosition];
        }
    }

    private String compileCommand(Command cmd){
        switch(cmd.getKind()){
            case INCVAL:
                return bytecode[0] + " ";
            case DECVAL:
                return bytecode[1] + " ";
            case INCPOS:
                return bytecode[2] + " ";
            case DECPOS:
                return bytecode[3] + " ";
            case LOOP: // unary
                if(cmd instanceof LoopCommand){
                    LoopCommand loop = (LoopCommand) cmd;
                    String loopCode = bytecode[4] + " ";
                    for(int i = 0; i < loop.getBody().length; i++){
                        loopCode += this.compileCommand(loop.getBody()[i]);
                    }
                    loopCode += bytecode[5] + " ";
                    return loopCode;
                }
            case WRITEVAL:
                return bytecode[6] + " ";
            case IN:
                return bytecode[7] + " ";
            case PUSH:
                return bytecode[8] + " ";
            case POP:
                return bytecode[9] + " ";
            case CREATEDEPENDENCY: // binary //! gets called twice, error!!!
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();

                    String dependencyAndAlias = "";
                    if(Main.EPU_FLAG){
                        dependencyAndAlias = dependencyToBin("N" + field1[1].getLiteral() + " N" + field1[2].getLiteral(), field2[1].getLiteral());
                    } else {
                        dependencyAndAlias = dependencyToBin("D" + field1[1].getLiteral() + field1[2].getLiteral() + field1[3].getLiteral(), field2[1].getLiteral());
                    }

                    return bytecode[10] + " " + dependencyAndAlias;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case CALLDEPENDENCY: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String alias = byteCodeFromAlias(field[1].getLiteral(), false);
                    return bytecode[11] + " " + alias;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READVAL:
                return bytecode[12] + " ";
            case SYSCALL: // unary // different unary format from rest
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String syscallCode = bytecode[13] + " ";
                    if(field[1].getType() == Token.TokenType.ALIAS){
                        syscallCode += byteCodeFromAlias(field[1].getLiteral(), true);
                        syscallCode += byteCodeFromAlias(field[2].getLiteral(), true);
                        syscallCode += byteCodeFromAlias(field[3].getLiteral(), true);
                        syscallCode += byteCodeFromAlias(field[4].getLiteral(), true);
                        syscallCode += byteCodeFromAlias(field[5].getLiteral(), true);
                        return syscallCode;
                    } else if(field[1].getType() == Token.TokenType.NUMBER){
                        syscallCode += bytecodePrefixes[3] + field[1].getLiteral() + " ";
                        syscallCode += bytecodePrefixes[3] + field[2].getLiteral() + " ";
                        syscallCode += bytecodePrefixes[3] + field[3].getLiteral() + " ";
                        syscallCode += bytecodePrefixes[3] + field[4].getLiteral() + " ";
                        syscallCode += bytecodePrefixes[3] + field[5].getLiteral() + " ";
                        return syscallCode;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case CREATELABEL: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    return bytecode[15] + " " + labelToBin(field[1].getLiteral());
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case JUMPLABEL: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    return bytecode[16] + " " + byteCodeFromAlias(field[1].getLiteral(), true);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case DELETELABEL: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    return bytecode[17] + " " + byteCodeFromAlias(field[1].getLiteral(), true);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READPOS:
                return bytecode[18] + " ";
            case SET: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String num = bytecodePrefixes[3] + field[1].getLiteral();
                    return bytecode[19] + " " + num + " ";
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case MOVE: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String num = bytecodePrefixes[3] + field[1].getLiteral();
                    return bytecode[22] + " " + num + " ";
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case INTERRUPT: // binary
                String interruptCode = bytecode[23] + " ";
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    interruptCode += byteCodeFromAlias(field1[1].getLiteral(), true);
                    interruptCode += byteCodeFromComparison(field1[2].getType());
                    interruptCode += byteCodeFromAlias(field1[3].getLiteral(), true);
                    interruptCode += byteCodeFromAlias(field2[1].getLiteral(), false);
                    return interruptCode;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case NOP:
                return bytecode[24] + " ";
            case WRITE: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String num = bytecodePrefixes[3] + field[1].getLiteral();
                    return bytecode[25] + " " + num + " ";
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case OUT:
                return bytecode[26] + " ";
            default:
                return "";
        }
    }

    private String dependencyToBin(String dependency, String alias){
        if(Main.EPU_FLAG){
            dependencies.put(alias, dependencyCounter);
            dependencyCounter++;
        }

        return dependency + " " + bytecodePrefixes[0] + dependencies.get(alias) + " ";
    }

    private String labelToBin(String label){
        labels.put(label, labelCounter);
        labelCounter++;
        return bytecodePrefixes[1] + labels.get(label) + " ";
    }

    private String byteCodeFromAlias(String alias, boolean isLabel){
        if(isLabel){
            return bytecodePrefixes[1] + labels.get(alias) + " ";
        }
        return bytecodePrefixes[0] + dependencies.get(alias) + " ";
    }

    private String byteCodeFromComparison(Token.TokenType comparision){
        switch(comparision){
            case NOTEQUAL:
                return "C0 ";
            case EQUAL:
                return "C1 ";
            case GREATER:
                return "C2 ";
            case LESS:
                return "C3 ";
            case GREATEREQUAL:
                return "C4 ";
            case LESSEQUAL:
                return "C5 ";
            default:
                throw new IllegalArgumentException("Invalid comparison type.");
        }
    }
}
