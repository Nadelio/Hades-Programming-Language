package src.parser;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import src.Main;
import src.parser.Token.TokenType;
import src.util.Constants;

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
            if(Main.DEBUG_FLAG){ System.out.println(Constants.ANSI_MSG + "Reading Token: " + Constants.ANSI_INFO + this.ast.getTree()[this.readPosition].getLiteral() + Constants.ANSI_RESET); }
            this.tok = this.ast.getTree()[this.readPosition];
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    /**
    Reads the token at readPosition + offset without incrementing read pointer
    @param offset
    @return {@link Token}
    */
    private Token peekToken(int offset) {
        if (this.readPosition + offset >= this.ast.getTree().length) {
            return new Token(Token.TokenType.EOF, '\u0000');
        } else {
            return this.ast.getTree()[this.readPosition + offset];
        }
    }

    /**
     * Reads the next token without incrementing read pointer
     * @return {@link Token}
     */
    private Token peekToken(){
        return this.peekToken(0);
    }

    /**
    Parses the AST
    @return {@link ASTC} 
    */
    public ASTC parse(){
        java.util.ArrayList<Command> commands = new java.util.ArrayList<Command>();
        while(!this.tok.getType().equals(Token.TokenType.EOF)){
            Command cmd = this.nextCommand();
            if(Main.DEBUG_FLAG){System.out.println(Constants.ANSI_MSG + "Completed Command: " + Constants.ANSI_INFO + cmd.toString() + Constants.ANSI_RESET);}
            commands.add(cmd);
            this.readToken();
        }
        return new ASTC(commands.toArray(new Command[commands.size()]));
    }

    /**
    Generates the next command from the AST 
    @return {@link Command}
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
            case DROP:
                return new Command(Token.TokenType.DROP);
            case READTOHELDLABELPOSITION:
                return new Command(Token.TokenType.READTOHELDLABELPOSITION);
            case READTOHELDLABELVALUE:
                return new Command(Token.TokenType.READTOHELDLABELVALUE);
            case OUTNUMBER:
                return new Command(Token.TokenType.OUTNUMBER);
            case INVALUE:
                return new Command(Token.TokenType.INVALUE);
            case INSTRING:
                return new Command(Token.TokenType.INSTRING);
            case MOVE:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.MOVE, field1);
            case SET:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.SET, field1);
            case WRITE:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.WRITE, field1);
            case CREATELABEL:
                field1 = this.doLabelField();
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
            case HOLD:
                field1 = this.doAliasField();
                return new UnaryCommand(Token.TokenType.HOLD, field1);
            case MOVEHELDLABELPOSITION:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.MOVEHELDLABELPOSITION, field1);
            case SETHELDLABELVALUE:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.SETHELDLABELVALUE, field1);
            case OUTVALUE:
                field1 = this.doLabelField();
                return new UnaryCommand(Token.TokenType.OUTVALUE, field1);
            case OUTRANGE:
                Token.BuilderTypes[] patternSemantics = new Token.BuilderTypes[]{Token.BuilderTypes.SINGLE, Token.BuilderTypes.BIVARARGLIST, Token.BuilderTypes.SINGLE};
                field1 = this.buildPatternedField(patternSemantics, Token.TokenType.LBRACKET, Token.TokenType.NUMBER, Token.TokenType.ALIAS, Token.TokenType.RBRACKET);
                return new UnaryCommand(Token.TokenType.OUTRANGE, field1);
            case WRITEDATADUMP:
                Token.BuilderTypes[] patternSemantics2 = new Token.BuilderTypes[]{Token.BuilderTypes.SINGLE, Token.BuilderTypes.TRIVARARGLIST, Token.BuilderTypes.SINGLE};
                field1 = this.buildPatternedField(patternSemantics2, Token.TokenType.LBRACKET, Token.TokenType.NUMBER, Token.TokenType.ALIAS, Token.TokenType.STRING, Token.TokenType.RBRACKET);
                return new UnaryCommand(Token.TokenType.WRITEDATADUMP, field1);
            case FILESTREAMCLOSE:
            case READFROMFILE:
            case WRITETOFILE:
            case SETWRITEMODE:
                //TODO: Implement
                throw new UnsupportedOperationException("Not yet implemented");
            case SYSCALL:
                field1 = new Token[7];
                if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
                    this.readToken();
                    field1[0] = tok;
                    
                    if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
                        this.readToken();
                        field1[1] = tok;
                        if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
                            this.readToken();
                            field1[2] = tok;
                            if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
                                this.readToken();
                                field1[3] = tok;
                                if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
                                    this.readToken();
                                    field1[4] = tok;
                                    if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
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
        System.out.println(Constants.ANSI_ERROR + "Syntax error: expected " + Constants.ANSI_INFO + type + Constants.ANSI_ERROR + " @ " + Constants.ANSI_INFO + (this.readPosition) + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_ERROR + "Recieved: " + Constants.ANSI_INFO + this.peekToken().getType() + Constants.ANSI_RESET);
        System.exit(1);
    }

    private void exitWithError(String type){
        System.out.println(Constants.ANSI_ERROR + "Syntax error: expected " + Constants.ANSI_INFO + type + Constants.ANSI_ERROR + " @ " + Constants.ANSI_INFO + (this.readPosition) + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_ERROR + "Recieved: " + Constants.ANSI_INFO + this.peekToken().getType() + Constants.ANSI_RESET);
        System.exit(1);
    }

    private void exitWithError(Token.TokenType type1, Token.TokenType type2){
        System.out.println(Constants.ANSI_ERROR + "Syntax error: expected " + Constants.ANSI_INFO + type1 + Constants.ANSI_ERROR + " or " + Constants.ANSI_INFO + type2 + Constants.ANSI_ERROR + " @ " + Constants.ANSI_INFO + (this.readPosition) + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_ERROR + "Recieved: " + Constants.ANSI_INFO + this.peekToken().getType() + Constants.ANSI_RESET);
        System.exit(1);
    }

    private void exitWithError(Token.TokenType type1, Token.TokenType type2, Token.TokenType type3){
        System.out.println(Constants.ANSI_ERROR + "Syntax error: expected " + Constants.ANSI_INFO + type1 + Constants.ANSI_ERROR + ", " + Constants.ANSI_INFO + type2 + Constants.ANSI_ERROR + ", or " + Constants.ANSI_INFO + type3 + Constants.ANSI_ERROR + " @ " + Constants.ANSI_INFO + (this.readPosition) + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_ERROR + "Recieved: " + Constants.ANSI_INFO + this.peekToken().getType() + Constants.ANSI_RESET);
        System.exit(1);
    }

    private boolean isComparison(Token.TokenType type){ return match(type, Token.TokenType.GREATER, Token.TokenType.LESS, Token.TokenType.GREATEREQUAL, Token.TokenType.LESSEQUAL, Token.TokenType.EQUAL, Token.TokenType.NOTEQUAL); }
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

    private Token[] doLabelField(){
        Token[] field = new Token[3];
        if(this.peekToken().getType().equals(Token.TokenType.LBRACKET)){
            this.readToken();
            field[0] = tok;
            if(match(this.peekToken().getType(), Token.TokenType.ALIAS, Token.TokenType.NUMBER)){
                this.readToken();
                field[1] = tok;
                if(this.peekToken().getType().equals(Token.TokenType.RBRACKET)){
                    this.readToken();
                    field[2] = tok;
                } else {
                    exitWithError(Token.TokenType.RBRACKET);
                }
            } else {
                exitWithError(Token.TokenType.ALIAS, Token.TokenType.NUMBER);
            }
        } else {
            exitWithError(Token.TokenType.LBRACKET);
        }
        return field;
    }
    
    /**
     * Builds a token field using a pattern builder semantic array and a pattern of types
     * @param patternSemantics
     * @param pattern
     * @return An array containing a series of validated {@link Token} instances
     */
    public Token[] buildPatternedField(Token.BuilderTypes[] patternSemantics, Token.TokenType... pattern){
        if(patternSemantics.length > pattern.length){ throw new IllegalArgumentException("Pattern semantics cannot be bigger than pattern"); }
        
        Token[] field = new Token[0];
        int patternIndex = 0;

        int readLength = 0;

        for(Token.BuilderTypes b : patternSemantics){
            switch(b){
                case LIST -> readLength++;
                case SINGLE -> readLength++;
                case BIVARARG -> readLength += 2;
                case BIVARARGLIST -> readLength += 2;
                case TRIVARARG -> readLength += 3;
                case TRIVARARGLIST -> readLength += 3;
                default -> throw new IllegalArgumentException("Invalid pattern builder type");
            }
        }

        if(readLength != pattern.length){ throw new IllegalArgumentException("Semantic Builder Pattern must read the all of the Type Pattern. Builder Pattern Read Length Mismatch."); }

        for(Token.BuilderTypes b : patternSemantics){
            switch(b){
                case LIST -> {
                    Token.TokenType listType = pattern[patternIndex];
                    while(match(this.peekToken().getType(), listType)){
                        this.readToken();
                        field = appendElement(field, tok);
                    }
                    patternIndex++;
                }
                case SINGLE -> {
                    Token.TokenType type = pattern[patternIndex];
                    if(match(this.peekToken().getType(), type)){
                        this.readToken();
                        field = appendElement(field, tok);
                    } else { exitWithError(type); }
                    patternIndex++;
                }
                case BIVARARG -> {
                    Token.TokenType firstType = pattern[patternIndex], secondType = pattern[patternIndex + 1];
                    if(match(this.peekToken().getType(), firstType, secondType)){
                        this.readToken();
                        field = appendElement(field, tok);
                    } else { exitWithError(firstType, secondType); }
                    patternIndex += 2;
                }
                case BIVARARGLIST -> {
                    Token.TokenType firstType = pattern[patternIndex], secondType = pattern[patternIndex + 1];
                    while(match(this.peekToken().getType(), firstType, secondType)){
                        this.readToken();
                        field = appendElement(field, tok);
                    }
                    patternIndex += 2;
                }
                case TRIVARARG -> {
                    Token.TokenType firstType = pattern[patternIndex], secondType = pattern[patternIndex + 1], thirdType = pattern[patternIndex + 2];
                    if(match(this.peekToken().getType(), firstType, secondType, thirdType)){
                        this.readToken();
                        field = appendElement(field, tok);
                    } else { exitWithError(firstType, secondType, thirdType); }
                    patternIndex += 3;
                }
                case TRIVARARGLIST -> {
                    Token.TokenType firstType = pattern[patternIndex], secondType = pattern[patternIndex + 1], thirdType = pattern[patternIndex + 2];
                    while(match(this.peekToken().getType(), firstType, secondType, thirdType)){
                        this.readToken();
                        field = appendElement(field, tok);
                    }
                    patternIndex += 3;
                }
                default -> throw new IllegalArgumentException("Invalid pattern builder type");
            }
        }

        return field;
    }

    private Token[] appendElement(Token[] field, Token element){
        Token[] newField = new Token[field.length + 1];
        for(int i = 0; i < field.length; i++){ newField[i] = field[i]; }
        newField[field.length] = element;
        return newField;
    }
    
    /**
     * Checks if the input token matches any of the given types
     * @param input
     * @param types
     * @return {@link Boolean}
     */
    private boolean match(Token.TokenType input, Token.TokenType... types){
        return Stream.of(Token.TokenType.values()).anyMatch(t -> t.equals(input));
    }

    /**
     * Checks if the input tokens matches any of the given types
     * @param input
     * @param types
     * @return {@link Boolean}
     */
    private boolean matchMultiple(Token.TokenType[] input, Token.TokenType... types){
        for(Token.TokenType type : types){
            if(Stream.of(input).anyMatch(t -> !t.equals(type))){ return false; }
        }
        return true;
    }

    /**
     * Checks if the input tokens matches the given pattern
     * @param input
     * @param types
     * @return {@link Boolean}
     */
    private boolean matchPattern(Token.TokenType[] input, Token.TokenType... types){
        if(input.length != types.length){ return false; }
        return IntStream.range(0, input.length).allMatch(i -> input[i].equals(types[i]));
    }

    @SuppressWarnings("unused")
    private void printParserInfo(){
        System.out.println(Constants.ANSI_MSG + "Current Token: [" + Constants.ANSI_INFO + this.tok + Constants.ANSI_MSG + "]" + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_MSG + "Next Token: " + Constants.ANSI_INFO + this.peekToken().getLiteral() + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_MSG + "Reading Position: " + Constants.ANSI_INFO + this.readPosition + Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_MSG + "Current Position: " + Constants.ANSI_INFO + this.position + Constants.ANSI_RESET);
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