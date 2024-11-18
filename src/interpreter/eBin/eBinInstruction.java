package src.interpreter.eBin;

public class eBinInstruction extends eBinCommand{
    public eBinInstruction(String literal){
        super(literal, eBinCommand.Types.INSTRUCTION, Integer.parseInt(literal));
    }
}
