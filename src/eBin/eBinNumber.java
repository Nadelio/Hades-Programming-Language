package src.eBin;

public class eBinNumber extends eBinCommand{
    public eBinNumber(String literal){
        super(literal, eBinCommand.Types.NUMBER, Integer.parseInt(literal));
    }
}
