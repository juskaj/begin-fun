package org.beginFun.visitor;

import org.antlr.v4.runtime.tree.RuleNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BeginFunVisitorImpl extends BeginFunBaseVisitor<Object> {

    Variables variables = new Variables();
    Stack<Variables> variablesStack = new Stack<>();
    Map<String, BeginFunParser.FunctionDeclarationContext> functions = new HashMap<>();

    @Override
    public Object visitAssignment(BeginFunParser.AssignmentContext ctx) {

        Object value = visit(ctx.expression());
        if(ctx.CONST_DECLARATION() == null && ctx.getFromArray() == null){
            Object variable = variables.findVariable(ctx.IDENTIFIER().getText());
            if(variable != null){
                return variables.addVariable(ctx.IDENTIFIER().getText(), value);
            }
            return null;
        }

        if(ctx.getFromArray() != null){
            Object array = variables.findVariable(ctx.getFromArray().IDENTIFIER().getText());
            if(array instanceof ArrayList){
                Object val = visit(ctx.getFromArray());
                ((ArrayList<Object>) array).set(((ArrayList<?>) array).indexOf(val), value);
            }

            return value;
        }


        switch (ctx.CONST_DECLARATION().getText()) {
            case "String" -> variables.addVariable(ctx.IDENTIFIER().getText(), value.toString());
            case "Int" -> variables.addVariable(ctx.IDENTIFIER().getText(), Integer.parseInt(value.toString()));
            case "Decimal" -> variables.addVariable(ctx.IDENTIFIER().getText(), Double.parseDouble(value.toString()));
            case "Bool" -> variables.addVariable(ctx.IDENTIFIER().getText(), Boolean.parseBoolean(value.toString()));
        }

        return value;
    }

    @Override
    public Object visitPrintFunctionCall(BeginFunParser.PrintFunctionCallContext ctx) {
        String printText = visit(ctx.expression()).toString();
        System.out.println(printText);
        return null;
    }

    @Override
    public Object visitBooleanBinaryOpExpression(BeginFunParser.BooleanBinaryOpExpressionContext ctx) {
        return checkBooleanBinaryOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.booleanBinaryOp().getText());
    }

    @Override
    public Object visitMatchStatement(BeginFunParser.MatchStatementContext ctx) {
        Object val = visit(ctx.expression());
        for(int i = 0; i < ctx.matchPart().size(); i++){
            if((boolean)visit(ctx.matchPart().get(i))){
                val = ctx.matchPart().get(i).matchNum() != null ? visit(ctx.matchPart().get(i).matchNum().expression(2)) : visit(ctx.matchPart().get(i).matchString().expression());
                break;
            }
        }
        if(!correctType(ctx.CONST_DECLARATION().getText(), val)){
            return null;
        }
        variables.addVariable(ctx.IDENTIFIER().getText(), val);
        return val;
    }

    @Override
    public Object visitMatchString(BeginFunParser.MatchStringContext ctx) {
        String strToCheck = variables.findVariable(ctx.IDENTIFIER().getText()).toString();
        String checkingStr = removeQuotation(ctx.STRING().getText());

        if(strToCheck.equals(checkingStr)){
            return true;
        }
        char a = strToCheck.charAt(0);
        char b = checkingStr.charAt(0);

        int bIndex = 1;
        int aIndex = 1;

        while(aIndex < checkingStr.length()){
            if(a != b){
                switch (b){
                    case '?':
                        b = checkingStr.charAt(aIndex++);
                        break;
                    case '.':
                        if(strToCheck.length() <= aIndex){
                            return false;
                        }
                        a = strToCheck.charAt(bIndex++);
                        b = checkingStr.charAt(aIndex++);
                        break;
                    default:
                        return false;
                }
                continue;
            }

            if(aIndex >= checkingStr.length()){
                break;
            }

            a = strToCheck.charAt(bIndex++);
            b = checkingStr.charAt(aIndex++);
        }
        return true;
    }

    @Override
    public Object visitMatchNum(BeginFunParser.MatchNumContext ctx) {
        return checkBooleanBinaryOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.booleanBinaryOp().getText());
    }

    @Override
    public Object visitConstantExpression(BeginFunParser.ConstantExpressionContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public Object visitIdentifierExpression(BeginFunParser.IdentifierExpressionContext ctx) {
        return variables.findVariable(ctx.IDENTIFIER().getText());
    }

    @Override
    public Object visitConstant(BeginFunParser.ConstantContext ctx) {
        if(ctx.INTEGER() != null){
            return Integer.parseInt(ctx.getText());
        }

        if(ctx.DECIMAL() != null){
            return Double.parseDouble(ctx.getText());
        }

        if(ctx.BOOLEAN() != null){
            return Boolean.parseBoolean(ctx.getText());
        }

        if(ctx.STRING() != null){
            return ctx.getText().substring(1, ctx.getText().length() - 1);
        }

        return null;
    }

    @Override
    public Object visitParenthesesExpression(BeginFunParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitIfStatement(BeginFunParser.IfStatementContext ctx) {
        if((Boolean) visit(ctx.expression())){
            variables.addBlock();
            Object blockReturnValue = visit(ctx.block());
            variables.removeBlock();
            return blockReturnValue;
        }
        return null;
    }

    @Override
    public Object visitIfElseStatement(BeginFunParser.IfElseStatementContext ctx) {
        Object blockReturnValue;
        if((Boolean) visit(ctx.expression())){
            variables.addBlock();
            blockReturnValue = visit(ctx.block(0));
            variables.removeBlock();
        }
        else{
            variables.addBlock();
            blockReturnValue = visit(ctx.block(1));
            variables.removeBlock();
        }
        return blockReturnValue;
    }

    @Override
    public Object visitNumericAddOpExpression(BeginFunParser.NumericAddOpExpressionContext ctx) {
        Object leftVal = visit(ctx.expression(0));
        Object rightVal = visit(ctx.expression(1));

        if((leftVal = checkForIntegerOrDecimal(leftVal.toString())) == null){
            throw new NumberFormatException();
        }
        if((rightVal = checkForIntegerOrDecimal(rightVal.toString())) == null){
            throw new NumberFormatException();
        }

        return switch (ctx.numericAddOp().getText()) {
            case "+" -> (Integer) leftVal + (Integer) rightVal;
            case "-" -> (Integer) leftVal - (Integer) rightVal;
            default -> null;
        };
    }

    @Override
    public Object visitNumericMultiOpExpression(BeginFunParser.NumericMultiOpExpressionContext ctx) {
        Object leftVal = visit(ctx.expression(0));
        Object rightVal = visit(ctx.expression(1));

        if((leftVal = checkForIntegerOrDecimal(leftVal.toString())) == null){
            throw new NumberFormatException();
        }
        if((rightVal = checkForIntegerOrDecimal(rightVal.toString())) == null){
            throw new NumberFormatException();
        }

        return switch (ctx.numericMultiOp().getText()) {
            case "*" -> (Integer) leftVal * (Integer) rightVal;
            case "/" -> (Integer) leftVal / (Integer) rightVal;
            case "%" -> (Integer) leftVal % (Integer) rightVal;
            default -> null;
        };
    }

    @Override
    public Object visitDeconstructingAssignment(BeginFunParser.DeconstructingAssignmentContext ctx) {
        for(int i = 0; i < ctx.IDENTIFIER().size(); i++){
            variables.addVariable(ctx.IDENTIFIER(i).getText(), visit(ctx.expression(i)).toString());
        }
        return null;
    }

    @Override
    public Object visitFunctionDeclaration(BeginFunParser.FunctionDeclarationContext ctx) {
        functions.put(ctx.IDENTIFIER(0).getText(), ctx);
        return null;
    }

    @Override
    public Object visitFunctionCallExpression(BeginFunParser.FunctionCallExpressionContext ctx) {
        return visitFunctionCall(ctx.functionCall());
    }

    @Override
    public Object visitFunctionStatement(BeginFunParser.FunctionStatementContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override
    public Object visitFunctionCall(BeginFunParser.FunctionCallContext ctx) {
        BeginFunParser.FunctionDeclarationContext functionCtx = functions.get(ctx.IDENTIFIER().getText());
        if(functionCtx == null){
            return null;
        }
        if(ctx.expression().size() != functionCtx.IDENTIFIER().size() - 1){
            return null;
        }
        Map<String, Object> functionVariables = new HashMap<>();
        for(int i = 0; i < functionCtx.IDENTIFIER().size() - 1; i++){

            Object argument = visit(ctx.expression(i));
            String parameterType = functionCtx.CONST_DECLARATION(i).getText();
            String parameterName = functionCtx.IDENTIFIER(i + 1).getText();

            if(!correctType(parameterType, argument)){
                return null;
            }

            functionVariables.put(parameterName, argument);
        }
        variablesStack.push(variables);
        variables = new Variables(variables.globalVariables, functionVariables);
        Object blockReturnValue = visit(functionCtx.functionBlock());
        Object returnValue;
        if(blockReturnValue instanceof ReturnValue){
            returnValue = ((ReturnValue) blockReturnValue).getValue();
        }
        else{
            returnValue = functionCtx.expression() != null ? visit(functionCtx.expression()) : null;
        }
        variables = variablesStack.pop();
        return returnValue;
    }

    @Override
    public Object visitWriteToFileStatement(BeginFunParser.WriteToFileStatementContext ctx) {

        String fileName = removeQuotation(ctx.STRING(0).getText());
        Object content = null;

        if(ctx.STRING().size() > 1){
            content = removeQuotation(ctx.STRING(1).getText()).replace("\\n", "\n");
        }
        else{
            content = visit(ctx.expression()).toString().replace("\\n", "\n");;
        }
        try {
            FileWriter writer = new FileWriter(fileName, Boolean.parseBoolean(ctx.BOOLEAN().getText()));
            writer.write(content.toString());
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return super.visitWriteToFileStatement(ctx);
    }

    private String removeQuotation(String string){
        return string.substring(1, string.length() - 1);
    }

    @Override
    public Object visitWhileLoopStatement(BeginFunParser.WhileLoopStatementContext ctx) {

        Object blockReturnValue = null;
        while((boolean) visit(ctx.expression())){
            variables.addBlock();
            blockReturnValue = visit(ctx.block());
            variables.removeBlock();

            if(blockReturnValue instanceof ReturnValue){
                break;
            }
        }

        return blockReturnValue;
    }

    @Override
    public Object visitArrayDeclaration(BeginFunParser.ArrayDeclarationContext ctx) {
        ArrayList<Object> array = new ArrayList<>();
        String type = ctx.CONST_DECLARATION().getText();
        Boolean correctType = false;

        if(ctx.constant().size() > 0){
            if(ctx.constant().size() != Integer.parseInt(ctx.array().INTEGER().getText())){
                return null;
            }
            for(var objToAdd : ctx.constant()){
                if(!correctType(type, visit(objToAdd))){
                    return null;
                }
                array.add(visit(objToAdd));
            }
        }
        variables.addVariable(ctx.IDENTIFIER().getText(), array);
        return array;
    }

    @Override
    public Object visitStringBinaryOpExpression(BeginFunParser.StringBinaryOpExpressionContext ctx) {
        return visit(ctx.expression(0)).toString() + visit(ctx.expression(1)).toString();
    }

    @Override
    public Object visitGetFromArray(BeginFunParser.GetFromArrayContext ctx) {

        Object array = variables.findVariable(ctx.IDENTIFIER().getText());
        if(array instanceof ArrayList){
            return ((ArrayList<Object>)variables.findVariable(ctx.IDENTIFIER().getText())).get(Integer.parseInt(ctx.INTEGER().getText()));
        }
        return null;
    }

    @Override
    public Object visitBooleanUnaryOpExpression(BeginFunParser.BooleanUnaryOpExpressionContext ctx) {
        return !(boolean)visit(ctx.expression());
    }

    @Override
    public Object visitReturnStatement(BeginFunParser.ReturnStatementContext ctx) {
        if(ctx.expression() == null){
            return new ReturnValue(null);
        }
        return new ReturnValue(visit(ctx.expression()));
    }

    private Boolean correctType(String type, Object objectToCheck){
        switch (type) {
            case "String":
                if (!(objectToCheck instanceof String)) {
                    return false;
                }
                break;
            case "Int":
                if (!(objectToCheck instanceof Integer)) {
                    return false;
                }
                break;
            case "Bool":
                if (!(objectToCheck instanceof Boolean)) {
                    return false;
                }
                break;
            case "Decimal":
                if (!(objectToCheck instanceof Double)) {
                    return false;
                }
                break;
        }
        return true;
    }

    private Boolean checkBooleanBinaryOp(Object left, Object right, String op){
        return switch (op) {
            case "||" -> Boolean.parseBoolean(left.toString()) || Boolean.parseBoolean(right.toString());
            case "&&" -> Boolean.parseBoolean(left.toString()) && Boolean.parseBoolean(right.toString());
            case ">" -> Integer.parseInt(left.toString()) > Integer.parseInt(right.toString());
            case "<" -> Integer.parseInt(left.toString()) < Integer.parseInt(right.toString());
            case ">=" -> Integer.parseInt(left.toString()) >= Integer.parseInt(right.toString());
            case "<=" -> Integer.parseInt(left.toString()) <= Integer.parseInt(right.toString());
            case "!=" -> Integer.parseInt(left.toString()) != Integer.parseInt(right.toString());
            case "==" -> Integer.parseInt(left.toString()) == Integer.parseInt(right.toString());
            default -> false;
        };
    }

    private Object checkForIntegerOrDecimal(String value){
        if((tryParseInt(value)) != null){
            return Integer.parseInt(value);
        }

        if(tryParseDecimal(value) != null){
            return Double.parseDouble(value);
        }
        return null;
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double tryParseDecimal(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        return !(currentResult instanceof ReturnValue);
    }
}