package src.interpreter.eBin;

public class eBinCommand {
    private String literal;
    private eBinCommand.Types type;
    private int data;

    public enum Types{
        FUNCTION,
        LABEL,
        NUMBER,
        COMPARISON,
        STRING,
        INSTRUCTION
    }

    public eBinCommand(String literal, eBinCommand.Types type, int data){
        this.literal = literal;
        this.type = type;
        this.data = data;
    }

    public String getLiteral(){return this.literal;}
    public eBinCommand.Types getType(){return this.type;}
    public int getData(){return this.data;}
}
