package src.parser;

import java.util.ArrayList;

import src.Main;
import src.parser.Token.TokenType;

public class Parser {

    private AST ast;
    private int position;
    private int readPosition;
    private Token tok;

    public Parser(Token[] tokens) {
        this.ast = new AST(tokens);
        this.position = -1;
        this.readPosition = 0;
        this.tok = new Token(Token.TokenType.INVALID, '\u0000');
        this.readToken();
        System.out.print("AST: [");
        for(Token t : this.ast.getTree()){
            System.out.print(t + ", ");
        }
        System.out.println("]");
    }

    /**
    Reads the next token from the AST and increments read pointer
    */
    private void readToken() {
        System.out.println("Reading Token: " + this.ast.getTree()[this.readPosition].getLiteral());
        if(Main.DEBUG_FLAG){ System.out.println("Reading Token: " + this.ast.getTree()[this.readPosition].getLiteral()); }
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
        if(Main.DEBUG_FLAG){ System.out.println("Peeking Token: " + this.ast.getTree()[this.readPosition].getLiteral()); }
        if (this.readPosition >= this.ast.getTree().length) {
            return new Token(Token.TokenType.EOF, '\u0000');
        } else {
            return this.ast.getTree()[this.readPosition];
        }
    }

    /**
    Parses the AST
    @return ASTC 
    */
    public ASTC parse(){
        java.util.ArrayList<Command> commands = new java.util.ArrayList<Command>();
        while(this.tok.getType() != Token.TokenType.EOF){
            Command cmd = this.nextCommand();
            if(Main.DEBUG_FLAG){System.out.println("Completed Command: " + cmd.toString());}
            commands.add(cmd);
            this.readToken();
        }
        return new ASTC(commands.toArray(new Command[commands.size()]));
    }

    /**
    Generates the next command from the AST 
    @return Command
     */
    private Command nextCommand(){
        Token[] field1;
        Token[] field2;
        // build command
        switch(this.tok.getType()){
            case Token.TokenType.NOP:
                return new Command(Token.TokenType.NOP);
            case Token.TokenType.END:
                return new Command(Token.TokenType.END);
            case Token.TokenType.OUT:
                return new Command(Token.TokenType.OUT);
            case Token.TokenType.IN:
                return new Command(Token.TokenType.IN);
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
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.MOVE, field1);
            case Token.TokenType.SET:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.SET, field1);
            case Token.TokenType.WRITE:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.WRITE, field1);
            case Token.TokenType.CREATELABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.CREATELABEL, field1);
            case Token.TokenType.DELETELABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.DELETELABEL, field1);
            case Token.TokenType.JUMPLABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.JUMPLABEL, field1);
            case Token.TokenType.CALLDEPENDENCY:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.CALLDEPENDENCY, field1);
            case Token.TokenType.SYSCALL:
                this.readToken();
                field1 = new Token[7];
                if(this.tok.getType() == Token.TokenType.LBRACKET){
                    field1[0] = tok;
                    this.readToken();
                    if(this.tok.getType() == Token.TokenType.ALIAS || this.peekToken().getType() == Token.TokenType.NUMBER){
                        field1[1] = tok;
                        this.readToken();
                        if(this.tok.getType() == Token.TokenType.ALIAS || this.peekToken().getType() == Token.TokenType.NUMBER){
                            field1[2] = tok;
                            this.readToken();
                            if(this.tok.getType() == Token.TokenType.ALIAS || this.peekToken().getType() == Token.TokenType.NUMBER){
                                field1[3] = tok;
                                this.readToken();
                                if(this.tok.getType() == Token.TokenType.ALIAS || this.peekToken().getType() == Token.TokenType.NUMBER){
                                    field1[4] = tok;
                                    this.readToken();
                                    if(this.tok.getType() == Token.TokenType.ALIAS || this.peekToken().getType() == Token.TokenType.NUMBER){
                                        field1[5] = tok;
                                        this.readToken();
                                        if(this.tok.getType() == Token.TokenType.RBRACKET){
                                            field1[6] = tok;
                                            this.readToken();
                                            return new UnaryCommand(Token.TokenType.SYSCALL, field1);
                                        } else {
                                            exitWithError(Token.TokenType.RBRACKET);
                                        }
                                    } else {
                                        exitWithError(TokenType.ALIAS, Token.TokenType.NUMBER);
                                    }
                                } else {
                                    exitWithError(TokenType.ALIAS, Token.TokenType.NUMBER);
                                }
                            } else {
                                exitWithError(TokenType.ALIAS, Token.TokenType.NUMBER);
                            }
                        } else {
                            exitWithError(TokenType.ALIAS, Token.TokenType.NUMBER);
                        }
                    } else {
                        exitWithError(TokenType.ALIAS, Token.TokenType.NUMBER);
                    }
                } else {
                    exitWithError(Token.TokenType.LBRACKET);
                }
                break;
            case Token.TokenType.COMMENT:
                ArrayList<Token> content = new ArrayList<Token>();
                while(this.tok.getType() != Token.TokenType.COMMENT){
                    content.add(this.tok);
                    this.readToken();
                }
                return new UnaryCommand(Token.TokenType.COMMENT, content.toArray(new Token[content.size()]));
            case Token.TokenType.CREATEDEPENDENCY:
                field2 = new Token[3];
                
                // build field1
                if(!Main.EPU_FLAG){
                    field1 = new Token[5];
                    if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                        this.readToken();
                        field1[0] = tok;
                        if(this.peekToken().getType() == Token.TokenType.ALIAS){
                            this.readToken();
                            field1[1] = tok;
                            if(this.peekToken().getType() == Token.TokenType.FILEINDENTIFIER){
                                this.readToken();
                                field1[2] = tok;
                                if(this.peekToken().getType() == Token.TokenType.EXTENSION){
                                    this.readToken();
                                    field1[3] = tok;
                                    if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                                        this.readToken();
                                        field1[4] = tok;
                                    } else {
                                        exitWithError(Token.TokenType.RBRACKET);
                                    }
                                } else {
                                    exitWithError(Token.TokenType.EXTENSION);
                                }
                            } else {
                                exitWithError(Token.TokenType.FILEINDENTIFIER);
                            }
                        } else {
                            exitWithError(Token.TokenType.ALIAS);
                        }
                    } else {
                        exitWithError(Token.TokenType.LBRACKET);
                    }
                } else {
                    field1 = new Token[4];
                    System.out.println("Next Token: " + this.peekToken().getType());
                    System.out.println("Reading Position: " + this.readPosition);
                    System.out.println("Current Position: " + this.position);
                    if(this.peekToken().getType() == Token.TokenType.LBRACKET){
                        this.readToken();
                        field1[0] = tok;
                        if(this.peekToken().getType() == Token.TokenType.NUMBER){
                            this.readToken();
                            field1[1] = tok;
                            if(this.peekToken().getType() == Token.TokenType.NUMBER){
                                this.readToken();
                                field1[2] = tok;
                                if(this.peekToken().getType() == Token.TokenType.RBRACKET){
                                    this.readToken();
                                    field1[3] = tok;
                                } else {
                                    exitWithError(Token.TokenType.RBRACKET);
                                }
                            } else {
                                exitWithError(Token.TokenType.NUMBER);
                            }
                        } else {
                            exitWithError(Token.TokenType.NUMBER);
                        }
                    } else {
                        exitWithError(Token.TokenType.LBRACKET);
                    }
                }

                // build field2
                field2 = this.doAliasField();

                return new BinaryCommand(Token.TokenType.CREATEDEPENDENCY, field1, field2);
            case Token.TokenType.INTERRUPT:
                this.readToken();
                field1 = new Token[5];
                field2 = new Token[3];

                // build field1
                if(this.tok.getType() == Token.TokenType.LBRACKET){
                    field1[0] = tok;
                    this.readToken();
                    if(this.tok.getType() == Token.TokenType.ALIAS){
                        field1[1] = tok;
                        this.readToken();
                        if(this.isComparison(this.tok.getType())){
                            field1[2] = tok;
                            this.readToken();
                            if(this.tok.getType() == Token.TokenType.ALIAS){
                                field1[3] = tok;
                                this.readToken();
                                if(this.tok.getType() == Token.TokenType.RBRACKET){
                                    field1[4] = tok;
                                    this.readToken();
                                } else {
                                    exitWithError(Token.TokenType.RBRACKET);
                                }
                            } else {
                                exitWithError(Token.TokenType.ALIAS);
                            }
                        } else {
                            exitWithError("Comparision");
                        }
                    } else {
                        exitWithError(Token.TokenType.ALIAS);
                    }
                } else {
                    exitWithError(Token.TokenType.LBRACKET);
                }

                // build field2
                field2 = this.doAliasField();

                return new BinaryCommand(Token.TokenType.INTERRUPT, field1, field2);
            case Token.TokenType.LOOP:
                ArrayList<Command> loopBody = new ArrayList<Command>();
                if(this.peekToken().getType() == Token.TokenType.LBRACKET){ this.readToken(); } else { exitWithError(Token.TokenType.LBRACKET); }
                while(this.peekToken().getType() != Token.TokenType.RBRACKET){ loopBody.add(this.nextCommand()); }
                if(this.peekToken().getType() == Token.TokenType.RBRACKET){ this.readToken();
                    return new LoopCommand(loopBody.toArray(new Command[loopBody.size()]));
                } else { exitWithError(Token.TokenType.RBRACKET); }
                break;
            default: break;
        }
        return new Command(Token.TokenType.INVALID);
    }

    private void exitWithError(Token.TokenType type){
        System.out.println("Syntax error: expected " + type + " @ " + (this.position + 1));
        System.out.println("Recieved: " + this.peekToken().getType());
        System.exit(1);
    }

    private void exitWithError(String type){
        System.out.println("Syntax error: expected " + type + " @ " + (this.position + 1));
        System.out.println("Recieved: " + this.peekToken().getType());
        System.exit(1);
    }

    private void exitWithError(Token.TokenType type1, Token.TokenType type2){
        System.out.println("Syntax error: expected " + type1 + " or " + type2 + " @ " + (this.position + 1));
        System.out.println("Recieved: " + this.peekToken().getType());
        System.exit(1);
    }

    private boolean isComparison(Token.TokenType type){ return type == Token.TokenType.EQUAL || type == Token.TokenType.NOTEQUAL || type == Token.TokenType.GREATER || type == Token.TokenType.LESS || type == Token.TokenType.GREATEREQUAL || type == Token.TokenType.LESSEQUAL; }
    private Token[] doAliasField(){
        Token[] field = new Token[3];
        if(this.tok.getType() == Token.TokenType.LBRACKET){
            field[0] = tok;
            this.readToken();
            if(this.tok.getType() == Token.TokenType.ALIAS){
                field[1] = tok;
                this.readToken();
                if(this.tok.getType() == Token.TokenType.RBRACKET){
                    field[2] = tok;
                    this.readToken();
                } else {
                    exitWithError(this.peekToken().getType());
                }
            } else {
                exitWithError(this.peekToken().getType());
            }
        } else {
            exitWithError(this.peekToken().getType());
        }
        return field;
    }
}