package compiler;

import lexparse.KnightCodeBaseVisitor;
import lexparse.KnightCodeParser;

public class KccVisitor extends KnightCodeBaseVisitor<Void> {
    private SymbolTable symbolTable = new SymbolTable();
    private BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

    @Override
    public Void visitDeclare(KnightCodeParser.DeclareContext ctx) {
        for (KnightCodeParser.VariableContext variableCtx : ctx.variable()) {
            String varName = variableCtx.identifier().getText();
            String varType = variableCtx.vartype().getText();
            symbolTable.register(varName, varType);
            // Example: Assuming all variables are integers for simplicity
            bytecodeGenerator.addIntegerVariable(varName, 0); // Initializing with 0
        }
        return null;
    }


    @Override
    public Void visitPrint(KnightCodeParser.PrintContext ctx) {
        // Handle print statements, including bytecode generation
        return null;
    }

    public BytecodeGenerator getBytecodeGenerator() {
        return this.bytecodeGenerator;
    }

    // Implement additional visit methods for arithmetic, control structures, etc.
}

