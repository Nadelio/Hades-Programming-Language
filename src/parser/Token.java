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
        HOLD,
        DROP,
        MOVEHELDLABELPOSITION,
        READTOHELDLABELPOSITION,
        SETHELDLABELVALUE,
        READTOHELDLABELVALUE,
        FUNCTIONMACRO, // binary
        OUTNUMBER, 
        OUTVALUE,
        OUTRANGE,
        INVALUE,
        INSTRING,
        SETWRITEMODE,
        // can take in STRING type
        WRITEDATADUMP,
        CREATEDATASTRUCTURE, 
        FILESTREAMOPEN,
        FILESTREAMCLOSE, // takes in string and a label/int
        READFROMFILE, // takes in string and a label/int
        WRITETOFILE,  // takes in string and a label/int
        
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
        EOF,
        END,
        INVALID,
        COMMENT,

        // v1.1.0
        STRING
    }

    /**
     * A list of different semantics for the {@link src.parser.Parser#buildPatternedField(BuilderTypes[], TokenType...)} method
     */
    public enum BuilderTypes {
        /**
         * Match a single token to the expected pattern type
         */
        SINGLE,
        /**
         * Match a single tokens to one of two expected pattern types
         */
        BIVARARG,
        /**
         * Match a single tokens to one of three expected pattern types
         */
        TRIVARARG,
        /**
         * Match a list of tokens to the expected pattern type
         */
        LIST,
        /**
         * Match a list of tokens to one of two expected pattern types
         */
        BIVARARGLIST,
        /**
         * Match a list of tokens to one of three expected pattern types
         */
        TRIVARARGLIST
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
