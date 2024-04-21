/**
 * This class extends the KnightCodeBaseVisitor to implement custom visit methods
 * that generate bytecode for the KnightCode language.
 * The visitor uses a symbol table for variable tracking and a bytecode generator
 * for generating executable bytecode.
 *
 * @author Brent Reynolds
 * @version 1.0
 * @assignment Assignment 5
 * @course CS322 - Compiler Construction
 * @term Spring 2024
 */
package compiler;

import org.objectweb.asm.Label;
import lexparse.KnightCodeBaseVisitor;
import lexparse.KnightCodeParser;


public class KccVisitor extends KnightCodeBaseVisitor<Void> {
    private final SymbolTable symbolTable;
    private final BytecodeGenerator bytecodeGenerator;

    /**
     * Constructs a new KccVisitor with the specified symbol table and bytecode generator.
     *
     * @param symbolTable the symbol table to be used for variable tracking
     * @param bytecodeGenerator the bytecode generator to be used for generating bytecode
     */
    public KccVisitor(SymbolTable symbolTable, BytecodeGenerator bytecodeGenerator) {
        this.symbolTable = symbolTable;
        this.bytecodeGenerator = bytecodeGenerator;
    }

    /**
     * Visits the DeclareContext of the KnightCodeParser. This method registers variables
     * declared in the source code into the symbol table.
     *
     * @param ctx the context of the declare statement in KnightCode
     * @return null after registering all variables
     */
    @Override
    public Void visitDeclare(KnightCodeParser.DeclareContext ctx) {
        
        for (KnightCodeParser.VariableContext variableCtx : ctx.variable()) {
            String varName = variableCtx.identifier().getText();
            String varType = variableCtx.vartype().getText();
            symbolTable.register(varName, varType); // Register variable in symbol table
        }
        return null;
    }

    /**
     * Visits the SetvarContext of the KnightCodeParser. This method assigns values to
     * previously declared variables or prints an error if the variable is not declared.
     *
     * @param ctx the context of the set variable statement in KnightCode
     * @return null after variable assignment or error handling
     */
    @Override
    public Void visitSetvar(KnightCodeParser.SetvarContext ctx) {
        String varName = ctx.ID().getText();
        
        if (ctx.STRING() != null) {
            String value = ctx.STRING().getText();
            value = value.substring(1, value.length() - 1); // Remove quotes from string
            if (symbolTable.isDeclared(varName)) {
                int index = symbolTable.getIndex(varName); 
                bytecodeGenerator.storeString(index, value); // Store string value in variable
            } else {
                System.err.println("Variable " + varName + " not declared.");
            }
        }
        else if (ctx.expr() != null) {
            evaluateExpression(ctx.expr()); // Evaluate the expression
            if (symbolTable.isDeclared(varName)) {
                int index = symbolTable.getIndex(varName);
                String type = symbolTable.getType(varName);
                bytecodeGenerator.storeVariable(index, type); // Store the result in variable
            } else {
                System.err.println("Variable " + varName + " not declared.");
            }
        }
        
        return null; 
    }

    /**
     * Method to evaluate expressions in the context of KnightCode.
     * This method handles different types of expressions like numbers, additions, multiplications,
     * etc., and uses the bytecode generator to push values or perform operations.
     *
     * @param expr the expression context to evaluate
     */
    private void evaluateExpression(KnightCodeParser.ExprContext expr) {
        if (expr instanceof KnightCodeParser.NumberContext) {
            int value = Integer.parseInt(expr.getText());
            bytecodeGenerator.pushValue(value);
        } else if (expr instanceof KnightCodeParser.MultiplicationContext) {
            KnightCodeParser.MultiplicationContext multCtx = (KnightCodeParser.MultiplicationContext) expr;
            evaluateExpression(multCtx.expr(0)); 
            evaluateExpression(multCtx.expr(1)); 
            bytecodeGenerator.multiplyIntegers(); // Perform multiplication
        } else if (expr instanceof KnightCodeParser.DivisionContext) {
            KnightCodeParser.DivisionContext divCtx = (KnightCodeParser.DivisionContext) expr;
            evaluateExpression(divCtx.expr(0)); 
            evaluateExpression(divCtx.expr(1)); 
            bytecodeGenerator.divideIntegers(); // Perform division
        } else if (expr instanceof KnightCodeParser.AdditionContext) {
            KnightCodeParser.AdditionContext addCtx = (KnightCodeParser.AdditionContext) expr;
            evaluateExpression(addCtx.expr(0));
            evaluateExpression(addCtx.expr(1));
            bytecodeGenerator.addIntegers(); // Perform addition
        } else if (expr instanceof KnightCodeParser.SubtractionContext) {
            KnightCodeParser.SubtractionContext subCtx = (KnightCodeParser.SubtractionContext) expr;
            evaluateExpression(subCtx.expr(0));
            evaluateExpression(subCtx.expr(1));
            bytecodeGenerator.subtractIntegers(); // Perform subtraction
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

    /**
     * Visits the ComparisonContext of the KnightCodeParser. This method evaluates comparison expressions 
     * and uses the bytecode generator to perform the appropriate comparison operation.
     *
     * @param ctx the comparison context in KnightCode
     * @return null after performing the comparison
     */
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

    /**
     * Visits the DecisionContext of the KnightCodeParser. Handles conditional statements and generats labels and jumps
     * based on the condition evaluated.
     *
     * @param ctx the decision context in KnightCode
     * @return null after handling the decision structure
     */
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
                throw new RuntimeException("Unsupported relational operator: " + relop);
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

    /**
     * Loads a value into the bytecode generator from a given term. The term can either be a numeric literal
     * or a variable name.
     *
     * @param term the string term to load (either a numeric literal or a variable name)
     */
    private void loadValue(String term) {
         // Check if the term is a numeric literal or a negative number
        if (Character.isDigit(term.charAt(0)) || (term.charAt(0) == '-' && term.length() > 1 && Character.isDigit(term.charAt(1)))) {
            bytecodeGenerator.pushValue(Integer.parseInt(term));
        } else {
            int index = symbolTable.getIndex(term); // Get index of the variable from symbol table
            bytecodeGenerator.loadVariable(index, symbolTable.getType(term)); // Load variable from symbol table
        }
    }

    /**
     * Visits the LoopContext of the KnightCodeParser. This method handles creates labels
     * and jumps to implement looping logic.
     *
     * @param ctx the loop context in KnightCode
     * @return null after setting up the loop
     */
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


    /**
     * Visits the PrintContext of the KnightCodeParser. This method handles print operations for both strings
     * and variables.
     *
     * @param ctx the print context in KnightCode
     * @return null after printing the content
     */
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

    /**
     * Visits the AdditionContext of the KnightCodeParser. It processes addition operations by visiting all subexpressions and adding their results.
     *
     * @param ctx the context of the addition expression in KnightCode
     * @return null after performing the addition
     */
    @Override
    public Void visitAddition(KnightCodeParser.AdditionContext ctx) {
        ctx.expr().forEach(this::visit);
        bytecodeGenerator.addIntegers();
        return null;
    }

    /**
     * Visits the ReadContext of the KnightCodeParser. It reads input into a specified variable, handling both integer and string types.
     *
     * @param ctx the context of the read statement in KnightCode
     * @return null after reading input into the variable
     * @throws RuntimeException if the variable is not declared
     */
    @Override
    public Void visitRead(KnightCodeParser.ReadContext ctx) {
        String varName = ctx.ID().getText();
        if (!symbolTable.isDeclared(varName)) {
            throw new RuntimeException("Variable '" + varName + "' is not declared.");
        }
        String varType = symbolTable.getType(varName);
        int index = symbolTable.getIndex(varName);
        
        // Determine the type of the variable and read it in
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

