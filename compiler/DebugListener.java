package compiler;

import org.antlr.v4.runtime.ParserRuleContext;

import lexparse.KnightCodeBaseListener;
import lexparse.KnightCodeParser;

public class DebugListener extends KnightCodeBaseListener{

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        System.out.println("Entering rule: " + ctx.getText());
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        System.out.println("Exiting rule: " + ctx.getText());
    }
    
}
