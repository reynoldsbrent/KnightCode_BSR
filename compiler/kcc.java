package compiler;


import org.antlr.v4.runtime.CharStreams;
import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;
import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class kcc {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Make sure to enter command line arguments like this: java compiler/kcc inputFile outputFile");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
        
        try {
            // Setup ANTLR input stream from the provided file
            var input = CharStreams.fromFileName(inputFile);

            // Initialize the lexer
            var lexer = new KnightCodeLexer(input);
            var tokens = new CommonTokenStream(lexer);

            // Initialize the parser
            var parser = new KnightCodeParser(tokens);

            // Parse the input file to generate the parse tree
            ParseTree tree = parser.file(); // file is root in grammar

            // Initialize your custom visitor here and visit the parse tree
            KccVisitor visitor = new KccVisitor();
            visitor.visit(tree);

            // Placeholder for bytecode generation logic
            System.out.println("Parsing and visitor traversal complete. Output file generation not yet implemented.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing input file: " + inputFile);
        }
    }
}
