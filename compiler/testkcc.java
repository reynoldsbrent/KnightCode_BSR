package compiler;

import org.antlr.v4.runtime.CharStreams;
import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;
import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class testkcc {
    public static void main(String[] args) {
        // Check command-line arguments
        if (args.length != 2) {
            System.err.println("Usage: java compiler.kcc <inputFile> <outputFile>");
            System.exit(1);
        }
        String inputFile = args[0];
        // String outputFile = args[1];

        try {
            // Set up ANTLR to parse the input file
            KnightCodeLexer lexer = new KnightCodeLexer(CharStreams.fromFileName(inputFile));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            KnightCodeParser parser = new KnightCodeParser(tokens);
            ParseTree tree = parser.file(); 

            // Walk the parse tree with the debug listener
            ParseTreeWalker walker = new ParseTreeWalker();
            DebugListener listener = new DebugListener();
            walker.walk(listener, tree);

            System.out.println("Parse tree walking complete with DebugListener.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing the input file: " + inputFile);
        }
    }
}

