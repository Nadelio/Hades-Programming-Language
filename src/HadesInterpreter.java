package src;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class HadesInterpreter {
    // ast tree variables
    private ASTC ast;
    private int pos;
    private int readPos;
    private Command cmd;

    // interpreter variables
    private HashMap<String, Integer> labels = new HashMap<String, Integer>();
    private HashMap<String, File> functions = new HashMap<String, File>();
    private int[] memory = new int[65536];
    private int[] stack = new int[256];
    private int ptr = 0;
    private int ptrVal = 0;

    public HadesInterpreter(ASTC ast) {
        this.ast = ast;
        this.pos = 0;
        this.readPos = 0;
        this.cmd = null;
    }

    public void interpret(){
        while(this.cmd.getKind() != Token.TokenType.END){
            this.interpretCommand(this.cmd);
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
                System.out.println((char) memory[ptr]);
                return Result.Success();
            case IN:
                Scanner sc = new Scanner(System.in);
                int val = (int) sc.next().charAt(0);
                sc.close();
                this.memory[ptr] = val;
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
}
