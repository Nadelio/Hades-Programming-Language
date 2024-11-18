package src.interpreter;

import src.data.ASTC;
import src.data.Lexer;
import src.data.Parser;
import src.data.Token;

public class InterMain {
    public static boolean DEBUG_FLAG = false;
    public static boolean EPU_FLAG = false;

    public static void main(String[] args) {
        
        String hadesCode = "";

        // get eBF code from user
        if(args.length == 2 && args[0].startsWith("-")){
            if(args[0].contains("f")){
                try{
                    java.io.File file = new java.io.File(args[1]);

                    if(!file.getName().endsWith(".hds")){
                        System.out.println("Given file is not a Hades file.");
                        System.exit(1);
                    }

                    java.util.Scanner sc = new java.util.Scanner(file);
                    while(sc.hasNextLine()){
                        hadesCode += sc.nextLine() + " ";
                    }
                    sc.close();
                } catch(java.io.FileNotFoundException e) {
                    System.out.println("Given file not found.");
                    System.exit(1);
                }
            }

            if(args[0].contains("d")){
                DEBUG_FLAG = true;
            }

            if(args[0].contains("e")){
                EPU_FLAG = true;
            }

        } else if(args.length <= 1){
            System.out.println("Too few arguments.");
            System.exit(1);
        } else {
            System.out.println("Too many arguments.");
            System.exit(1);
        }
        
        try {
            // lex Hades code
            System.out.println("Lexing Hades code...");
            Lexer lexer = new Lexer(hadesCode);
            Token[] tokens = lexer.lex();
            
            // parse Hades code
            System.out.println("Parsing Hades code...");
            Parser parser = new Parser(tokens);
            ASTC ast = parser.parse();

            // interpret Hades code
            System.out.println("Interpreting Hades code...");
            HadesInterpreter interpreter = new HadesInterpreter(ast);
            interpreter.interpret();

        } catch(Exception e) { e.printStackTrace(); }
    }
}
