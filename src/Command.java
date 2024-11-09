package src;

public class Command {
    private Token.TokenType kind;
    public Command(Token.TokenType kind) {this.kind = kind;}
    public Token.TokenType getKind() {return kind;}

    public String toString() {
        return kind.toString();
    }
}