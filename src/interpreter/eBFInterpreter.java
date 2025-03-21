package src.interpreter;

import src.parser.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

public class eBFInterpreter {
    private int progPos;
    File file;
    HadesInterpreter caller;
    String[] commands;
    int c; // for loops

    public eBFInterpreter(File file, HadesInterpreter caller){
        this.progPos = 0;
        this.file = file;
        this.caller = caller;
        this.c = 0;
    }

    public void interpret(){
        String code = "";

        try {
            code = Files.readString(this.file.toPath());
        } catch (IOException e) {
            Result.Error(Result.Errors.FILE_NOT_FOUND, file.getName() + " at position: " + progPos).handleError();
        }
        
        commands = code.split("");
        while (!Objects.equals(commands[this.progPos], "END")) {
            Result r = this.interpretCommand(commands[this.progPos]);
            if(!r.getSuccess()){
                r.handleError();
            }
            progPos++;
        }
    }

    private Result interpretCommand(String command){
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
                if(this.caller.memory[this.caller.ptr] == 0) {
                    this.progPos++;
                    while(c > 0 || !this.commands[this.progPos].equals("]")) {
                        
                        switch (this.commands[this.progPos]) {
                            case "[" -> c++;
                            case "]" -> c--;
                            case "DPND" -> {
                                return Result.Error(Result.Errors.LOOPED_DEPENDENCY_SET, command + " at position: " + progPos);
                            }
                        }
                        this.progPos++;
                    }
                }
                return Result.Success();
            case "]":
                if(this.caller.memory[this.caller.ptr] != 0) {
                    this.progPos--;
                    while(c > 0 || !this.commands[this.progPos].equals("[")) {
                        
                        switch (this.commands[this.progPos]) {
                            case "]" -> c++;
                            case "[" -> c--;
                            case "DPND" -> {
                                return Result.Error(Result.Errors.LOOPED_DEPENDENCY_SET, command + " at position: " + progPos);
                            }
                        }
                        this.progPos--;
                    }
                }
                return Result.Success();
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
                int val = sc.next().charAt(0);
                sc.close();
                this.caller.memory[this.caller.ptr] = val;
                return Result.Success();    
            case ">>":
                this.caller.stack[this.caller.stackPtr] = this.caller.ptrVal;
                this.caller.ptrVal = 0;
                this.caller.stackPtr++;
                return Result.Success();
            case "<<":
                this.caller.ptrVal = this.caller.stack[this.caller.stackPtr];
                this.caller.stack[this.caller.stackPtr] = 0;
                this.caller.stackPtr--;
                return Result.Success();
            case "DPND":
                File f = new File(this.commands[this.progPos + 1]);
                this.caller.externalFunctions.put(this.commands[this.progPos + 2], f);
                this.progPos += 2;
                return Result.Success();
            case "%":
                this.progPos++;
                return this.caller.callFunction(this.commands[this.progPos]);
            case "#":
                this.progPos++;
                this.caller.labels.put(this.commands[this.progPos], this.caller.ptr);
                return Result.Success();
            case "!#":
                this.progPos++;
                if(this.caller.labels.containsKey(this.commands[this.progPos])){
                    this.caller.labels.remove(this.commands[this.progPos]);
                } else {
                    return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.commands[this.progPos] + " at position: " + progPos);
                }
                return Result.Success();
            case "@":
                this.progPos++;
                if(this.caller.labels.containsKey(this.commands[this.progPos])){
                    this.caller.ptr = this.caller.labels.get(this.commands[this.progPos]);
                } else {
                    return Result.Error(Result.Errors.NONEXISTENT_LABEL, this.commands[this.progPos] + " at position: " + progPos);
                }
                return Result.Success();
            case "/*":  
                while(!Objects.equals(this.commands[this.progPos], "*/")){ this.progPos++; }
                this.progPos++;
                return Result.Success();
            case "END":
                return Result.Success();
            default:
                return Result.Error(Result.Errors.INVALID_COMMAND, command + " at position: " + progPos);
        }
    }
}