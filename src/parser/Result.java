package src.parser;

public class Result {
    private String result;
    private boolean success;

    public enum Errors {
        INVALID_COMMAND("\u001B[31mInvalid command: \u001B[33m"),
        NONEXISTENT_LABEL("\u001B[31mNon-existent label: \u001B[33m"),
        NONEXISTENT_FUNCTION("\u001B[31mNon-existent function: \u001B[33m"),
        INVALID_VALUE("\u001B[31mInvalid Value: \u001B[33m"),
        INVALID_COMPARISON("\u001B[31mInvalid comparison: \u001B[33m"),
        INVALID_FILE("\u001B[31mInvalid file: \u001B[33m"),
        FILE_NOT_FOUND("\u001B[31mCannot find file: \u001B[33m"),
        SYNTAX_ERROR("\u001B[31mSyntax error: \u001B[33m"),
        LOOPED_DEPENDENCY_SET("\u001B[31mLooped dependency set: \u001B[33m");

        private String errmsg;
        private Errors(String errmsg) { this.errmsg = errmsg; }
        public String getError() { return this.errmsg; }
    }

    private Result(String result, boolean success){
        this.result = result;
        this.success = success;
    }

    public static Result Error(Result.Errors err, String context){return new Result(err.getError() + context, false);}
    public static Result Success(){return new Result("Command Succeeded", true);}
    
    public void handleError(){
        System.out.println(this.result);
        System.exit(1);
    }
    
    public String getResult(){ return this.result; }
    public boolean getSuccess(){ return this.success; }
}
