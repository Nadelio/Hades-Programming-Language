import java.io.File;

public class Main{

    public static boolean DEBUG_FLAG = false;

    public static void main(String[] args) {
        
        String hadesCode = "";

        // get eBF code from user
        if(args.length == 2){
            if(args[0].contains("-f")){
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

            if(args[0].contains("-d")){
                DEBUG_FLAG = true;
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
            Lexer lexer = new Lexer(hadesCode);
            Token[] tokens = lexer.lex();

            // parse Hades code
            Parser parser = new Parser(tokens);
            ASTC ast = parser.parse();

            // compile Hades code
            Compiler compiler = new Compiler(ast);
            String eBinCode = compiler.compile();

            // write eBin code to file
            File inputFile = new File(args[1]);
            File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().length() - 4) + ".ebin");
            System.out.println("Writing eBin code to " + outputFile.getName());
            java.io.FileWriter fw = new java.io.FileWriter(outputFile);
            fw.write(eBinCode);
            fw.close();
            System.out.println("Completed writing eBin code to " + outputFile.getName());

        } catch(Exception e) { e.printStackTrace(); }
    }
}