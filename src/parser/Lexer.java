package src.parser;

import src.Main;
import src.util.Constants;

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
            if(Main.DEBUG_FLAG){ System.out.println(Constants.ANSI_MSG + "Reading Character: " + Constants.ANSI_INFO + this.input.charAt(this.readPosition) + Constants.ANSI_RESET); }
            this.ch = this.input.charAt(this.readPosition);
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    private char peekChar() {
        if (this.readPosition >= this.input.length()) {
            return '\u0000';
        } else {
            if(Main.DEBUG_FLAG){ System.out.println(Constants.ANSI_MSG + "Peeking Character: " + Constants.ANSI_INFO + this.input.charAt(this.readPosition) + Constants.ANSI_RESET); }
            return this.input.charAt(this.readPosition);
        }
    }

    public Token[] lex(){
        java.util.ArrayList<Token> tokens = new java.util.ArrayList<Token>();
        while(this.ch != '\u0000'){
            Token tok = this.nextToken();
            if(Main.DEBUG_FLAG){System.out.println(Constants.ANSI_MSG + "Completed Command: " + Constants.ANSI_INFO + tok.toString() + Constants.ANSI_RESET);}
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
            case '"':
                int pos = this.position;
                while(this.ch != '"'){
                    this.readChar();
                }
                String lit = this.input.substring(pos + 1, this.position);
                tok = new Token(Token.TokenType.STRING, lit);
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
            case "HOLD":
                return new Token(Token.TokenType.HOLD, literal);
            case "DROP":
                return new Token(Token.TokenType.DROP, literal);
            case "MLB":
                return new Token(Token.TokenType.MOVEHELDLABELPOSITION, literal);
            case "MLP":
                return new Token(Token.TokenType.READTOHELDLABELPOSITION, literal);
            case "SLB":
                return new Token(Token.TokenType.SETHELDLABELVALUE, literal);
            case "SLV":
                return new Token(Token.TokenType.READTOHELDLABELVALUE, literal);
            case "WDD":
                return new Token(Token.TokenType.WRITEDATADUMP, literal);
            case "DS":
                return new Token(Token.TokenType.CREATEDATASTRUCTURE, literal);
            case "FUNC":
                return new Token(Token.TokenType.FUNCTIONMACRO, literal);
            case "OUTN":
                return new Token(Token.TokenType.OUTNUMBER, literal);
            case "OUTV":
                return new Token(Token.TokenType.OUTVALUE, literal);
            case "OUTR":
                return new Token(Token.TokenType.OUTRANGE, literal);
            case "INV":
                return new Token(Token.TokenType.INVALUE, literal);
            case "INS":
                return new Token(Token.TokenType.INSTRING, literal);
            case "FSO":
                return new Token(Token.TokenType.FILESTREAMOPEN, literal);
            case "FSC":
                return new Token(Token.TokenType.FILESTREAMCLOSE, literal);
            case "RFF":
                return new Token(Token.TokenType.READFROMFILE, literal);
            case "WTF":
                return new Token(Token.TokenType.WRITETOFILE, literal);
            case "SWM":
                return new Token(Token.TokenType.SETWRITEMODE, literal);
            default:
                return new Token(Token.TokenType.ALIAS, literal);
        }
    }

    private void skipWhitespace(){while(isWhitespace(this.ch)){this.readChar();}}
    private static boolean isWhitespace(char ch){return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';}
    private static boolean isLetter(char ch){return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';}
    private static boolean isNumber(char ch){return '0' <= ch && ch <= '9';}
}
