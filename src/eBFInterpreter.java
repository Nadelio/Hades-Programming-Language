package src;

import java.io.File;
import java.util.Scanner;

public class eBFInterpreter {
    private int progPos;
    File file;
    HadesInterpreter caller;
    String[] commands;

    public eBFInterpreter(File file, HadesInterpreter caller){
        this.progPos = 0;
        this.file = file;
        this.caller = caller;
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
        while(commands[this.progPos] != "END"){
            Result r = this.interpretCommand(commands[this.progPos]);
            if(!r.getSuccess()){
                r.handleError();
            }
            progPos++;
        }
    }

    private Result interpretCommand(String command){
        int c = 0;
        switch(command){
            case "+":
                this.caller.ptrVal++;
                return Result.Success();
            case "-":
                this.caller.ptrVal--;
                if(this.caller.ptrVal < 0){ this.caller.ptrVal = 0; }
                return Result.Success();
            case ">":
                this.caller.ptr++;
                if(this.caller.ptr >= this.caller.memory.length){ this.caller.ptr = 0; }
                return Result.Success();
            case "<":
                this.caller.ptr--;
                if(this.caller.ptr < 0){ this.caller.ptr = this.caller.memory.length - 1; }
                return Result.Success();
            case "[":
                
            case "]":

            case "'":
                this.caller.ptrVal = this.caller.memory[this.caller.ptr];
                return Result.Success();
            case "\"":
                this.caller.ptrVal = this.caller.ptr;
                return Result.Success();
            case ",":
                this.caller.memory[this.caller.ptr] = this.caller.ptrVal;
                return Result.Success();
            case "=":
                System.out.print((char) this.caller.memory[this.caller.ptr]);
                return Result.Success();
            case ".":
                Scanner sc = new Scanner(System.in);
                int val = (int) sc.next().charAt(0);
                sc.close();
                this.caller.memory[this.caller.ptr] = val;
                return Result.Success();    
            case ">>":

            case "<<":

            case "DPND":

            case "%":

            case "#":

            case "!#":

            case "@":

            case "/*":
                while(this.commands[this.progPos] != "*/"){ this.progPos++; }
            case "END":
                return Result.Success();
            default:
                return Result.Error(Result.Errors.INVALID_COMMAND, command + " at position: " + progPos);
        }
    }
}