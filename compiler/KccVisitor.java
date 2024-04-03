package compiler;

import lexparse.KnightCodeBaseVisitor;
import lexparse.KnightCodeParser;

public class KccVisitor extends KnightCodeBaseVisitor<Void> {
    private final SymbolTable symbolTable;
    private final BytecodeGenerator bytecodeGenerator;

    // Constructor injection for dependencies
    public KccVisitor(SymbolTable symbolTable, BytecodeGenerator bytecodeGenerator) {
        this.symbolTable = symbolTable;
        this.bytecodeGenerator = bytecodeGenerator;
    }

    @Override
    public Void visitDeclare(KnightCodeParser.DeclareContext ctx) {
        // Declaration logic, including variable registration and type handling
        for (KnightCodeParser.VariableContext variableCtx : ctx.variable()) {
            String varName = variableCtx.identifier().getText();
            String varType = variableCtx.vartype().getText();
            symbolTable.register(varName, varType); // Assume register also assigns an index
        }
        return null;
    }

    @Override
    public Void visitSetvar(KnightCodeParser.SetvarContext ctx) {
        String varName = ctx.ID().getText();
        int value = Integer.parseInt(ctx.expr().getText()); // Assuming expr results in a directly usable integer
        if (symbolTable.isDeclared(varName)) {
            bytecodeGenerator.storeVariable(symbolTable.getIndex(varName), value);
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
        return null; // Continue tree traversal
    }

    @Override
    public Void visitPrint(KnightCodeParser.PrintContext ctx) {
        if (ctx.ID() != null) {
            String varName = ctx.ID().getText();
            int index = symbolTable.getIndex(varName); // Retrieve variable index
            bytecodeGenerator.loadVariable(index); // Load variable onto stack
            bytecodeGenerator.printInteger(); // Assume the variable is an integer
        } else if (ctx.STRING() != null) {
            bytecodeGenerator.printString(ctx.STRING().getText()); // Directly print string literals
        }
        return null;
    }

    @Override
    public Void visitAddition(KnightCodeParser.AdditionContext ctx) {
        // Visit children to push their results onto the stack
        ctx.expr().forEach(this::visit);
        bytecodeGenerator.addIntegers(); // Perform addition
        return null;
    }

    // Implement additional visit methods as needed

    public BytecodeGenerator getBytecodeGenerator() {
        return bytecodeGenerator;
    }
}

