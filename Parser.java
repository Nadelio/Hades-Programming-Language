import java.util.ArrayList;

public class Parser {

    private AST ast;
    private int position;
    private int readPosition;
    private Token tok;

    public Parser(Token[] tokens) {
        this.ast = new AST(tokens);
        this.position = 0;
        this.readPosition = 0;
        this.tok = new Token(Token.TokenType.INVALID, '\u0000');
        this.readToken();
    }

    /**
    Reads the next token from the AST and increments read pointer
    */
    private void readToken() {
        if (this.readPosition >= this.ast.getTree().length) {
            this.tok = new Token(Token.TokenType.EOF, '\u0000');
        } else {
            this.tok = this.ast.getTree()[this.readPosition];
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    /**
    Reads the next token without incrementing read pointer
    @return Token
    */
    private Token peekToken() {
        if (this.readPosition >= this.ast.getTree().length) {
            return new Token(Token.TokenType.EOF, '\u0000');
        } else {
            return this.ast.getTree()[this.readPosition];
        }
    }

    public ASTC parse(){
        java.util.ArrayList<Command> commands = new java.util.ArrayList<Command>();
        while(this.tok.getType() != Token.TokenType.EOF){
            Command cmd = this.nextCommand();
            commands.add(cmd);
            this.readToken();
        }
        return new ASTC(commands.toArray(new Command[commands.size()]));
    }

    private Command nextCommand(){
        Token[] field1;
        Token[] field2;
        switch(this.tok.getType()){
            case Token.TokenType.NOP:
                return new Command(Token.TokenType.NOP);
            case Token.TokenType.END:
                return new Command(Token.TokenType.END);
            case Token.TokenType.WRITEPOS:
                return new Command(Token.TokenType.WRITEPOS);
            case Token.TokenType.READPOS:
                return new Command(Token.TokenType.READPOS);
            case Token.TokenType.INCPOS:
                return new Command(Token.TokenType.INCPOS);
            case Token.TokenType.DECPOS:
                return new Command(Token.TokenType.DECPOS);
            case Token.TokenType.WRITEVAL:
                return new Command(Token.TokenType.WRITEVAL);
            case Token.TokenType.READVAL:
                return new Command(Token.TokenType.READVAL);
            case Token.TokenType.INCVAL:
                return new Command(Token.TokenType.INCVAL);
            case Token.TokenType.DECVAL:
                return new Command(Token.TokenType.DECVAL);
            case Token.TokenType.PUSH:
                return new Command(Token.TokenType.PUSH);
            case Token.TokenType.POP:
                return new Command(Token.TokenType.POP);
            case Token.TokenType.MOVE:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.NUMBER){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.MOVE, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
            case Token.TokenType.SET:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.NUMBER){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.SET, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
            case Token.TokenType.WRITE:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.NUMBER){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.WRITE, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
                case Token.TokenType.CREATELABEL:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.ALIAS){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.CREATELABEL, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
                case Token.TokenType.DELETELABEL:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.ALIAS){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.DELETELABEL, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
                case Token.TokenType.JUMPLABEL:
                field1 = new Token[3];
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType() == Token.TokenType.ALIAS){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.JUMPLABEL, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
            case Token.TokenType.CALLDEPENDENCY:
            field1 = new Token[3];
            if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                this.readToken();
                field1[0] = tok;
                if(this.peekToken().getType() == Token.TokenType.ALIAS){
                    this.readToken();
                    field1[1] = tok;
                    if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                            this.readToken();
                            field1[2] = tok;
                            return new UnaryCommand(Token.TokenType.CALLDEPENDENCY, field1);
                        } else {
                            exitWithError();
                        }
                    } else {
                        exitWithError();
                    }
                } else {
                    exitWithError();
                }
                break;
            case Token.TokenType.SYSCALL:
                field1 = new Token[7];
                // TODO: syscall
                // ? 5 labels OR numbers
                break;
            case Token.TokenType.COMMENT:
                ArrayList<Token> content = new ArrayList<Token>();
                // TODO: comment
                // ? add to content until sees comment token again
                break;
            case Token.TokenType.CREATEDEPENDENCY:
                //TODO: create dependency
                break;
            case Token.TokenType.INTERRUPT:
                //TODO: interrupt
                break;
            case Token.TokenType.LOOP:
                //TODO: loop
                break;
        }
        return new Command(Token.TokenType.INVALID);
    }

    private void exitWithError(){
        System.out.println("Syntax error: expected LBRACKET @ " + this.position);
        System.out.println("Recieved: " + this.peekToken().getType());
        System.exit(1);
    }
}