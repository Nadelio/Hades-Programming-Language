package src.parser;

import src.util.Constants;

public class Result {
    private String result;
    private boolean success;

    public enum Errors {
        INVALID_COMMAND(Constants.ANSI_ERROR + "Invalid command:" + Constants.ANSI_RESET),
        NONEXISTENT_LABEL(Constants.ANSI_ERROR + "Non-existent label:" + Constants.ANSI_RESET),
        NONEXISTENT_FUNCTION(Constants.ANSI_ERROR + "Non-existent function:" + Constants.ANSI_RESET),
        INVALID_VALUE(Constants.ANSI_ERROR + "Invalid Value:" + Constants.ANSI_RESET),
        INVALID_COMPARISON(Constants.ANSI_ERROR + "Invalid comparison:" + Constants.ANSI_RESET),
        INVALID_FILE(Constants.ANSI_ERROR + "Invalid file:" + Constants.ANSI_RESET),
        FILE_NOT_FOUND(Constants.ANSI_ERROR + "Cannot find file:" + Constants.ANSI_RESET),
        INVALID_FILE_STREAM_MODE(Constants.ANSI_ERROR + "Invalid file stream mode:" + Constants.ANSI_RESET),
        SYNTAX_ERROR(Constants.ANSI_ERROR + "Syntax error:" + Constants.ANSI_RESET),
        LOOPED_DEPENDENCY_SET(Constants.ANSI_ERROR + "Looped dependency set:" + Constants.ANSI_RESET),
        NO_HELD_LABEL(Constants.ANSI_ERROR + "No held label:" + Constants.ANSI_RESET);

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
