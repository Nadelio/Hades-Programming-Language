package src.compiler;

import java.util.HashMap;

import src.Main;
import src.parser.ASTC;
import src.parser.BinaryCommand;
import src.parser.Command;
import src.parser.FunctionMacroCommand;
import src.parser.LoopCommand;
import src.parser.Token;
import src.parser.UnaryCommand;

public class Compiler {
    private static final String[] bytecode = {
        "0",  // INCV
        "1",  // DECV
        "2",  // INCP
        "3",  // DECP
        "4",  // LOOP [ 
        "5",  // ] (for loop)
        "6",  // WTV
        "7",  // IN
        "8",  // PUSH
        "9",  // POP
        "10", // CDP
        "11", // CALL
        "12", // RDV
        "13", // SYS
        "14", // HLT
        "15", // CLB
        "16", // JLB
        "17", // DLB
        "18", // RDP
        "19", // SET
        "20", // Move Up (unused)
        "21", // Move Down (unused)
        "22", // MOV
        "23", // INT
        "24", // NOP
        "25", // WRT
        "26", // OUT
        // v1.1.0 additions
        "27", // HOLD
        "28", // DROP
        "29", // MLB
        "30", // MLP
        "31", // SLB
        "32", // SLV
        "33", // FUNC [
        "34", // ] (for func)
        "35", // OUTN
        "36", // OUTV
        "37", // OUTR
        "38", // INV
        "39", // INS
        "40", // SWM
        "41", // WDD [
        "42", // ] (for WDD)
        "43", // DS [
        "44", // ] [] (for DS)
        "45", // FSO
        "46", // FSC
        "47", // RFF
        "48", // WTF
    };

    private static final String[] bytecodePrefixes = {
        "F", // functions
        "L", // labels
        "C", // comparisons
        "N", // numbers
        // v1.1.0 additions
        "S", // strings
    };

    private ASTC ast;
    @SuppressWarnings("unused")
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
            case CREATEDEPENDENCY: // binary 
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();

                    String dependencyAndAlias = "";
                    if(Main.EPU_FLAG){
                        dependencyAndAlias = dependencyToBin("N" + field1[1].getLiteral() + " N" + field1[2].getLiteral(), field2[1].getLiteral());
                    } else {
                        dependencyAndAlias = dependencyToBin(byteCodeFromString(field1[1].getLiteral()), field2[1].getLiteral());
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

                    for(int i = 1; i < 6; i++){
                        switch(field[i].getType()){
                            case ALIAS:
                                syscallCode += byteCodeFromAlias(field[i].getLiteral(), true);
                                break;
                            case NUMBER:
                                syscallCode += bytecodePrefixes[3] + field[i].getLiteral() + " ";
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid field type.");
                        }
                    }
                    return syscallCode;
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
                    interruptCode += byteCodeFromVariantField(field1[1], true);
                    interruptCode += byteCodeFromComparison(field1[2].getType());
                    interruptCode += byteCodeFromVariantField(field1[3], true);
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
            case HOLD:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[27] + " " + byteCodeFromAlias(unary.getField()[1].getLiteral(), true);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case DROP:
                return bytecode[28] + " ";
            case MOVEHELDLABELPOSITION:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[29] + " " + byteCodeFromSingleField(unary.getField());
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READTOHELDLABELPOSITION:
                return bytecode[30] + " ";
            case SETHELDLABELVALUE:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[31] + " " + byteCodeFromSingleField(unary.getField());
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case READTOHELDLABELVALUE:
                return bytecode[32] + " ";
            case FUNCTIONMACRO:
                if(cmd instanceof FunctionMacroCommand){
                    FunctionMacroCommand funcMacro = (FunctionMacroCommand) cmd;
                    String funcMacroCode = bytecode[33] + " ";
                    
                    funcMacroCode += dependencyToBin("", funcMacro.getName()[1].getLiteral());
                    for(int i = 0; i < funcMacro.getBody().length; i++){
                        funcMacroCode += this.compileCommand(funcMacro.getBody()[i]);
                    }
                    return funcMacroCode + bytecode[34] + " ";
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case OUTNUMBER:
                return bytecode[35] + " ";
            case OUTVALUE:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[36] + " " + byteCodeFromSingleField(unary.getField());
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case OUTRANGE:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    Token[] field = unary.getField();
                    return bytecode[37] + " " + byteCodeFromField(field);
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case INVALUE:
                return bytecode[38] + " ";
            case INSTRING:
                return bytecode[39] + " ";
            case SETWRITEMODE:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[40] + " " + byteCodeFromSingleField(unary.getField());
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case WRITEDATADUMP:
                if(cmd instanceof UnaryCommand){
                    UnaryCommand unary = (UnaryCommand) cmd;
                    return bytecode[41] + " " + byteCodeFromField(unary.getField()) + bytecode[42] + " ";
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case CREATEDATASTRUCTURE:
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    String dataStructureCode = bytecode[43] + " ";
                    dataStructureCode += byteCodeFromField(field1);
                    dataStructureCode += bytecode[44] + " ";
                    labelToBin(field2[1].getLiteral());
                    dataStructureCode += byteCodeFromAlias(field2[1].getLiteral(), true);
                    return dataStructureCode;
                } else {
                    throw new IllegalArgumentException("Invalid command type.");
                }
            case FILESTREAMOPEN: // binary
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    String fileStreamCode = bytecode[45] + " ";
                    fileStreamCode += byteCodeFromField(field1);
                    fileStreamCode += byteCodeFromField(field2);
                    return fileStreamCode;
                }
            case FILESTREAMCLOSE: // unary
                if(cmd instanceof UnaryCommand){
                    UnaryCommand binary = (UnaryCommand) cmd;
                    Token[] field1 = binary.getField();
                    String fileStreamCode = bytecode[46] + " ";
                    fileStreamCode += byteCodeFromString(field1[1].getLiteral());
                    return fileStreamCode;
                }
            case READFROMFILE: // binary
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    String fileStreamCode = bytecode[47] + " ";
                    fileStreamCode += byteCodeFromString(field1[1].getLiteral());
                    fileStreamCode += byteCodeFromVariantField(field2[1], true);
                    return fileStreamCode;
                }
            case WRITETOFILE: // binary
                if(cmd instanceof BinaryCommand){
                    BinaryCommand binary = (BinaryCommand) cmd;
                    Token[] field1 = binary.getField1();
                    Token[] field2 = binary.getField2();
                    String fileStreamCode = bytecode[48] + " ";
                    fileStreamCode += byteCodeFromString(field1[1].getLiteral());
                    fileStreamCode += byteCodeFromVariantField(field2[1], true);
                    return fileStreamCode;
                }
            default:
                return "";
        }
    }

    private String dependencyToBin(String dependency, String alias){
        dependencies.put(alias, dependencyCounter);
        dependencyCounter++;
        return dependency + bytecodePrefixes[0] + dependencies.get(alias) + " ";
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

    private String byteCodeFromComparison(Token.TokenType comparison){
        switch(comparison){
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

    private String byteCodeFromSingleField(Token[] field){
        switch(field[1].getType()){
            case NUMBER:
                return byteCodeFromNumber(field[1].getLiteral());
            case STRING:
                return byteCodeFromString(field[1].getLiteral());
            case ALIAS:
                return byteCodeFromAlias(field[1].getLiteral(), true);
            default:
                throw new IllegalArgumentException("Invalid field type.");
        }
    }

    private String byteCodeFromVariantField(Token field, boolean isLabel){
        switch(field.getType()){
            case NUMBER:
                return byteCodeFromNumber(field.getLiteral());
            case ALIAS:
                return byteCodeFromAlias(field.getLiteral(), isLabel);
            default:
                throw new IllegalArgumentException("Invalid field type.");
        }
    }

    private String byteCodeFromField(Token[] field){
        String s = "";
        for(int i = 1; i < field.length - 1; i++){
            switch(field[i].getType()){
                case NUMBER:
                    s += byteCodeFromNumber(field[i].getLiteral());
                    break;
                case STRING:
                    s += byteCodeFromString(field[i].getLiteral());
                    break;
                case ALIAS:
                    s += byteCodeFromAlias(field[i].getLiteral(), true);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field type.");
            }
        }
        return s;
    }

    private String byteCodeFromNumber(String num){
        return bytecodePrefixes[3] + num + " ";
    }

    private String byteCodeFromString(String str) {
        char[] chars = str.toCharArray();
        String s = bytecodePrefixes[4] + chars.length + " ";
        for (char c : chars) {
            s += bytecodePrefixes[3] + (int) c + " ";
        }
        return s;
    }
}