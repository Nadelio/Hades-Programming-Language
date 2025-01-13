package src.parser;

public class Token {
    public static enum TokenType {
        // v1.1.0
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

        // v1.1.0
        HOLD, // unary instruction
        DROP,
        MOVEHELPLABELPOSITION, // unary instruction
        READTOLABELPOSITION,
        SETHELDLABELVALUE, // unary instruction
        READTOHELDLABELVALUE,
        FUNCTIONMACRO, // binary instruction, takes in instruction list
        OUTNUMBER, 
        OUTVALUE, // unary instruction, takes in int/label
        OUTRANGE, // unary instruction, takes in int/label list
        INVALUE,
        INSTRING,

        // can take in STRING type
        WRITEDATADUMP, // unary instruction, takes in int/label list
        CREATEDATASTRUCTURE, // binary instruction
        FILESTREAMOPEN, // binary instruction
        FILESTREAMCLOSE, // unary instruction
        READFROMFILE, // unary instruction
        WRITETOFILE,  // unary instruction
        
        SETWRITEMODE, // unary instruction, takes in 0 or 1 or label, throw error if(!0 || !1 || !label)

        // non-instructions
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
        COMMENT,

        // v1.1.0
        STRING
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
