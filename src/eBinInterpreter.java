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
                    case 0:
                        this.caller.ptrVal++;
                        return Result.Success();
                    case 1:
                        this.caller.ptrVal--;
                        if(this.caller.ptrVal < 0){ this.caller.ptrVal = 0; }
                        return Result.Success();
                    case 2:
                        this.caller.ptr++;
                        if(this.caller.ptr >= this.caller.memory.length){ this.caller.ptr = 0; }
                        return Result.Success();
                    case 3:
                        this.caller.ptr--;
                        if(this.caller.ptr < 0){ this.caller.ptr = this.caller.memory.length - 1; }
                        return Result.Success();
                    case 4:
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
                    case 5:
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
                    case 6:
                        this.caller.memory[this.caller.ptr] = this.caller.ptrVal;
                        return Result.Success();
                    case 7:
                        Scanner sc = new Scanner(System.in);
                        int val = (int) sc.next().charAt(0);
                        sc.close();
                        this.caller.memory[this.caller.ptr] = val;
                        return Result.Success(); 
                    case 8:
                        this.caller.stack[this.caller.stackPtr] = this.caller.ptrVal;
                        this.caller.ptrVal = 0;
                        this.caller.stackPtr++;
                        return Result.Success();
                    case 9:
                        this.caller.ptrVal = this.caller.stack[this.caller.stackPtr];
                        this.caller.stack[this.caller.stackPtr] = 0;
                        this.caller.stackPtr--;
                        return Result.Success();
                    case 10:
                        //TODO: create function // instruction function
                        this.progPos++;
                        this.caller.functions.put(this.eBinCommands[this.progPos].getLiteral(), );
                        return Result.Success();
                    case 11:
                        this.progPos++;
                        this.caller.callFunction(this.eBinCommands[this.progPos].getLiteral());
                        return Result.Success();
                    case 12:
                        this.caller.ptrVal = this.caller.memory[this.caller.ptr];
                        return Result.Success();
                    case 14:
                        return Result.Success();
                    case 15:
                        //TODO: create label // instruction label
                        return Result.Success();
                    case 16:
                        //TODO: jump label // instruction label
                        return Result.Success();
                    case 17:
                        //TODO: delete label // instruction label
                        return Result.Success();
                    case 18:
                        //TODO: read ptr to ptrval
                        return Result.Success();
                    case 19:
                        //TODO: SET [X] // instruction number
                        return Result.Success();
                    case 20:
                        //TODO: subtract 255 from ptr
                        //TODO: (ptr = |ptr - 255|; ptr > 0 -> ptr = memory.length - 1 - ptr;)
                        return Result.Success();
                    case 21:
                        //TODO: add 255 to ptr
                        //TODO: ptr > memory.length - 1 -> ptr = ptr - memory.length - 1;
                        return Result.Success();
                    case 22:
                        //TODO: MOV [X] // instruction number
                        return Result.Success();
                    case 23:
                        //TODO: INT [l/N C l/N] [f()] // instruction label/number comparison label/number function
                        return Result.Success();
                    case 24: // NOP
                        try{Thread.sleep(10);}catch(Exception e){};
                        return Result.Success();
                    case 25:
                        //TODO: WRT [X] // instruction number
                        return Result.Success();
                    default:
                        //TODO: Error handling
                        break;
                }
            default:
                return Result.Success();
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
            case 'F':
                cmd = new eBinFunction(command.substring(1));
                break;
            case 'L':
                cmd = new eBinLabel(command.substring(1));
                break;
            case 'N':
                cmd = new eBinNumber(command.substring(1));
                break;
            case 'C':
                cmd = new eBinComparison(command.substring(1));
                break;
            default:
                cmd = new eBinInstruction(command);
        }

        return cmd;
    }
}