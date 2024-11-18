package src.data;
public class Token {
    public static enum TokenType {
        MOVE,
        SET,
        WRITE,
        OUT,
        IN,
        NOP,
        INTERRUPT,
        SYSCALL,
        WRITEPOS,
        READPOS,
        INCPOS,
        DECPOS,
        WRITEVAL,
        READVAL,
        INCVAL,
        DECVAL,
        PUSH,
        POP,
        CREATELABEL,
        DELETELABEL,
        JUMPLABEL,
        CREATEDEPENDENCY,
        CALLDEPENDENCY,
        LOOP,
        ALIAS,
        NUMBER,
        LESS,
        GREATER,
        LESSEQUAL,
        GREATEREQUAL,
        EQUAL,
        NOTEQUAL,
        LBRACKET,
        RBRACKET,
        COMMA,
        FILEINDENTIFIER,
        EXTENSION,
        EOF,
        END,
        INVALID,
        COMMENT
    }

    private TokenType type;
    private String literal;

    public Token(TokenType type, int literal){
        this.type = type;
        this.literal = Integer.toString(literal);
    }

    public Token(TokenType type, String literal){
        this.type = type;
        this.literal = literal;
    }

    public Token(TokenType type, char literal){
        this.type = type;
        this.literal = Character.toString(literal);
    }

    public String getLiteral(){
        return this.literal;
    }

    public TokenType getType(){
        return this.type;
    }

    public String toString(){
        return this.type + " : " + this.literal;
    }
}
