package src.parser;

public class BinaryCommand extends Command {
    private Token[] field1;
    private Token[] field2;

    public BinaryCommand(Token.TokenType kind, Token[] field1, Token[] field2) {
        super(kind);
        this.field1 = field1;
        this.field2 = field2;
    }

    public Token[] getField1() {return field1;}
    public Token[] getField2() {return field2;}
}