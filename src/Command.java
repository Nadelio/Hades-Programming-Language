package src;
import src.Token.TokenType;

public class Command {
    public Token.TokenType kind;
    public Command(Token.TokenType kind) {this.kind = kind;}
    public Token.TokenType getKind() {return kind;}
}