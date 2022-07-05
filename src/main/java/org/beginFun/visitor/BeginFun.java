package org.beginFun.visitor;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class BeginFun {
    public static void main(String[] args) {
        String[] values = args[0].split("\\.");

        if(values.length != 2 || !values[1].equals("beginFun")){
            System.out.println("Wrong file...\nShould be .beginFUn");
            return;
        }

        try {
            execute(CharStreams.fromFileName(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object execute(CharStream stream) {
        BeginFunLexer lexer = new BeginFunLexer(stream);
        BeginFunParser parser = new BeginFunParser(new CommonTokenStream(lexer));
        parser.setBuildParseTree(true);
        ParseTree tree = parser.program();

        BeginFunVisitorImpl visitor = new BeginFunVisitorImpl();
        return visitor.visit(tree);
    }
}
