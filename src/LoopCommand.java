package src;
public class LoopCommand extends Command {
    private Command[] body;

    public LoopCommand(Command[] body) {
        super(Token.TokenType.LOOP);
        this.body = body;
    }

    public Command[] getBody() {return body;}
}
