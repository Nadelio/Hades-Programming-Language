package src;
import java.io.File;
import java.util.Scanner;

import src.compiler.Compiler;
import src.interpreter.HadesInterpreter;
import src.parser.ASTC;
import src.parser.Lexer;
import src.parser.Parser;
import src.parser.Token;
import src.util.LoadingBar;

public class Main{

    public static boolean DEBUG_FLAG = false;
    public static boolean EPU_FLAG = false;
    public static boolean COMPILE_FLAG = false;
    public static boolean RUN_FLAG = false;
    public static final String COMPILER_VERSION = "v1.1.0";

    public static String loadingPattern = "║" + LoadingBar.RED + "%s" + LoadingBar.CYAN + "%s" + LoadingBar.RESET + "║";
    public static LoadingBar loadingBar;

    public static void main(String[] args) {

        String hadesCode = "";

        // get eBF code from user
        if(args.length == 2 && args[0].startsWith("-")){
            if(args[0].contains("f")){
                try{
                    java.io.File file = new java.io.File(args[1]);

                    if(!file.getName().endsWith(".hds")){
                        System.out.println("\u001B[31mGiven file is not a Hades file.\u001B[0m");
                        System.exit(1);
                    }

                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine()){
                        hadesCode += sc.nextLine() + " ";
                    }
                    sc.close();
                } catch(java.io.FileNotFoundException e){
                    System.out.println("\u001B[31mGiven file not found.\u001B[0m");
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
            } else if(args[0].contains("v")){
                System.out.println("Hades Compiler Version: " + COMPILER_VERSION);
                System.exit(0);
            } else if(args[0].contains("h")){
                System.out.println( "1. Put the `.jar` and the `.bat` file in the same folder as where you want to put your files\r\n" + //
                                    "2. Write your Hades program\r\n" + //
                                    "\\\r\n" + //
                                    "For Linux Systems:\r\n" + //
                                    "3. Run the `hades` file using the `./hades -[flags] [file]` format\r\n" + //
                                    "  - Use `-fc` flag to compile the given file to eBin\r\n" + //
                                    "  - Use `-fr` flag to run the given file\r\n" + //
                                    "  - Use `-fce` flag to compile a ePU formatted Hades file to relevant eBin\r\n" + //
                                    "\\\r\n" + //
                                    "For Windows Systems:\r\n" + //
                                    "3. Run the bat script using the `./hades.bat -[flags] [file]` format\r\n" + //
                                    "  - Use `-fc` flag to compile the given file to eBin\r\n" + //
                                    "  - Use `-fr` flag to run the given file\r\n" + //
                                    "  - Use `-fce` flag to compile a ePU formatted Hades file to relevant eBin\r\n" + //
                                    "4. Profit");
                System.exit(0);
            }else {
                System.out.println("\u001B[31mMissing compile or run flag.\u001B[0m");
                System.exit(1);
            }

        } else if(args.length <= 1){
            System.out.println("\u001B[31mToo few arguments.\u001B[0m");
            System.exit(1);
        } else {
            System.out.println("\u001B[31mToo many arguments.\u001B[0m");
            System.exit(1);
        }
        
        if(COMPILE_FLAG){
            try {
                // lex Hades code
                System.out.println("\u001B[34mLexing Hades code...\u001B[0m");
                Lexer lexer = new Lexer(hadesCode);
                int loadingBarWidth = lexer.calculateLoadTime();
                loadingBar = new LoadingBar(loadingBarWidth - 2, loadingPattern, '=', '>', '═', "╔%s╗\n", "\n╚%s╝");
                Token[] tokens = lexer.lex();
                System.out.print(LoadingBar.CLEAR);
                
                // parse Hades code
                System.out.println("\u001B[34mParsing Hades code...\u001B[0m");
                Parser parser = new Parser(tokens);
                loadingBarWidth = parser.calculateLoadTime();
                loadingBar = new LoadingBar(loadingBarWidth - 2, loadingPattern, '=', '>', '═', "╔%s╗\n", "\n╚%s╝");
                ASTC ast = parser.parse();
                System.out.print(LoadingBar.CLEAR);
    
                // compile Hades code
                System.out.println("\u001B[34mCompiling Hades code...\u001B[0m");
                Compiler compiler = new Compiler(ast);
                loadingBarWidth = compiler.calculateLoadTime();
                loadingBar = new LoadingBar(loadingBarWidth - 2, loadingPattern, '=', '>', '═', "╔%s╗\n", "\n╚%s╝");
                String eBinCode = compiler.compile();
                System.out.print(LoadingBar.CLEAR);
    
                // write eBin code to file
                File inputFile = new File(args[1]);
                File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().length() - 4) + ".ebin");
                System.out.println("\u001B[34mWriting eBin code to \u001B[33m" + outputFile.getName() + "\u001B[34m...\u001B[0m");
                java.io.FileWriter fw = new java.io.FileWriter(outputFile);
                fw.write(eBinCode);
                fw.close();
                System.out.println("\u001B[34mCompleted writing eBin code to \u001B[33m" + outputFile.getName() + "\u001B[0m");
    
            } catch(Exception e) { e.printStackTrace(); }
        } else if(RUN_FLAG){
            // lex Hades code
            System.out.println("\u001B[34mLexing Hades code...\u001B[0m");
            Lexer lexer = new Lexer(hadesCode);
            int loadingBarWidth = lexer.calculateLoadTime();
            loadingBar = new LoadingBar(loadingBarWidth - 2, loadingPattern, '=', '>', '═', "╔%s╗\n", "\n╚%s╝");
            Token[] tokens = lexer.lex();
            System.out.print(LoadingBar.CLEAR);
            
            // parse Hades code
            System.out.println("\u001B[34mParsing Hades code...\u001B[0m");
            Parser parser = new Parser(tokens);
            loadingBarWidth = parser.calculateLoadTime();
            loadingBar = new LoadingBar(loadingBarWidth - 2, loadingPattern, '=', '>', '═', "╔%s╗\n", "\n╚%s╝");
            ASTC ast = parser.parse();
            System.out.print(LoadingBar.CLEAR);

            // interpreter Hades code
            System.out.println("\u001B[34mInterpreting Hades code...\u001B[0m");
            HadesInterpreter interpreter = new HadesInterpreter(ast);
            interpreter.interpret();
        }
    }
}
