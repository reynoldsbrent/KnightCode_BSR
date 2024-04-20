package compiler;

import org.objectweb.asm.Label;
import lexparse.KnightCodeBaseVisitor;
import lexparse.KnightCodeParser;


public class KccVisitor extends KnightCodeBaseVisitor<Void> {
    private final SymbolTable symbolTable;
    private final BytecodeGenerator bytecodeGenerator;

    
    public KccVisitor(SymbolTable symbolTable, BytecodeGenerator bytecodeGenerator) {
        this.symbolTable = symbolTable;
        this.bytecodeGenerator = bytecodeGenerator;
    }

    @Override
    public Void visitDeclare(KnightCodeParser.DeclareContext ctx) {
        
        for (KnightCodeParser.VariableContext variableCtx : ctx.variable()) {
            String varName = variableCtx.identifier().getText();
            String varType = variableCtx.vartype().getText();
            symbolTable.register(varName, varType); 
        }
        return null;
    }

    @Override
public Void visitSetvar(KnightCodeParser.SetvarContext ctx) {
    String varName = ctx.ID().getText();
    
    // Check if we're assigning a string directly
    if (ctx.STRING() != null) {
        String value = ctx.STRING().getText();
        value = value.substring(1, value.length() - 1); 
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName); 
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
    if (expr instanceof KnightCodeParser.NumberContext) {
        int value = Integer.parseInt(expr.getText());
        bytecodeGenerator.pushValue(value);
    } else if (expr instanceof KnightCodeParser.MultiplicationContext) {
        KnightCodeParser.MultiplicationContext multCtx = (KnightCodeParser.MultiplicationContext) expr;
        evaluateExpression(multCtx.expr(0)); 
        evaluateExpression(multCtx.expr(1)); 
        bytecodeGenerator.multiplyIntegers(); 
    } else if (expr instanceof KnightCodeParser.DivisionContext) {
        KnightCodeParser.DivisionContext divCtx = (KnightCodeParser.DivisionContext) expr;
        evaluateExpression(divCtx.expr(0)); 
        evaluateExpression(divCtx.expr(1)); 
        bytecodeGenerator.divideIntegers(); 
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
}

@Override
public Void visitComparison(KnightCodeParser.ComparisonContext ctx) {
    if (ctx instanceof KnightCodeParser.ComparisonContext) {
        KnightCodeParser.ComparisonContext compCtx = (KnightCodeParser.ComparisonContext) ctx;
        evaluateExpression(compCtx.expr(0)); 
        evaluateExpression(compCtx.expr(1)); 

        switch (ctx.comp().start.getType()) {
            case KnightCodeParser.GT:  // Greater than
                bytecodeGenerator.compareGreaterThan();
                break;
            case KnightCodeParser.LT:  // Less than
                bytecodeGenerator.compareLessThan();
                break;
            case KnightCodeParser.EQ:  // Equals
                bytecodeGenerator.compareEquals();
                break;
            case KnightCodeParser.NEQ: // Not equals
                bytecodeGenerator.compareNotEquals();
                break;
        }
    } else {
        throw new RuntimeException("Unsupported comparison: " + ctx.getText());
    }
    return null;
}

@Override
public Void visitDecision(KnightCodeParser.DecisionContext ctx) {
    
    loadValue(ctx.getChild(1).getText());
    loadValue(ctx.getChild(3).getText());

    String relop = ctx.getChild(2).getText();
    Label trueLabel = new Label();
    Label falseLabel = new Label();
    Label endLabel = new Label();

    // Determine the jump based on the relop
    switch (relop) {
        case ">":
            bytecodeGenerator.compareGreaterThan(trueLabel);
            break;
        case "<":
            bytecodeGenerator.compareLessThan(trueLabel);
            break;
        case "=":
            bytecodeGenerator.compareEquals(trueLabel);
            break;
        case "<>":
            bytecodeGenerator.compareNotEquals(trueLabel);
            break;
        default:
            throw new RuntimeException("Unsupported comparator: " + relop);
    }

    bytecodeGenerator.goTo(falseLabel);

    
    bytecodeGenerator.label(trueLabel);
    visit(ctx.getChild(5)); 
    bytecodeGenerator.goTo(endLabel);

    
    bytecodeGenerator.label(falseLabel);
    if (ctx.getChildCount() > 6 && "ELSE".equals(ctx.getChild(6).getText())) {
        visit(ctx.getChild(7));
    }

    bytecodeGenerator.label(endLabel);

    return null;
}

private void loadValue(String term) {
    if (Character.isDigit(term.charAt(0)) || (term.charAt(0) == '-' && term.length() > 1 && Character.isDigit(term.charAt(1)))) {
        bytecodeGenerator.pushValue(Integer.parseInt(term));
    } else {
        int index = symbolTable.getIndex(term);
        bytecodeGenerator.loadVariable(index, symbolTable.getType(term));
    }
}

@Override
public Void visitLoop(KnightCodeParser.LoopContext ctx) {
    Label beginningOfLoop = new Label();
    Label endOfLoop = new Label();

    bytecodeGenerator.label(beginningOfLoop);
    
    String firstNumber = ctx.getChild(1).getText();  
    String secondNumber = ctx.getChild(3).getText();  
    loadValue(firstNumber);    
    loadValue(secondNumber);

    String relop = ctx.getChild(2).getText();
    switch (relop) {
        case ">":
            bytecodeGenerator.compareLessThanOrEqual(endOfLoop);
            break;
        case "<":
            bytecodeGenerator.compareGreaterThanOrEqual(endOfLoop);
            break;
        case "=":
            bytecodeGenerator.compareNotEquals(endOfLoop);
            break;
        case "<>":
            bytecodeGenerator.compareEquals(endOfLoop);
            break;
        default:
            throw new RuntimeException("Unsupported relational operator: " + relop);
    }

    visitChildren(ctx);  

    bytecodeGenerator.goTo(beginningOfLoop);
    bytecodeGenerator.label(endOfLoop);

    return null;
}


@Override
public Void visitPrint(KnightCodeParser.PrintContext ctx) {
    if (ctx.STRING() != null) {
        String literal = ctx.STRING().getText();
        String text = literal.substring(1, literal.length() - 1);
        bytecodeGenerator.printString(text);
    } 
    else if (ctx.ID() != null) {
        String varName = ctx.ID().getText();
        if (symbolTable.isDeclared(varName)) {
            int index = symbolTable.getIndex(varName);
            String varType = symbolTable.getType(varName);
            if ("STRING".equals(varType)) {
                bytecodeGenerator.printStringVariable(index); 
            } else if ("INTEGER".equals(varType)) {
                bytecodeGenerator.printIntegerVariable(index); 
            }
        } else {
            System.err.println("Variable '" + varName + "' is not declared.");
        }
    }
    return null;
}

    @Override
    public Void visitAddition(KnightCodeParser.AdditionContext ctx) {
        ctx.expr().forEach(this::visit);
        bytecodeGenerator.addIntegers();
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

