package src.eBin;

public class eBinLabel extends eBinCommand {
    public eBinLabel(String literal){
        super(literal, eBinCommand.Types.LABEL, Integer.parseInt(literal));
    }
}
