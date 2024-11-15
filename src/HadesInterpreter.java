package src;

import java.io.File;
import java.util.HashMap;
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

    public Result interpretCommand(Command cmd){ //TODO: add try-catches to the Integer.parseInt code
        switch(cmd.getKind()){
            case MOVE:
                UnaryCommand move = (UnaryCommand) cmd;
                return this.move(Integer.parseInt(move.getField()[1].getLiteral()));
            case SET:
                UnaryCommand set = (UnaryCommand) cmd;
                return this.set(Integer.parseInt(set.getField()[1].getLiteral()));
            case 
        }

        return Result.Success();
    }

    private Result move(int val){
        this.ptr = val;
        return Result.Success();
    }
}
