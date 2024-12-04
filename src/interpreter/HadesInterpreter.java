package src.interpreter;

import java.io.File;
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

public class HadesInterpreter {
    // ast tree variables
    private ASTC ast;
    private int pos;
    private int readPos;
    private Command cmd;

    // interpreter variables
    public HashMap<String, Integer> labels = new HashMap<String, Integer>();
    public HashMap<String, File> functions = new HashMap<String, File>();
    public int[] memory = new int[65536];
    public int[] stack = new int[256];
    public int stackPtr = 0;
    public int ptr = 0;
    public int ptrVal = 0;

    public HadesInterpreter(ASTC ast) {
        this.ast = ast;
        this.pos = -1;
        this.readPos = 0;
        this.cmd = null;
        this.readCommand();
        System.out.println("[");
        for(Command c : ast.getTree()){
            System.out.println("    " + c.toString());
            if(c instanceof LoopCommand){
                System.out.println("    [");
                for(Command c2 : ((LoopCommand) c).getBody()){
                    System.out.println("        " + c2.toString());
                }
                System.out.println("    ]");
            }
        }
        System.out.println("]");
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
        if(Main.DEBUG_FLAG){System.out.println("\u001B[34mInterpreting Command: \u001B[33m" + cmd.toString() + "\u001B[0m");}
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
                Scanner sc = new Scanner(System.in);
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
            } catch(Exception e){
                return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField1()[1].getLiteral() + " at position: " + pos);
            }
        } else {
            lab1 = cmd.getField1()[1].getLiteral();
            if(this.labels.containsKey(lab1)){
                val1 = this.labels.get(lab1);
            } else {
                return Result.Error(Result.Errors.NONEXISTENT_LABEL, lab1 + " at position: " + pos);
            }
        }

        if(cmd.getField1()[3].getType() == Token.TokenType.NUMBER){
            try{
                val2 = Integer.parseInt(cmd.getField1()[3].getLiteral());
            } catch(Exception e){
                return Result.Error(Result.Errors.INVALID_VALUE, cmd.getField1()[3].getLiteral() + " at position: " + pos);
            }
        } else {
            lab2 = cmd.getField1()[3].getLiteral();
            if(this.labels.containsKey(lab2)){
                val2 = this.labels.get(lab2);
            } else {
                return Result.Error(Result.Errors.NONEXISTENT_LABEL, lab2 + " at position: " + pos);
            }
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
                        fileData += sc.nextLine();
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
        if(labels.containsKey(cmd.getField()[1].getLiteral())){
            return Result.Error(Result.Errors.NONEXISTENT_LABEL, cmd.getField()[1].getLiteral() + " at position: " + pos);
        }
        ptr = labels.get(cmd.getField()[1].getLiteral());

        return Result.Success();
    }

    private Result createDependency(BinaryCommand cmd){
        File f = new File(cmd.getField1()[1].getLiteral() + cmd.getField1()[2].getLiteral() + cmd.getField1()[3].getLiteral());
        if(!f.exists()){return Result.Error(Result.Errors.FILE_NOT_FOUND, cmd.getField1()[1].getLiteral() + cmd.getField1()[2].getLiteral() + cmd.getField1()[3].getLiteral() + "\u001B[31m at position: \u001B[33m" + pos);}
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
        int i = 0;
        while(i < body.length){
            Result r = this.interpretCommand(body[i]);
            if(!r.getSuccess()){
                r.handleError();
            }
            i++;
        }
    }
}