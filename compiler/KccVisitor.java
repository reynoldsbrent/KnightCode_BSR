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
    
    // Check if we're assigning a string directly
    if (ctx.STRING() != null) {
        String value = ctx.STRING().getText();
        value = value.substring(1, value.length() - 1); // Remove quotes
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName); // Convert varName to its index
            bytecodeGenerator.storeString(index, value); // Corrected to pass index and value
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
    }
    // Handle expressions
    else if (ctx.expr() != null) {
        evaluateExpression(ctx.expr());
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName);
            bytecodeGenerator.storeVariable(index); // Assume this method exists for storing the result of expressions
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
    }
    
    return null; // Continue tree traversal
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
    // Handle string literals directly
    if (ctx.STRING() != null) {
        String literal = ctx.STRING().getText();
        // Remove the surrounding quotes from the literal
        String text = literal.substring(1, literal.length() - 1);
        bytecodeGenerator.printString(text);
    } 
    // Handle printing of variables
    else if (ctx.ID() != null) {
        String varName = ctx.ID().getText();
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName);
            String varType = symbolTable.getType(varName);
            if ("STRING".equals(varType)) {
                bytecodeGenerator.printStringVariable(index); // For string variables
            } else if ("INTEGER".equals(varType)) {
                bytecodeGenerator.printIntegerVariable(index); // Assuming a method for integers
            }
        } else {
            System.err.println("Variable '" + varName + "' is not declared.");
        }
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

