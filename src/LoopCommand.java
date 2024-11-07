package src;
import src.Token.TokenType;
public class LoopCommand extends Command {
    private Command[] body;

    public LoopCommand(Command[] body) {
        super(Token.TokenType.LOOP);
        this.body = body;
    }

    public Command[] getBody() {return body;}
}
