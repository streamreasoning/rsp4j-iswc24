package graph.jena.syntax;

import graph.ContinuousQuery;
import graph.jena.datatypes.JenaGraphOrBindings;
import graph.seraph.syntax.CaseChangingCharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RSPQLQueryFactory {

    public static ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse(String query) {
        try {

            InputStream inputStream = new ByteArrayInputStream(query.getBytes());
            // Ignore case for keywords
            CaseChangingCharStream charStream = new CaseChangingCharStream(CharStreams.fromStream(inputStream), true);
            RSPQLLexer lexer = new RSPQLLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            RSPQLParser parser = new RSPQLParser(tokens);
            parser.setErrorHandler(new DefaultErrorStrategy());
            ParseTree tree = parser.queryUnit();
            RSPQLVisitorImpl visitor = new RSPQLVisitorImpl();
            visitor.visit(tree);
            return visitor.getQuery();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }


}
