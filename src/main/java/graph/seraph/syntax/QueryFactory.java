package graph.seraph.syntax;

import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphOrResult;
import graph.seraph.events.Result;
import graph.seraph.syntax.parser.SeraphLexer;
import graph.seraph.syntax.parser.SeraphParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.streamreasoning.rsp4j.api.querying.syntax.CaseChangingCharStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QueryFactory {

    static ThrowingErrorListener listener = ThrowingErrorListener.INSTANCE;

    public static SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> parse(String queryString, String stream) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(queryString.getBytes());
        return parse(inputStream, stream);
    }

    public static SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> parse(InputStream inputStream, String stream) throws IOException {
        // Ignore case for keywords
        CaseChangingCharStream charStream = new CaseChangingCharStream(CharStreams.fromStream(inputStream), true);
        SeraphLexer lexer = new SeraphLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SeraphParser parser = new SeraphParser(tokens);
        parser.setErrorHandler(new DefaultErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        SeraphVisitorImpl visitor = new SeraphVisitorImpl();

        visitor.visit(parser.oC_Seraph());

        return visitor.getQuery(stream);
    }
}
