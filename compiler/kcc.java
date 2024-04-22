/**
 * Main class for the KnightCode compiler. This class handles the command-line interface, parses input files,
 * and generates Java bytecode that can be run on the Java Virtual Machine.
 *
 * @author Brent Reynolds
 * @version 1.0
 * @assignment Assignment 5
 * @course CS322 - Compiler Construction
 * @term Spring 2024
 */
package compiler;

import org.antlr.v4.runtime.CharStreams;
import lexparse.KnightCodeLexer;
import lexparse.KnightCodeParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.nio.file.Files;
import java.nio.file.Paths;

public class kcc {

    /**
     * Entry point for the compiler. It processes command line arguments to get input and output file paths,
     * sets up the parsing and compiling environment, and initiates the compilation process.
     *
     * @param args command line arguments expecting two entries: the path to the input .kc file and the path for the output .class file.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java compiler/kcc <path to inputFile.kc> <path to outputFile>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
        
        // Ensure the output file has a .class extension
        if (!outputFile.endsWith(".class")) {
            outputFile += ".class";
        }
        
        try {
            var input = CharStreams.fromFileName(inputFile);

            // Initialize the lexer and parser
            var lexer = new KnightCodeLexer(input);
            var tokens = new CommonTokenStream(lexer);
            var parser = new KnightCodeParser(tokens);

            // Generate the parse tree
            ParseTree tree = parser.file(); 

            // Initialize SymbolTable and BytecodeGenerator
            SymbolTable symbolTable = new SymbolTable();
            BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

            String className = extractClassName(outputFile);
            bytecodeGenerator.startClass(className); // Initialize class generation
            bytecodeGenerator.startMainMethod();

            // Instantiate KccVisitor with SymbolTable and BytecodeGenerator
            KccVisitor visitor = new KccVisitor(symbolTable, bytecodeGenerator);
            
            // Visit the parse tree to generate bytecode
            visitor.visit(tree);
            
            // Finalize the main method and class generation
            bytecodeGenerator.finalizeMainMethod(); 
            byte[] bytecode = bytecodeGenerator.getBytecode(); 
            
            // Write the generated bytecode to the specified output file
            Files.write(Paths.get(outputFile), bytecode);
            System.out.println("Bytecode successfully written to " + outputFile);
            //System.out.println(symbolTable);
            System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("To run the file, use this command: java output/filename");
            System.out.println("----------------------------------------------------------------------------------------------------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing input file: " + inputFile);
        }
        
    }

     /**
     * Extracts the class name from the output file path.
     *
     * @param outputFile the output file path from which to extract the class name
     * @return the extracted class name
     */
    private static String extractClassName(String outputFile) {
        String fileName = new java.io.File(outputFile).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
}

