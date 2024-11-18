package src.eBin;

public class eBinComparison extends eBinCommand {
    public eBinComparison(String literal){
        super(literal, eBinCommand.Types.COMPARISON, Integer.parseInt(literal));
    }
}
