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
        int value = Integer.parseInt(expr.getText());
        bytecodeGenerator.pushValue(value);
    } else if (expr instanceof KnightCodeParser.MultiplicationContext) {
        KnightCodeParser.MultiplicationContext multCtx = (KnightCodeParser.MultiplicationContext) expr;
        evaluateExpression(multCtx.expr(0)); // Evaluate left operand
        evaluateExpression(multCtx.expr(1)); // Evaluate right operand
        bytecodeGenerator.multiplyIntegers(); // Perform multiplication
    } else if (expr instanceof KnightCodeParser.DivisionContext) {
        KnightCodeParser.DivisionContext divCtx = (KnightCodeParser.DivisionContext) expr;
        evaluateExpression(divCtx.expr(0)); // Evaluate left operand
        evaluateExpression(divCtx.expr(1)); // Evaluate right operand
        bytecodeGenerator.divideIntegers(); // Perform division
    } else if (expr instanceof KnightCodeParser.AdditionContext) {
        KnightCodeParser.AdditionContext addCtx = (KnightCodeParser.AdditionContext) expr;
        evaluateExpression(addCtx.expr(0));
        evaluateExpression(addCtx.expr(1));
        bytecodeGenerator.addIntegers();
    } else if (expr instanceof KnightCodeParser.SubtractionContext) {
        KnightCodeParser.SubtractionContext subCtx = (KnightCodeParser.SubtractionContext) expr;
        evaluateExpression(subCtx.expr(0));
        evaluateExpression(subCtx.expr(1));
        bytecodeGenerator.subtractIntegers();
    } else if (expr instanceof KnightCodeParser.IdContext) {
        String varName = expr.getText();
        if (!symbolTable.isDeclared(varName)) {
            throw new RuntimeException("Variable '" + varName + "' is not declared.");
        }
        int index = symbolTable.getIndex(varName);
        String varType = symbolTable.getType(varName);
        bytecodeGenerator.loadVariable(index, varType);
    }
    // Extend this method to include other expression types as needed
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
        System.out.println("Adding two integers");
        return null;
    }

    @Override
public Void visitRead(KnightCodeParser.ReadContext ctx) {
    String varName = ctx.ID().getText();
    if (!symbolTable.isDeclared(varName)) {
        throw new RuntimeException("Variable '" + varName + "' is not declared.");
    }
    String varType = symbolTable.getType(varName);
    int index = symbolTable.getIndex(varName);
    
    // Call the appropriate method to read an integer or string
    if ("INTEGER".equals(varType)) {
        bytecodeGenerator.readInteger(index);
    } else if ("STRING".equals(varType)) {
        bytecodeGenerator.readString(index);
    } else {
        throw new RuntimeException("Unsupported type for read operation: " + varType);
    }
    return null;
}



    public BytecodeGenerator getBytecodeGenerator() {
        return bytecodeGenerator;
    }
}

