public class UnaryCommand extends Command {
    private Token[] field;

    public UnaryCommand(Token.TokenType kind, Token[] field) {
        super(kind);
        this.field = field;
    }

    public Token[] getField() {return field;}
}
