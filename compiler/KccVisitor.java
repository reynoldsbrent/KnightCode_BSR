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
        // This call now prepares bytecode for evaluating the expression and leaves the result on the stack.
        evaluateExpression(ctx.expr());
        
        // Now, we expect the result of the expression to be on top of the stack.
        // Next, we need to store this result in the variable `varName`.
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName);
            bytecodeGenerator.storeVariable(index); // Adjusted to assume result is already on the stack.
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
        return null;
    }

private void evaluateExpression(KnightCodeParser.ExprContext expr) {
    if (expr instanceof KnightCodeParser.NumberContext) {
        // Push number constant onto the stack.
        int value = Integer.parseInt(expr.getText());
        bytecodeGenerator.pushValue(value);
    } else if (expr instanceof KnightCodeParser.AdditionContext) {
        KnightCodeParser.AdditionContext additionCtx = (KnightCodeParser.AdditionContext) expr;
        // Evaluate left and right expressions to push their results onto the stack
        evaluateExpression(additionCtx.expr(0));
        evaluateExpression(additionCtx.expr(1));
        // Perform addition on the top two stack values
        bytecodeGenerator.addIntegers();
    } // Implement subtraction, multiplication, and division similarly.
    else if (expr instanceof KnightCodeParser.IdContext) {
        String varName = expr.getText();
        if (symbolTable.isDeclared(varName)) {
            // Load the variable's value onto the stack.
            int index = symbolTable.getIndex(varName);
            bytecodeGenerator.loadVariable(index);
        } else {
            throw new RuntimeException("Variable '" + varName + "' is not declared.");
        }
    }
    // Extend with more else-if blocks for other expression types as necessary.
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

