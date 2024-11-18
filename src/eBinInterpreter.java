package src;

import java.io.File;
import java.util.Scanner;
import src.eBin.*;

public class eBinInterpreter {
    private int progPos;
    File file;
    HadesInterpreter caller;
    String[] commands;
    eBinCommand[] eBinCommands;
    int c; // for loops

    public eBinInterpreter(File file, HadesInterpreter caller){
        this.progPos = 0;
        this.file = file;
        this.caller = caller;
        this.c = 0;
    }

    public void interpret(){
        String code = "";
        try{
            Scanner sc = new Scanner(this.file);
            while(sc.hasNextLine()){
                code += sc.nextLine();
            }
            sc.close();
        } catch(Exception e){
            Result.Error(Result.Errors.FILE_NOT_FOUND, file.getName() + " at position: " + progPos).handleError();;    
        }

        commands = code.split("");

        eBinCommands = this.buildAST(commands);

        while(eBinCommands[this.progPos].getData() != 14){
            Result r = this.interpretCommand(eBinCommands[this.progPos]);
            if(!r.getSuccess()){
                r.handleError();
            }
            progPos++;
        }
    }

    private Result interpretCommand(eBinCommand command){

        switch(command.getType()){
            case INSTRUCTION:
                switch(command.getData()){
                    case 0: // INCV
                        this.caller.ptrVal++;
                        return Result.Success();
                    case 1: // DECV
                        this.caller.ptrVal--;
                        if(this.caller.ptrVal < 0){ this.caller.ptrVal = 0; }
                        return Result.Success();
                    case 2: // INCP
                        this.caller.ptr++;
                        if(this.caller.ptr >= this.caller.memory.length){ this.caller.ptr = 0; }
                        return Result.Success();
                    case 3: // DECP
                        this.caller.ptr--;
                        if(this.caller.ptr < 0){ this.caller.ptr = this.caller.memory.length - 1; }
                        return Result.Success();
                    case 4: // LOOP [
                        if(this.caller.memory[this.caller.ptr] == 0) {
                            this.progPos++;
                            while(this.c > 0 || !this.commands[this.progPos].equals("]")) {
                                
                                if(this.commands[this.progPos].equals("[")) {
                                    this.c++;
                                } else if(this.commands[this.progPos].equals("]")) {
                                    this.c--;
                                } else if(this.commands[this.progPos].equals("DPND")) {
                                    return Result.Error(Result.Errors.LOOPED_DEPENDENCY_SET, command + " at position: " + progPos);  
                                }
                                this.progPos++;
                            }
                        }
                        return Result.Success();
                    case 5: // ]
                        if(this.caller.memory[this.caller.ptr] != 0) {
                            this.progPos--;
                            while(this.c > 0 || !this.commands[this.progPos].equals("[")) {
                                
                                if(this.commands[this.progPos].equals("]")) {
                                    this.c++;
                                } else if(this.commands[this.progPos].equals("[")) {
                                    this.c--;
                                } else if(this.commands[this.progPos].equals("DPND")) {
                                    return Result.Error(Result.Errors.LOOPED_DEPENDENCY_SET, command + " at position: " + progPos);  
                                }
                                this.progPos--;
                            }
                        }
                        return Result.Success();
                    case 6: // WTV
                        this.caller.memory[this.caller.ptr] = this.caller.ptrVal;
                        return Result.Success();
                    case 7: // IN
                        Scanner sc = new Scanner(System.in);
                        int val = (int) sc.next().charAt(0);
                        sc.close();
                        this.caller.memory[this.caller.ptr] = val;
                        return Result.Success(); 
                    case 8: // PUSH
                        this.caller.stack[this.caller.stackPtr] = this.caller.ptrVal;
                        this.caller.ptrVal = 0;
                        this.caller.stackPtr++;
                        return Result.Success();
                    case 9: // POP
                        this.caller.ptrVal = this.caller.stack[this.caller.stackPtr];
                        this.caller.stack[this.caller.stackPtr] = 0;
                        this.caller.stackPtr--;
                        return Result.Success();
                    case 10: // CDP [F]
                        this.progPos++;
                        File f = new File(this.eBinCommands[this.progPos].getLiteral());
                        if(!f.exists()){ return Result.Error(Result.Errors.FILE_NOT_FOUND, this.eBinCommands[this.progPos].getLiteral() + " at position: " + this.progPos); }
                        this.progPos++;
                        this.caller.functions.put(this.eBinCommands[this.progPos].getLiteral(), f);
                        return Result.Success();
                    case 11: // CALL [F]
                        this.progPos++;
                        this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());
                        return Result.Success();
                    case 12: // RDV
                        this.caller.ptrVal = this.caller.memory[this.caller.ptr];
                        return Result.Success();
                    case 14: // HLT
                        return Result.Success();
                    case 15: // CLB [L]
                        this.progPos++;
                        this.caller.labels.put(this.eBinCommands[this.progPos].getLiteral(), this.caller.ptr);
                        return Result.Success();
                    case 16: // JLB [L]
                        this.progPos++;
                        if(this.caller.labels.containsKey(this.eBinCommands[this.progPos].getLiteral())){
                            this.caller.ptr = this.caller.labels.get(this.eBinCommands[this.progPos].getLiteral());
                        } else {
                            return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        return Result.Success();
                    case 17: // DLB [L]
                        this.progPos++;
                        if(this.caller.labels.containsKey(this.eBinCommands[this.progPos].getLiteral())){
                            this.caller.labels.remove(this.eBinCommands[this.progPos].getLiteral());
                        } else {
                            return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        return Result.Success();
                    case 18: // RDP
                        this.caller.ptrVal = this.caller.ptr;
                        return Result.Success();
                    case 19: // SET [N]
                        this.progPos++;
                        this.caller.ptrVal = this.eBinCommands[this.progPos].getData();
                        return Result.Success();
                    case 20: // MOVDN
                        this.caller.ptr -= 255;
                        if(this.caller.ptr < 0){ this.caller.ptr = (this.caller.memory.length - 1) + this.caller.ptr; }
                        return Result.Success();
                    case 21: // MOVUP
                        this.caller.ptr += 255;
                        if(this.caller.ptr >= this.caller.memory.length){ this.caller.ptr = this.caller.ptr - this.caller.memory.length; }
                        return Result.Success();
                    case 22: // MOV [N]
                        this.progPos++;
                        this.caller.ptr = this.eBinCommands[this.progPos].getData();
                        return Result.Success();
                    case 23: // INT [N/L C N/L] [F]
                        int firstVal;
                        int secondVal;
                        int comparison;
                        this.progPos++;
                        if(this.eBinCommands[this.progPos] instanceof eBinLabel){
                            if(this.caller.labels.containsKey(this.eBinCommands[this.progPos].getLiteral())){
                                firstVal = this.caller.labels.get(this.eBinCommands[this.progPos].getLiteral());
                            } else {
                                return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                            }
                        } else if(this.eBinCommands[this.progPos] instanceof eBinNumber){
                            firstVal = this.eBinCommands[this.progPos].getData();
                        } else {
                            return Result.Error(Result.Errors.INVALID_COMMAND, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        this.progPos++;
                        if(this.eBinCommands[this.progPos] instanceof eBinComparison){
                            comparison = this.eBinCommands[this.progPos].getData();
                        } else {
                            return Result.Error(Result.Errors.INVALID_COMPARISON, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        this.progPos++;
                        if(this.eBinCommands[this.progPos] instanceof eBinLabel){
                            if(this.caller.labels.containsKey(this.eBinCommands[this.progPos].getLiteral())){
                                secondVal = this.caller.labels.get(this.eBinCommands[this.progPos].getLiteral());
                            } else {
                                return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                            }
                        } else if(this.eBinCommands[this.progPos] instanceof eBinNumber){
                            secondVal = this.eBinCommands[this.progPos].getData();
                        } else {
                            return Result.Error(Result.Errors.INVALID_COMMAND, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        this.progPos++;
                        if(this.eBinCommands[this.progPos] instanceof eBinFunction){
                            if(this.caller.functions.containsKey(this.eBinCommands[this.progPos].getLiteral())){
                                switch(comparison){
                                    case 0:
                                        if(firstVal != secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    case 1:
                                        if(firstVal == secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    case 2:
                                        if(firstVal > secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    case 3:
                                        if(firstVal < secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    case 4:
                                        if(firstVal >= secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    case 5:
                                        if(firstVal <= secondVal){ this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());}
                                        break;
                                    default:
                                        return Result.Error(Result.Errors.INVALID_COMPARISON, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                                }
                            } else {
                                return Result.Error(Result.Errors.NONEXISTENT_FUNCTION, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                            }
                        } else {
                            return Result.Error(Result.Errors.INVALID_COMMAND, this.eBinCommands[this.progPos] + " at position: " + this.progPos);
                        }
                        return Result.Success();
                    case 24: // NOP
                        try{Thread.sleep(10);}catch(Exception e){};
                        return Result.Success();
                    case 25: // WRT [N]
                        this.progPos++;
                        this.caller.memory[this.caller.ptr] = this.eBinCommands[this.progPos].getData();
                        return Result.Success();
                    case 26: // OUT
                        System.out.print((char) this.caller.memory[this.caller.ptr]);
                        return Result.Success();
                    default:
                        return Result.Error(Result.Errors.INVALID_COMMAND, command + " at position: " + progPos);
                }
            default:
                return Result.Error(Result.Errors.INVALID_COMMAND, command + " at position: " + progPos);
        }
    }

    private eBinCommand[] buildAST(String[] commands){
        eBinCommand[] eBinCommands = new eBinCommand[commands.length];
        for(int i = 0; i < commands.length; i++){
            eBinCommands[i] = makeCommand(commands[i]);
        }
        return eBinCommands;
    }

    private eBinCommand makeCommand(String command){
        eBinCommand cmd;
        switch(command.charAt(0)){
            case 'F': // function names
                cmd = new eBinFunction(command.substring(1));
                break;
            case 'L': // label names
                cmd = new eBinLabel(command.substring(1));
                break;
            case 'N': // numbers
                cmd = new eBinNumber(command.substring(1));
                break;
            case 'C': // comparisons
                cmd = new eBinComparison(command.substring(1));
                break;
            case 'D': // files
                cmd = new eBinFile(command.substring(1));
                break;
            default: // instructions
                cmd = new eBinInstruction(command);
        }

        return cmd;
    }
}