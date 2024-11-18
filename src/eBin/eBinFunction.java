package src.eBin;

public class eBinFunction extends eBinCommand {
    public eBinFunction(String literal){
        super(literal, eBinCommand.Types.FUNCTION, Integer.parseInt(literal));
    }
}
