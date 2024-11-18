package src.parser;
public class UnaryCommand extends Command {
    /*
    field[0] = [
    field[1] = data
    field[2] = ]

    EXCEPT: SYSCALL
    field[0] = [
    field[1] = arg1
    field[2] = arg2
    field[3] = arg3
    field[4] = arg4
    field[5] = arg5
    field[6] = ]
    */
    private Token[] field;

    public UnaryCommand(Token.TokenType kind, Token[] field) {
        super(kind);
        this.field = field;
    }

    public Token[] getField() {return field;}
}
