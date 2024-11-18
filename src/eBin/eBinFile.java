package src.eBin;

public class eBinFile extends eBinCommand{
    public eBinFile(String path){
        super(path, eBinCommand.Types.FILE, -1);
    }
}
