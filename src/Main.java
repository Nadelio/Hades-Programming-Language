package src;
import java.io.File;

import src.compiler.Compiler;
import src.interpreter.HadesInterpreter;
import src.parser.ASTC;
import src.parser.Lexer;
import src.parser.Parser;
import src.parser.Token;

public class Main{

    public static boolean DEBUG_FLAG = false;
    public static boolean EPU_FLAG = false;
    public static boolean COMPILE_FLAG = false;
    public static boolean RUN_FLAG = false;

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

            if(args[0].contains("c")){
                COMPILE_FLAG = true;
                if(args[0].contains("e")){
                    EPU_FLAG = true;
                }
            } else if(args[0].contains("r")){
                RUN_FLAG = true;
            } else {
                System.out.println("Missing compile or run flag.");
                System.exit(1);
            }

        } else if(args.length <= 1){
            System.out.println("Too few arguments.");
            System.exit(1);
        } else {
            System.out.println("Too many arguments.");
            System.exit(1);
        }
        
        if(COMPILE_FLAG){
            try {
                // lex Hades code
                System.out.println("Lexing Hades code...");
                Lexer lexer = new Lexer(hadesCode);
                Token[] tokens = lexer.lex();
                
                // parse Hades code
                System.out.println("Parsing Hades code...");
                Parser parser = new Parser(tokens);
                ASTC ast = parser.parse();
    
                // compile Hades code
                System.out.println("Compiling Hades code...");
                Compiler compiler = new Compiler(ast);
                String eBinCode = compiler.compile();
    
                // write eBin code to file
                File inputFile = new File(args[1]);
                File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().length() - 4) + ".ebin");
                System.out.println("Writing eBin code to " + outputFile.getName() + "...");
                java.io.FileWriter fw = new java.io.FileWriter(outputFile);
                fw.write(eBinCode);
                fw.close();
                System.out.println("Completed writing eBin code to " + outputFile.getName());
    
            } catch(Exception e) { e.printStackTrace(); }
        } else if(RUN_FLAG){
            // lex Hades code
            System.out.println("Lexing Hades code...");
            Lexer lexer = new Lexer(hadesCode);
            Token[] tokens = lexer.lex();

            // parse Hades code
            System.out.println("Parsing Hades code...");
            Parser parser = new Parser(tokens);
            ASTC ast = parser.parse();

            // interpreter Hades code
            System.out.println("Interpreting Hades code...");
            HadesInterpreter interpreter = new HadesInterpreter(ast);
            interpreter.interpret();
        }
    }
}
