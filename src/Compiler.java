package src;

import java.util.HashMap;

public class Compiler {
    private static final String[] bytecode = {
        "0000000000000001", // INCV : 0
        "0000000000000010", // DECV : 1
        "0000000000000011", // INCP : 2
        "0000000000000100", // DECP : 3
        "0000000000000101", // LOOP [ : 4
        "0000000000000110", // ] : 5
        "0000000000000111", // WTV : 6
        "0000000000001000", // IN : 7
        "0000000000001001", // PUSH : 8
        "0000000000001010", // POP : 9
        "0000000000001011", // CDP : 10
        "0000000000001100", // CALL : 11
        "0000000000001101", // RDV : 12
        "0000000000001110", // SYS : 13
        "0000000000001111", // HLT : 14
        "0000000000010000", // CLB : 15
        "0000000000010001", // JLB : 16
        "0000000000010010", // DLB : 17
        "0000000000010011", // RDP : 18
        "0000000000010100", // SET : 19
        "0000000000010101", // N/A : 20
        "0000000000010110", // N/A : 21
        "0000000000010111", // MOV : 22
        "0000000000011000", // INT : 23
        "0000000000011001", // NOP : 24
        "0000000000011010", // SYS CALL OBRACKET : 25
        "0000000000011011", // SYS CALL CBRACKET : 26
        "0000000000011100", // WRITE : 27
    };

    private ASTC ast;
    private int position = 0;
    private int readPosition = 0;
    private Command cmd;
    private HashMap<String, Integer> dependencies = new HashMap<String, Integer>();
    private int dependencyCounter = 0;
    private HashMap<String, Integer> labels = new HashMap<String, Integer>();
    private int labelCounter = 0;

    public Compiler(ASTC astc) {
        this.ast = astc;
        this.cmd = ast.getTree()[this.position];
    }

    public String compile(){
        String compiledCode = "";
        while(this.cmd.getKind() != Token.TokenType.END){
            compiledCode += this.compileCommand(this.cmd);
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
                return bytecode[0];
            case DECVAL:
                return bytecode[1];
            case INCPOS:
                return bytecode[2];
            case DECPOS:
                return bytecode[3];
            case LOOP: // unary
                if(cmd instanceof LoopCommand){
                    LoopCommand loop = (LoopCommand) cmd;
                    String loopCode = bytecode[4] + " ";
                    for(int i = 0; i < loop.getBody().length; i++){
                        loopCode += this.compileCommand(loop.getBody()[i]) + " ";
                    }
                    loopCode += bytecode[5] + " ";
                    return loopCode;
                }
            case WRITEVAL:
                return bytecode[6];
            case IN:
                return bytecode[7];
            case PUSH:
                return bytecode[8];
            case POP:
                return bytecode[9];
            case CREATEDEPENDENCY: // binary
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();

                    String dependencyAndAlias = "";
                    if(Main.EPU_FLAG){
                        dependencyAndAlias = dependencyToBin(field1[1].getLiteral() + " " + field1[2].getLiteral(), field2[1].getLiteral());
                    } else {
                        dependencyAndAlias = dependencyToBin(field1[1].getLiteral() + field1[2].getLiteral() + field1[3].getLiteral(), field2[1].getLiteral());
                    }

                    return bytecode[10] + " " + dependencyAndAlias;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case CALLDEPENDENCY: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String alias = binFromAlias(field[1].getLiteral(), false);
                    return bytecode[11] + " " + alias;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READVAL:
                return bytecode[12];
            case SYSCALL: // unary // different unary format from rest
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String syscallCode = bytecode[13] + " ";
                    if(field[1].getType() == Token.TokenType.ALIAS){
                        syscallCode += bytecode[25] + " ";
                        syscallCode += binFromAlias(field[1].getLiteral(), true) + " ";
                        syscallCode += binFromAlias(field[2].getLiteral(), true) + " ";
                        syscallCode += binFromAlias(field[3].getLiteral(), true) + " ";
                        syscallCode += binFromAlias(field[4].getLiteral(), true) + " ";
                        syscallCode += binFromAlias(field[5].getLiteral(), true) + " ";
                        syscallCode += bytecode[26] + " ";
                        return syscallCode;
                    } else if(field[1].getType() == Token.TokenType.NUMBER){
                        syscallCode += intToBin(Integer.parseInt(field[1].getLiteral())) + " ";
                        syscallCode += intToBin(Integer.parseInt(field[2].getLiteral())) + " ";
                        syscallCode += intToBin(Integer.parseInt(field[3].getLiteral())) + " ";
                        syscallCode += intToBin(Integer.parseInt(field[4].getLiteral())) + " ";
                        syscallCode += intToBin(Integer.parseInt(field[5].getLiteral())) + " ";
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
                    return bytecode[16] + " " + binFromAlias(field[1].getLiteral(), true);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case DELETELABEL: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    return bytecode[17] + " " + binFromAlias(field[1].getLiteral(), true);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READPOS:
                return bytecode[18];
            case SET: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String intToBin = intToBin(Integer.parseInt(field[0].getLiteral()));
                    return bytecode[19] + intToBin;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case MOVE: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String intToBin = intToBin(Integer.parseInt(field[0].getLiteral()));
                    return bytecode[22] + intToBin;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case INTERRUPT: // binary
                String interruptCode = bytecode[23] + " ";
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    interruptCode += binFromAlias(field1[1].getLiteral(), true) + " ";
                    interruptCode += binFromComparison(field1[2].getType()) + " ";
                    interruptCode += binFromAlias(field1[3].getLiteral(), true) + " ";
                    interruptCode += binFromAlias(field2[1].getLiteral(), false) + " ";
                    return interruptCode;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case NOP:
                return bytecode[24];
            case WRITE: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    String intToBin = intToBin(Integer.parseInt(field[0].getLiteral()));
                    return bytecode[27] + intToBin;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            default:
                return "";
        }
    }

    private String dependencyToBin(String dependency, String alias){
        if(Main.EPU_FLAG){
            dependencies.put(alias, dependencyCounter);
            dependencyCounter++;
        }

        return dependency + " " + intToBin(dependencies.get(alias));
    }

    private String labelToBin(String label){
        labels.put(label, labelCounter);
        labelCounter++;
        return intToBin(labels.get(label));
    }

    private String binFromAlias(String alias, boolean isLabel){
        if(isLabel){
            return intToBin(labels.get(alias));
        }
        return intToBin(dependencies.get(alias));
    }

    private String binFromComparison(Token.TokenType comparision){
        switch(comparision){
            case NOTEQUAL:
                return "0000000000000001";
            case EQUAL:
                return "0000000000000010";
            case GREATER:
                return "0000000000000011";
            case LESS:
                return "0000000000000100";
            case GREATEREQUAL:
                return "0000000000000101";
            case LESSEQUAL:
                return "0000000000000110";
            default:
                throw new IllegalArgumentException("Invalid comparison type.");
        }
    }

    private String intToBin(int num){
        String intToBin = Integer.toBinaryString(num);
        if(intToBin.length() < 16){ intToBin = "0".repeat(16 - intToBin.length()) + intToBin; }
        return intToBin;
    }
}
