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
    System.out.println("Setting a variable...");
    String varName = ctx.ID().getText();
    
    // Check if we're assigning a string directly
    if (ctx.STRING() != null) {
        String value = ctx.STRING().getText();
        value = value.substring(1, value.length() - 1); // Remove quotes
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName); // Convert varName to its index
            bytecodeGenerator.storeString(index, value); 
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
    }
    // Handle expressions
    else if (ctx.expr() != null) {
        evaluateExpression(ctx.expr());
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName);
            String type = symbolTable.getType(varName);
            bytecodeGenerator.storeVariable(index, type); 
        } else {
            System.err.println("Variable " + varName + " not declared.");
        }
    }
    
    return null; 
}



private void evaluateExpression(KnightCodeParser.ExprContext expr) {
    System.out.println("Evaluating expression...");
    if (expr instanceof KnightCodeParser.NumberContext) {
        // Direct number constant
        int value = Integer.parseInt(expr.getText());
        bytecodeGenerator.pushValue(value);
    } else if (expr instanceof KnightCodeParser.AdditionContext) {
        // Addition: Recursive evaluation for both sides of the addition
        KnightCodeParser.AdditionContext additionCtx = (KnightCodeParser.AdditionContext) expr;
        evaluateExpression(additionCtx.expr(0));
        evaluateExpression(additionCtx.expr(1));
        bytecodeGenerator.addIntegers();
    }
      else if (expr instanceof KnightCodeParser.SubtractionContext) {
        KnightCodeParser.SubtractionContext subtractionCtx = (KnightCodeParser.SubtractionContext) expr;
        evaluateExpression(subtractionCtx.expr(0)); // Evaluate the left side
        evaluateExpression(subtractionCtx.expr(1)); // Evaluate the right side
        bytecodeGenerator.subtractIntegers(); // Perform the subtraction
    } else if (expr instanceof KnightCodeParser.IdContext) {
        String varName = expr.getText();
        if (!symbolTable.isDeclared(varName)) {
            throw new RuntimeException("Variable '" + varName + "' is not declared.");
        }
        int index = symbolTable.getIndex(varName);
        String varType = symbolTable.getType(varName);
        // Depending on the variable type, load it appropriately
        if ("INTEGER".equals(varType)) {
            bytecodeGenerator.loadVariable(index, varType);
        } else if ("STRING".equals(varType)) {
            bytecodeGenerator.loadVariable(index, varType);
        }
    }
    // Handle other expression types (e.g., Subtraction, Multiplication, Division) similarly
}



@Override
public Void visitPrint(KnightCodeParser.PrintContext ctx) {
    System.out.println("I am printing something");
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
                bytecodeGenerator.printIntegerVariable(index); // For integer variables
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


    public BytecodeGenerator getBytecodeGenerator() {
        return bytecodeGenerator;
    }
}

