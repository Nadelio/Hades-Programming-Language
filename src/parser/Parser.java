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
    }

    /**
    Reads the next token from the AST and increments read pointer
    */
    private void readToken() {
        if (this.readPosition >= this.ast.getTree().length) {
            this.tok = new Token(Token.TokenType.EOF, '\u0000');
        } else {
            if(Main.DEBUG_FLAG){ System.out.println("\u001B[34mReading Token: \u001B[33m" + this.ast.getTree()[this.readPosition].getLiteral() + "\u001B[0m"); }
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

    /**
    Parses the AST
    @return ASTC 
    */
    public ASTC parse(){
        java.util.ArrayList<Command> commands = new java.util.ArrayList<Command>();
        while(!this.tok.getType().equals(Token.TokenType.EOF)){
            Command cmd = this.nextCommand();
            if(Main.DEBUG_FLAG){System.out.println("\u001B[34mCompleted Command: \u001B[33m" + cmd.toString() + "\u001B[0m");}
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
            case NOP:
                return new Command(Token.TokenType.NOP);
            case END:
                return new Command(Token.TokenType.END);
            case OUT:
                return new Command(Token.TokenType.OUT);
            case IN:
                return new Command(Token.TokenType.IN);
            case WRITEPOS:
                return new Command(Token.TokenType.WRITEPOS);
            case READPOS:
                return new Command(Token.TokenType.READPOS);
            case INCPOS:
                return new Command(Token.TokenType.INCPOS);
            case DECPOS:
                return new Command(Token.TokenType.DECPOS);
            case WRITEVAL:
                return new Command(Token.TokenType.WRITEVAL);
            case READVAL:
                return new Command(Token.TokenType.READVAL);
            case INCVAL:
                return new Command(Token.TokenType.INCVAL);
            case DECVAL:
                return new Command(Token.TokenType.DECVAL);
            case PUSH:
                return new Command(Token.TokenType.PUSH);
            case POP:
                return new Command(Token.TokenType.POP);
            case MOVE:
                field1 = this.doNumberField();
                return new UnaryCommand(Token.TokenType.MOVE, field1);
            case SET:
                field1 = this.doNumberField();
                return new UnaryCommand(Token.TokenType.SET, field1);
            case WRITE:
                field1 = this.doNumberField();
                return new UnaryCommand(Token.TokenType.WRITE, field1);
            case CREATELABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.CREATELABEL, field1);
            case DELETELABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.DELETELABEL, field1);
            case JUMPLABEL:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.JUMPLABEL, field1);
            case CALLDEPENDENCY:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.CALLDEPENDENCY, field1);
            case SYSCALL:
                field1 = new Token[7];
                if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType().equals(Token.TokenType.ALIAS) || this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                        this.readToken();
                        field1[1] = tok;
                        if(this.peekToken().getType().equals(Token.TokenType.ALIAS) || this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                            this.readToken();
                            field1[2] = tok;
                            if(this.peekToken().getType().equals(Token.TokenType.ALIAS) || this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                                this.readToken();
                                field1[3] = tok;
                                if(this.peekToken().getType().equals(Token.TokenType.ALIAS) || this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                                    this.readToken();
                                    field1[4] = tok;
                                    if(this.peekToken().getType().equals(Token.TokenType.ALIAS) || this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                                        this.readToken();
                                        field1[5] = tok;
                                        if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                                            this.readToken();
                                            field1[6] = tok;
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
            case COMMENT:
                ArrayList<Token> content = new ArrayList<Token>();
                while((!this.peekToken().getType().equals(Token.TokenType.COMMENT)) && (this.position < this.ast.getTree().length)){
                    this.readToken();
                    content.add(this.tok);
                }
                this.readToken();
                return new UnaryCommand(Token.TokenType.COMMENT, content.toArray(new Token[content.size()]));
            case CREATEDEPENDENCY: //! need to update interpreter and compiler to handle STRING argument
                field2 = new Token[3];
                
                // build field1
                if(!Main.EPU_FLAG){
                    field1 = new Token[5];
                    if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                        this.readToken();
                        field1[0] = tok;
                        if(this.peekToken().getType().equals(Token.TokenType.ALIAS)){
                            this.readToken();
                            field1[1] = tok;
                            if(this.peekToken().getType().equals(Token.TokenType.FILEINDENTIFIER)){
                                this.readToken();
                                field1[2] = tok;
                                if(this.peekToken().getType().equals(Token.TokenType.EXTENSION)){
                                    this.readToken();
                                    field1[3] = tok;
                                    if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
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
                        } else if(this.peekToken().getType().equals(Token.TokenType.STRING)){
                            field1 = new Token[3];
                            field1[0] = new Token(Token.TokenType.LBRACKET, '[');
                            this.readToken();
                            field1[1] = tok;
                            if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                                this.readToken();
                                field1[2] = tok;
                            } else {
                                exitWithError(Token.TokenType.RBRACKET);
                            }
                        } else {
                            exitWithError(Token.TokenType.ALIAS, Token.TokenType.STRING);
                        }
                    } else {
                        exitWithError(Token.TokenType.LBRACKET);
                    }
                } else {
                    field1 = new Token[4];
                    if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                        this.readToken();
                        field1[0] = tok;
                        if(this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                            this.readToken();
                            field1[1] = tok;
                            if(this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                                this.readToken();
                                field1[2] = tok;
                                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
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
            case INTERRUPT:
                field1 = new Token[5];
                field2 = new Token[3];

                // build field1
                if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                    this.readToken();
                    field1[0] = tok;
                    if(this.peekToken().getType().equals(Token.TokenType.ALIAS)){
                        this.readToken();
                        field1[1] = tok;
                        if(this.isComparison(this.peekToken().getType())){
                            this.readToken();
                            field1[2] = tok;
                            if(this.peekToken().getType().equals(Token.TokenType.ALIAS)){
                                this.readToken();
                                field1[3] = tok;
                                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                                    this.readToken();
                                    field1[4] = tok;
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
            case LOOP:
                ArrayList<Command> loopBody = new ArrayList<Command>();
                if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                    this.readToken();
                } else { exitWithError(Token.TokenType.LBRACKET); }
                while(!this.peekToken().getType().equals(Token.TokenType.RBRACKET) && this.position < this.ast.getTree().length){
                    this.readToken();
                    Command c = this.nextCommand();
                    loopBody.add(c);
                }
                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                    this.readToken();
                    return new LoopCommand(loopBody.toArray(new Command[loopBody.size()]));
                } else { exitWithError(Token.TokenType.RBRACKET); }
                break;
            default: break;
        }
        return new Command(Token.TokenType.INVALID);
    }

    private void exitWithError(Token.TokenType type){
        System.out.println("\u001B[31mSyntax error: expected \u001B[33m" + type + "\u001B[31m @ \u001B[33m" + (this.readPosition) + "\u001B[0m");
        System.out.println("\u001B[31mRecieved: \u001B[33m" + this.peekToken().getType() + "\u001B[0m");
        System.exit(1);
    }

    private void exitWithError(String type){
        System.out.println("\u001B[31mSyntax error: expected \u001B[33m" + type + "\u001B[31m @ \u001B[33m" + (this.readPosition) + "\u001B[0m");
        System.out.println("\u001B[31mRecieved: \u001B[33m" + this.peekToken().getType() + "\u001B[0m");
        System.exit(1);
    }

    private void exitWithError(Token.TokenType type1, Token.TokenType type2){
        System.out.println("\u001B[31mSyntax error: expected \u001B[33m" + type1 + "\u001B[31m or \u001B[33m" + type2 + "\u001B[31m @ \u001B[33m" + (this.readPosition) + "\u001B[0m");
        System.out.println("\u001B[31mRecieved: \u001B[33m" + this.peekToken().getType() + "\u001B[0m");
        System.exit(1);
    }

    private boolean isComparison(Token.TokenType type){ return type.equals(Token.TokenType.EQUAL) || type.equals(Token.TokenType.NOTEQUAL) || type.equals(Token.TokenType.GREATER) || type.equals(Token.TokenType.LESS) || type.equals(Token.TokenType.GREATEREQUAL) || type.equals(Token.TokenType.LESSEQUAL); }
    private Token[] doAliasField(){
        Token[] field = new Token[3];
        if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
            this.readToken();
            field[0] = tok;
            if(this.peekToken().getType().equals(Token.TokenType.ALIAS)){
                this.readToken();
                field[1] = tok;
                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                    this.readToken();
                    field[2] = tok;
                } else {
                    exitWithError(Token.TokenType.LBRACKET);
                }
            } else {
                exitWithError(Token.TokenType.ALIAS);
            }
        } else {
            exitWithError(Token.TokenType.RBRACKET);
        }
        return field;
    }

    private Token[] doNumberField(){
        Token[] field = new Token[3];
        if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
            this.readToken();
            field[0] = tok;
            if(this.peekToken().getType().equals(Token.TokenType.NUMBER)){
                this.readToken();
                field[1] = tok;
                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                    this.readToken();
                    field[2] = tok;
                } else {
                    exitWithError(Token.TokenType.LBRACKET);
                }
            } else {
                exitWithError(Token.TokenType.NUMBER);
            }
        } else {
            exitWithError(Token.TokenType.RBRACKET);
        }
        return field;
    }

    @SuppressWarnings("unused")
    private void printParserInfo(){
        System.out.println("\u001B[34mCurrent Token: [\u001B[33m" + this.tok + "\u001B[34m]\u001B[0m");
        System.out.println("\u001B[34mNext Token: \u001B[33m" + this.peekToken().getLiteral() + "\u001B[0m");
        System.out.println("\u001B[34mReading Position: \u001B[33m" + this.readPosition + "\u001B[0m");
        System.out.println("\u001B[34mCurrent Position: \u001B[33m" + this.position + "\u001B[0m");
    }

    @SuppressWarnings("unused")
    private void printTree(){
        System.out.println("[");
        for(Token t : this.ast.getTree()){
            System.out.println("  [ " + t + " ],");
        }
        System.out.println("\n]");
    }
}