package src.parser;

public class FunctionMacroCommand extends Command {
    private Command[] body;
    private Token[] name;

    public FunctionMacroCommand(Command[] body, Token[] name) {
        super(Token.TokenType.FUNCTIONMACRO);
        this.body = body;
        this.name = name;
    }

    public Command[] getBody() {return body;}
    public Token[] getName() {return name;}
}
