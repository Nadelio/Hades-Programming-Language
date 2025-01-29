package src.interpreter.eBin;

public class eBinString extends eBinCommand{

    private int length;
    private String content;

    public eBinString(int length, String content){
        super(content, eBinCommand.Types.STRING, -1);
        this.length = length;
        this.content = content;
    }

    public static String buildString(String[] commands, int beginningIndex, int length){
        StringBuilder sb = new StringBuilder();
        for(int i = beginningIndex; i < beginningIndex + length; i++){
            sb.append((char) Integer.parseInt(commands[i].substring(1)));
        }
        return sb.toString();
    }

    public int getLength(){ return this.length; }
    public String getContent(){ return this.content; }
}
