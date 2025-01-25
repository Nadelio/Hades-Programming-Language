package src.interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Scanner;

import src.Main;
import src.parser.ASTC;
import src.parser.BinaryCommand;
import src.parser.Command;
import src.parser.Lexer;
import src.parser.LoopCommand;
import src.parser.Parser;
import src.parser.Result;
import src.parser.Token;
import src.parser.UnaryCommand;
import src.util.Constants;

public class HadesInterpreter {
    // ast tree variables
    private ASTC ast;
    private int pos;
    private int readPos;
    private Command cmd;

    // interpreter variables
    public HashMap<String, Integer> labels = new HashMap<String, Integer>();
    public HashMap<String, File> functions = new HashMap<String, File>();
    public HashMap<String, Token[]> structures = new HashMap<String, Token[]>();
    public int[] memory = new int[65536];
    public int[] stack = new int[256];
    public Label heldLabel;
    public int stackPtr = 0;
    public int ptr = 0;
    public int ptrVal = 0;
    private Scanner sc;
    private boolean writemode = false;
    private FileInputStream fileinputstream;
    private FileOutputStream fileoutputstream;

    public HadesInterpreter(ASTC ast) {
        this.ast = ast;
        this.pos = -1;
        this.readPos = 0;
        this.cmd = null;
        this.readCommand();
    }

    public void interpret(){
        while(this.cmd.getKind() != Token.TokenType.END){
            Result r = this.interpretCommand(this.cmd);
            if(!r.getSuccess()){
                r.handleError();
            }
            this.readCommand();
        }
    }

    public void readCommand(){
        this.peekCommand();
        this.pos = this.readPos;
        this.readPos++;
    }

    public void peekCommand(){
        if (this.readPos >= this.ast.getTree().length) {
            this.cmd = new Command(Token.TokenType.END);
        } else {
            this.cmd = this.ast.getTree()[this.readPos];
        }
    }

    public Result interpretCommand(Command cmd){
        if(Main.DEBUG_FLAG){System.out.println(Constants.ANSI_MSG + "Interpreting Command: " + Constants.ANSI_INFO + cmd.toString() + Constants.ANSI_RESET);}
        switch(cmd.getKind()){
            case MOVE:
                UnaryCommand move = (UnaryCommand) cmd;
                return this.move(move);
            case SET:
                UnaryCommand set = (UnaryCommand) cmd;
                return this.set(set);
            case WRITE:
                UnaryCommand write = (UnaryCommand) cmd;
                return this.write(write);
            case OUT:
                System.out.print((char) memory[ptr]);
                return Result.Success();
            case IN:
                sc = new Scanner(System.in);
                int val = (int) sc.next().charAt(0);
                sc.close();
                this.memory[ptr] = val;
                return Result.Success();
            case NOP:
                try{Thread.sleep(10);}catch(Exception e){}
                return Result.Success();
            case INTERRUPT:
                BinaryCommand interrupt = (BinaryCommand) cmd;
                return this.interrupt(interrupt);
            case WRITEPOS:
                return this.writePosition();
            case READPOS:
                return this.readPosition();
            case INCPOS:
                return this.incrementPosition();
            case DECPOS:
                return this.decrementPosition();
            case WRITEVAL:
                return this.writeValue();
            case READVAL:
                return this.readValue();
            case INCVAL:
                return this.incrementValue();
            case DECVAL:
                return this.decrementValue();
            case PUSH:
                return this.push();
            case POP:
                return this.pop();
            case CREATELABEL:
                UnaryCommand cLabel = (UnaryCommand) cmd;
                return this.createLabel(cLabel);
            case DELETELABEL:
                UnaryCommand dLabel = (UnaryCommand) cmd;
                return this.deleteLabel(dLabel);
            case JUMPLABEL:
                UnaryCommand jLabel = (UnaryCommand) cmd;
                return this.jumpLabel(jLabel);
            case CREATEDEPENDENCY:
                BinaryCommand cFunc = (BinaryCommand) cmd;
                return this.createDependency(cFunc);
            case CALLDEPENDENCY:
                UnaryCommand callFunc = (UnaryCommand) cmd;
                return this.callDependency(callFunc);
            case LOOP:
                LoopCommand loop = (LoopCommand) cmd;
                return this.loop(loop);
            case HOLD:
                UnaryCommand hold = (UnaryCommand) cmd;
                return this.hold(hold);
            case DROP:
                return this.drop();
            case MOVEHELDLABELPOSITION:
                UnaryCommand mlb = (UnaryCommand) cmd;
                return this.moveHeldLabelPosition(mlb);
            case READTOHELDLABELPOSITION:
                return this.readToHeldLabelPosition();
            case SETHELDLABELVALUE:
                UnaryCommand slb = (UnaryCommand) cmd;
                return this.setHeldLabelValue(slb);
            case READTOHELDLABELVALUE:
                return this.readToHeldLabelValue();
            case FUNCTIONMACRO:
                BinaryCommand func = (BinaryCommand) cmd;
                return Result.Error(Result.Errors.NONEXISTENT_FUNCTION, "The instruction: FUNC [] [] at position: " + pos + " is not yet implemented.");
            case OUTNUMBER:
                System.out.print(memory[ptr]);
                return Result.Success();
            case OUTVALUE:
                UnaryCommand outValue = (UnaryCommand) cmd;
                System.out.print((char) Integer.parseInt(outValue.getField()[1].getLiteral()));
                return Result.Success();
            case OUTRANGE:
                UnaryCommand outRange = (UnaryCommand) cmd;
                try{
                    int start = Integer.parseInt(outRange.getField()[1].getLiteral());
                    int end = Integer.parseInt(outRange.getField()[2].getLiteral());
                    for(int i = start; i <= end; i++){ System.out.print((char) memory[i]); }
                    return Result.Success();
                } catch(Exception e){
                    return Result.Error(Result.Errors.INVALID_VALUE, outRange.getField()[1].getLiteral() + " or " + outRange.getField()[2].getLiteral() + " at position: " + pos);
                }
            case INVALUE:
                sc = new Scanner(System.in);
                memory[ptr] = sc.nextInt();
                sc.close();
                return Result.Success();
            case INSTRING:
                sc = new Scanner(System.in);
                String str = sc.nextLine();
                for(int i = 0; i < str.length(); i++){
                    memory[ptr + i] = (int) str.charAt(i);
                }
                sc.close();
                return Result.Success();
            case SETWRITEMODE:
                UnaryCommand swm = (UnaryCommand) cmd;
                if(swm.getField()[1].getLiteral().equals("0")){
                    writemode = false;
                    return Result.Success();
                } else {
                    writemode = true;
                    return Result.Success();
                }
            case WRITEDATADUMP:
                UnaryCommand wdd = (UnaryCommand) cmd;
                return writeDataDump(wdd);
            case CREATEDATASTRUCTURE:
                UnaryCommand cds = (UnaryCommand) cmd;
                return createDataStructure(cds);
            case FILESTREAMOPEN:
                BinaryCommand fso = (BinaryCommand) cmd;
                boolean mode = false;
                switch(fso.getField2()[1].getType()){
                    case NUMBER:
                        mode = Integer.parseInt(fso.getField2()[1].getLiteral()) >= 1 ? true : false;
                        break;
                    case ALIAS:
                        mode = memory[labels.get(fso.getField2()[1].getLiteral())] >= 1 ? true : false;
                        break;
                    default:
                        return Result.Error(Result.Errors.INVALID_VALUE, "Invalid input: " + fso.getField2()[1].getLiteral() + " at position: " + pos + " is not yet implemented.");
                }
                if(mode){
                    try{fileoutputstream = new FileOutputStream(fso.getField1()[1].getLiteral());}catch(Exception e){return Result.Error(Result.Errors.FILE_NOT_FOUND, fso.getField1()[1].getLiteral() + " at position: " + pos);}
                } else {
                    try{fileinputstream = new FileInputStream(fso.getField1()[1].getLiteral());}catch(Exception e){return Result.Error(Result.Errors.FILE_NOT_FOUND, fso.getField1()[1].getLiteral() + " at position: " + pos);}
                }
                return Result.Success();
            case FILESTREAMCLOSE:
                BinaryCommand fsc = (BinaryCommand) cmd;
                return Result.Error(Result.Errors.INVALID_COMMAND, "The instruction: FSC at position: " + pos + " is not yet implemented.");
            case READFROMFILE:
                BinaryCommand rff = (BinaryCommand) cmd;
                return Result.Error(Result.Errors.INVALID_COMMAND, "The instruction: RFF at position: " + pos + " is not yet implemented.");
            case WRITETOFILE:
                BinaryCommand wtf = (BinaryCommand) cmd;
                return Result.Error(Result.Errors.INVALID_COMMAND, "The instruction: WTF at position: " + pos + " is not yet implemented.");
            case COMMENT:
            case END:
            case EOF:
                return Result.Success();
            default:
                return Result.Error(Result.Errors.INVALID_COMMAND, cmd.getKind() + " at position: " + pos);
        }
    }

    private Result move(UnaryCommand cmd){
        try{
            int val = Integer.parseInt(cmd.getField()[1].getLiteral());
            this.ptr = val;
            return Result.Success();
        } catch(Exception e){
            return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField()[1].getLiteral() + " at position: " + pos);
        }
    }

    private Result set(UnaryCommand cmd){
        try{
            int val = Integer.parseInt(cmd.getField()[1].getLiteral());
            this.ptrVal = val;
            return Result.Success();
        } catch(Exception e){
            return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField()[1].getLiteral() + " at position: " + pos);
        }
    }

    private Result write(UnaryCommand cmd){
        try{
            int val = Integer.parseInt(cmd.getField()[1].getLiteral());
            this.memory[ptr] = val;
            return Result.Success();
        } catch(Exception e){
            return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField()[1].getLiteral() + " at position: " + pos);
        }
    }

    private Result interrupt(BinaryCommand cmd){
        int val1;
        int val2;
        String lab1;
        String lab2;
        if(cmd.getField1()[1].getType() == Token.TokenType.NUMBER){
            try{
                val1 = Integer.parseInt(cmd.getField1()[1].getLiteral());
            } catch(Exception e){ return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField1()[1].getLiteral() + " at position: " + pos); }
        } else {
            lab1 = cmd.getField1()[1].getLiteral();
            if(this.labels.containsKey(lab1)){
                val1 = memory[this.labels.get(lab1)];
            } else { return Result.Error(Result.Errors.NONEXISTENT_LABEL, lab1 + " at position: " + pos); }
        }

        if(cmd.getField1()[3].getType() == Token.TokenType.NUMBER){
            try{
                val2 = Integer.parseInt(cmd.getField1()[3].getLiteral());
            } catch(Exception e){ return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField1()[3].getLiteral() + " at position: " + pos); }
        } else {
            lab2 = cmd.getField1()[3].getLiteral();
            if(this.labels.containsKey(lab2)){
                val2 = memory[this.labels.get(lab2)];
            } else { return Result.Error(Result.Errors.NONEXISTENT_LABEL, lab2 + " at position: " + pos); }
        }

        switch(cmd.getField1()[2].getType()){
            case LESSEQUAL:
                if(val1 <= val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            case GREATEREQUAL:
                if(val1 >= val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            case LESS:
                if(val1 < val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            case GREATER:
                if(val1 > val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            case EQUAL:
                if(val1 == val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            case NOTEQUAL:
                if(val1 != val2){
                    return callFunction(cmd.getField2()[1].getLiteral());
                }
                break;
            default:
                return Result.Error(Result.Errors.INVALID_COMPARISON, cmd.getField1()[2].getLiteral() + " at position: " + pos);
        }
        return Result.Success();
    }

    public Result callFunction(String funcName){
        if(this.functions.containsKey(funcName)){
            File file = this.functions.get(funcName);

            if(file.getName().endsWith(".hds")){
                String fileData = "";
                try{
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine()){
                        fileData += sc.nextLine() + " ";
                    }
                    sc.close();
                } catch(Exception e){
                    return Result.Error(Result.Errors.FILE_NOT_FOUND, file.getName() + " at position: " + pos);
                }
    
                Lexer l = new Lexer(fileData);
                Token[] tokens = l.lex();
                Parser p = new Parser(tokens);
                ASTC ast = p.parse();
                HadesInterpreter i = new HadesInterpreter(ast);
                i.interpret();
                return Result.Success();
            } else if(file.getName().endsWith(".ebf")){
                eBFInterpreter i = new eBFInterpreter(file, this);
                i.interpret();
                return Result.Success();
            } else if(file.getName().endsWith(".ebin")){
                eBinInterpreter i = new eBinInterpreter(file, this);
                i.interpret();
                return Result.Success();
            } else {
                return Result.Error(Result.Errors.INVALID_FILE, file.getName() + " at position: " + pos);
            }

        } else {
            return Result.Error(Result.Errors.NONEXISTENT_FUNCTION, funcName + " at position: " + pos);
        }
    }

    private Result writePosition(){
        memory[ptr] = ptr;
        return Result.Success();
    }

    private Result readPosition(){
        ptrVal = ptr;
        return Result.Success();
    }

    private Result incrementPosition(){
        ptr++;
        if(ptr >= memory.length){ ptr = 0; }
        return Result.Success();
    }
    
    private Result decrementPosition(){
        ptr--;
        if(ptr <= -1){ ptr = memory.length - 1; }
        return Result.Success();
    }

    private Result writeValue(){
        memory[ptr] = ptrVal;
        return Result.Success();
    }

    private Result readValue(){
        ptrVal = memory[ptr];
        return Result.Success();
    }

    private Result incrementValue(){
        ptrVal++;
        return Result.Success();
    }

    private Result decrementValue(){
        ptrVal--;
        if(ptrVal < 0){ ptrVal = 0; }
        return Result.Success();
    }

    private Result push(){
        stack[stackPtr] = ptrVal;
        ptrVal = 0;
        stackPtr++;
        return Result.Success();
    }

    private Result pop(){
        ptrVal = stack[stackPtr];
        stack[stackPtr] = 0;
        stackPtr--;
        return Result.Success();
    }

    private Result createLabel(UnaryCommand cmd){
        labels.put(cmd.getField()[1].getLiteral(), ptr);
        return Result.Success();
    }

    private Result deleteLabel(UnaryCommand cmd){
        if(!labels.containsKey(cmd.getField()[1].getLiteral())){
            return Result.Error(Result.Errors.NONEXISTENT_LABEL, cmd.getField()[1].getLiteral() + " at position: " + pos);
        }
        labels.remove(cmd.getField()[1].getLiteral());
        return Result.Success();
    }

    private Result jumpLabel(UnaryCommand cmd){
        if(!labels.containsKey(cmd.getField()[1].getLiteral())){
            return Result.Error(Result.Errors.NONEXISTENT_LABEL, cmd.getField()[1].getLiteral() + Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        ptr = labels.get(cmd.getField()[1].getLiteral());

        return Result.Success();
    }

    private Result createDependency(BinaryCommand cmd){
        File f = new File(cmd.getField1()[1].getLiteral() + cmd.getField1()[2].getLiteral() + cmd.getField1()[3].getLiteral());
        if(!f.exists()){return Result.Error(Result.Errors.FILE_NOT_FOUND, cmd.getField1()[1].getLiteral() + cmd.getField1()[2].getLiteral() + cmd.getField1()[3].getLiteral() + Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);}
        String alias = cmd.getField2()[1].getLiteral();
        functions.put(alias, f);
        return Result.Success();
    }

    private Result callDependency(UnaryCommand cmd){
        String alias = cmd.getField()[1].getLiteral();
        callFunction(alias);
        return Result.Success();
    }

    private Result loop(LoopCommand cmd){
        Command[] body = cmd.getBody();

        while(memory[ptr] != 0){ this.subinterpret(body); }
        return Result.Success();
    }

    private void subinterpret(Command[] body){
        for(Command c : body){
            Result r = this.interpretCommand(c);
            if(!r.getSuccess()){ r.handleError(); }
        }
    }

    private Result hold(UnaryCommand cmd){
        if(!labels.containsKey(cmd.getField()[1].getLiteral())){
            return Result.Error(Result.Errors.NONEXISTENT_LABEL, cmd.getField()[1].getLiteral() + Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        String label = cmd.getField()[1].getLiteral();
        heldLabel = new Label(label, labels.get(label));
        return Result.Success();
    }

    private Result drop(){
        heldLabel = null;
        return Result.Success();
    }

    private Result moveHeldLabelPosition(UnaryCommand cmd){
        if(heldLabel == null){
            return Result.Error(Result.Errors.NO_HELD_LABEL, Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        heldLabel.address = Integer.parseInt(cmd.getField()[1].getLiteral());
        return Result.Success();
    }

    private Result readToHeldLabelPosition(){
        if(heldLabel == null){
            return Result.Error(Result.Errors.NO_HELD_LABEL, Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        heldLabel.address = memory[ptr];
        return Result.Success();
    }

    private Result setHeldLabelValue(UnaryCommand cmd){
        if(heldLabel == null){
            return Result.Error(Result.Errors.NO_HELD_LABEL, Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        memory[heldLabel.address] = Integer.parseInt(cmd.getField()[1].getLiteral());
        return Result.Success();
    }

    private Result readToHeldLabelValue(){
        if(heldLabel == null){
            return Result.Error(Result.Errors.NO_HELD_LABEL, Constants.ANSI_ERROR + " at position: " + Constants.ANSI_INFO + pos);
        }
        memory[heldLabel.address] = memory[ptr];
        return Result.Success();
    }

    private Result writeDataDump(UnaryCommand cmd){
        Token[] field = cmd.getField();
        Token[] field1 = new Token[field.length - 2];
        for(int i = 1; i < field.length - 1; i++){ field1[i - 1] = field[i]; }
        int origin = ptr;
        int writeIndex = 0;
        for(int i = 0; i < field1.length; i++){
            switch(field1[i].getType()){
                case NUMBER:
                    memory[origin + writeIndex++] = Integer.parseInt(field1[i].getLiteral());
                    break;
                case STRING:
                    for(int j = 0; j < field1[i].getLiteral().length(); j++){
                        memory[origin + writeIndex++] = (int) field1[i].getLiteral().charAt(j);
                    }
                    break;
                case ALIAS:
                    if(structures.containsKey(field1[i].getLiteral())){
                        writeIndex = writeDataStructure(field1[i].getLiteral(), writeIndex, origin);
                    } else if(labels.containsKey(field1[i].getLiteral())){
                        memory[origin + writeIndex++] = memory[labels.get(field1[i].getLiteral())];
                    } else {
                        return Result.Error(Result.Errors.NONEXISTENT_LABEL, field1[i].getLiteral() + " at position: " + pos);
                    }
                    break;
                default:
                    return Result.Error(Result.Errors.INVALID_VALUE, field1[i].getLiteral() + " at position: " + pos);
            }
        }
        return Result.Success();
    }

    private int writeDataStructure(String alias, int offset, int origin){
        Token[] data = structures.get(alias);
        for(Token t : data){
            switch(t.getType()){
                case NUMBER:
                    memory[origin + offset++] = Integer.parseInt(t.getLiteral());
                    break;
                case STRING:
                    for(int j = 0; j < t.getLiteral().length(); j++){
                        memory[origin + offset++] = (int) t.getLiteral().charAt(j);
                    }
                    break;
                case ALIAS:
                    if(structures.containsKey(t.getLiteral())){
                        offset = writeDataStructure(t.getLiteral(), offset, origin);
                    } else if(labels.containsKey(t.getLiteral())){
                        memory[origin + offset++] = memory[labels.get(t.getLiteral())];
                    } else {
                        throw new IllegalArgumentException("Non-existent label in data structure: " + t.getLiteral());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid token type in data structure: " + t.getLiteral());
            }
        }
        return offset;
    }

    private Result createDataStructure(UnaryCommand cmd){
        Token[] field = new Token[cmd.getField().length - 2];
        for(int i = 1; i < cmd.getField().length - 1; i++){ field[i - 1] = cmd.getField()[i]; }
        structures.put(cmd.getField()[1].getLiteral(), field);
        return Result.Success();
    }
}