package src.parser;

import src.Main;

public class Lexer {
    
    private String input;
    private int position;
    private int readPosition;
    private char ch;

    public Lexer(String hadesCode) {
        this.input = hadesCode;
        this.position = -1;
        this.readPosition = 0;
        this.ch = '\u0000';
        this.readChar();
    }

    private void readChar() {
        if (this.readPosition >= this.input.length()) {
            this.ch = '\u0000';
        } else {
            if(Main.DEBUG_FLAG){ System.out.println("\u001B[34mReading Character: \u001B[33m" + this.input.charAt(this.readPosition) + "\u001B[0m"); }
            this.ch = this.input.charAt(this.readPosition);
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    private char peekChar() {
        if (this.readPosition >= this.input.length()) {
            return '\u0000';
        } else {
            if(Main.DEBUG_FLAG){ System.out.println("\u001B[34mPeeking Character: \u001B[33m" + this.input.charAt(this.readPosition) + "\u001B[0m"); }
            return this.input.charAt(this.readPosition);
        }
    }

    public Token[] lex(){
        java.util.ArrayList<Token> tokens = new java.util.ArrayList<Token>();
        while(this.ch != '\u0000'){
            Token tok = this.nextToken();
            if(Main.DEBUG_FLAG){System.out.println("\u001B[34mCompleted Command: \u001B[33m" + tok.toString() + "\u001B[0m");}
            tokens.add(tok);
            this.readChar();
        }
        return tokens.toArray(new Token[tokens.size()]);
    }

    private Token nextToken(){
        this.skipWhitespace();
        Token tok;

        switch(this.ch){
            case ',':
                tok = new Token(Token.TokenType.COMMA, this.ch);
                break;
            case ';':
                tok = new Token(Token.TokenType.COMMENT, this.ch);
                break;
            case '[':
                tok = new Token(Token.TokenType.LBRACKET, this.ch);
                break;
            case ']':
                tok = new Token(Token.TokenType.RBRACKET, this.ch);
                break;
            case '\u0000':
                tok = new Token(Token.TokenType.EOF, this.ch);
                break;
            case '<':
                if(this.peekChar() == '='){
                    this.readChar();
                    tok = new Token(Token.TokenType.LESSEQUAL, "<=");
                } else {
                    tok = new Token(Token.TokenType.LESS, "<");
                }
                break;
            case '>':
                if(this.peekChar() == '='){
                    this.readChar();
                    tok = new Token(Token.TokenType.GREATEREQUAL, ">=");
                } else {
                    tok = new Token(Token.TokenType.GREATER, ">");
                }
                break;
            case '=':
                if(this.peekChar() == '='){
                    this.readChar();
                    tok = new Token(Token.TokenType.EQUAL, "==");
                } else {
                    tok = new Token(Token.TokenType.INVALID, this.ch);
                }
                break;
            case '!':
                if(this.peekChar() == '='){
                    this.readChar();
                    tok = new Token(Token.TokenType.NOTEQUAL, "!=");
                } else {
                    tok = new Token(Token.TokenType.INVALID, this.ch);
                }
                break;
            case '.':
                tok = new Token(Token.TokenType.FILEINDENTIFIER, this.ch);
                break;
            default:
                if(isLetter(this.ch)){
                    String literal = this.readIdentifier();
                    tok = matchKeyword(literal);
                } else if(isNumber(this.ch)){
                    int literal = this.readNumber();
                    tok = new Token(Token.TokenType.NUMBER, literal);
                } else {
                    tok = new Token(Token.TokenType.INVALID, this.ch);
                }
                break;
        }
        
        return tok;
    }

    /**
    Moves the lexer char pointer and read pointer back once
     */
    private void backtrack(){
        if (this.position <= 0) {
            this.ch = '\u0000';
        } else {
            this.ch = this.input.charAt(this.position);
        }
        this.position--;
        this.readPosition--;
    }

    private int readNumber(){
        int pos = this.position;
        while(isNumber(this.ch)){
            this.readChar();
        }
        int num = Integer.parseInt(this.input.substring(pos, this.position));
        backtrack();
        return num;
    }

    private String readIdentifier(){
        int pos = this.position;
        while(isLetter(this.ch)){
            this.readChar();
        }
        String keyword = this.input.substring(pos, this.position);
        backtrack();
        return keyword;
    }

    private Token matchKeyword(String literal){
        switch(literal){
            case "MOV":
                return new Token(Token.TokenType.MOVE, literal);
            case "SET":
                return new Token(Token.TokenType.SET, literal);
            case "WRT":
                return new Token(Token.TokenType.WRITE, literal);
            case "OUT":
                return new Token(Token.TokenType.OUT, literal);
            case "IN":
                return new Token(Token.TokenType.IN, literal);
            case "NOP":
                return new Token(Token.TokenType.NOP, literal);
            case "HLT":
                return new Token(Token.TokenType.END, literal);
            case "INT":
                return new Token(Token.TokenType.INTERRUPT, literal);
            case "SYS":
                return new Token(Token.TokenType.SYSCALL, literal);
            case "WTP":
                return new Token(Token.TokenType.WRITEPOS, literal);
            case "RDP":
                return new Token(Token.TokenType.READPOS, literal);
            case "INCP":
                return new Token(Token.TokenType.INCPOS, literal);
            case "DECP":
                return new Token(Token.TokenType.DECPOS, literal);
            case "WTV":
                return new Token(Token.TokenType.WRITEVAL, literal);
            case "RDV":
                return new Token(Token.TokenType.READVAL, literal);
            case "INCV":
                return new Token(Token.TokenType.INCVAL, literal);
            case "DECV":
                return new Token(Token.TokenType.DECVAL, literal);
            case "PUSH":
                return new Token(Token.TokenType.PUSH, literal);
            case "POP":
                return new Token(Token.TokenType.POP, literal);
            case "CLB":
                return new Token(Token.TokenType.CREATELABEL, literal);
            case "DLB":
                return new Token(Token.TokenType.DELETELABEL, literal);
            case "JLB":
                return new Token(Token.TokenType.JUMPLABEL, literal);
            case "CDP":
                return new Token(Token.TokenType.CREATEDEPENDENCY, literal);
            case "CALL":
                return new Token(Token.TokenType.CALLDEPENDENCY, literal);
            case "LOOP":
                return new Token(Token.TokenType.LOOP, literal);
            case "ebin":
                return new Token(Token.TokenType.EXTENSION, literal);
            case "ebf":
                return new Token(Token.TokenType.EXTENSION, literal);
            case "hds":
                return new Token(Token.TokenType.EXTENSION, literal);
            default:
                return new Token(Token.TokenType.ALIAS, literal);
        }
    }

    private void skipWhitespace(){while(isWhitespace(this.ch)){this.readChar();}}
    private static boolean isWhitespace(char ch){return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';}
    private static boolean isLetter(char ch){return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '/';}
    private static boolean isNumber(char ch){return '0' <= ch && ch <= '9';}
}
