package src.data;

public class Result {
    private String result;
    private boolean success;

    public enum Errors {
        INVALID_COMMAND("Invalid command: "),
        NONEXISTENT_LABEL("Non-existent label: "),
        NONEXISTENT_FUNCTION("Non-existent function: "),
        INVALID_VALUE("Invalid Value"),
        INVALID_COMPARISON("Invalid comparison: "),
        INVALID_FILE("Invalid file: "),
        FILE_NOT_FOUND("Cannot find file: "),
        SYNTAX_ERROR("Syntax error: "),
        LOOPED_DEPENDENCY_SET("Looped dependency set: ");

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
