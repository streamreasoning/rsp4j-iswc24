package graph.seraph.syntax;

import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphOrResult;
import graph.seraph.events.Result;
import graph.seraph.op.FullQueryUnaryNeo;
import graph.seraph.op.RelationToStreamOpNeo;
import graph.seraph.streams.PGStream;
import graph.seraph.streams.ResultStream;
import graph.seraph.streams.SDSNeo;
import graph.seraph.syntax.parser.SeraphBaseVisitor;
import graph.seraph.syntax.parser.SeraphParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;
import shared.querying.TaskImpl;

import java.time.Duration;
import java.util.*;

public class SeraphVisitorImpl extends SeraphBaseVisitor<SeraphQuery> {


    private Map<String, StreamToRelationOperator<PGraph, PGraph, PGraphOrResult>> inputs = new HashMap<>();
    private Map<String, R2S> outputs = new HashMap<>();
    private Map<String, String> inputParameters = new HashMap<>();
    private Map<String, Object> outputParameters = new HashMap<>();
    private List<String> projections = new ArrayList<>();
    private Map<String, List<StringBuilder>> relationParameters = new HashMap<>();
    private final ContentFactory<PGraph, PGraph, PGraphOrResult> accumulatorContentFactory;


    private final Report report = new ReportImpl();
    private final Tick tick = Tick.TIME_DRIVEN;
    private final ReportGrain report_grain = ReportGrain.SINGLE;
    private final Time instance = new TimeImpl(0);


    public SeraphVisitorImpl() {

        relationParameters.put("range", new ArrayList<>());
        relationParameters.put("r2r", new ArrayList<>(Collections.singletonList(new StringBuilder())));
        this.accumulatorContentFactory = new AccumulatorContentFactory<>((g) -> g, PGraphOrResult::new, PGraphOrResult::new, new PGraphOrResult());

        report.add(new OnWindowClose());

    }


    @Override
    public SeraphQuery visitOC_Seraph(SeraphParser.OC_SeraphContext ctx) {

        // *S2R Part
        inputParameters.put("identifier", ctx.id.getText());

        String defaultInputStream = null;

        inputParameters.put("input", defaultInputStream);
        inputParameters.put("starting", ctx.oS_T0().getText());


        // *R2S Part
        Duration period = Duration.parse(ctx.range.getText().trim());
        String streamOperator = ctx.stream_op.getText();

        String defaultOutputStream = null;

        outputParameters.put("output", defaultOutputStream);
        outputParameters.put("period", period.toString());
        outputParameters.put("streamOperator", streamOperator);
        outputParameters.put("projections", projections);

        return super.visitOC_Seraph(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionBody(SeraphParser.OC_ProjectionBodyContext ctx) {

        return super.visitOC_ProjectionBody(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionItem(SeraphParser.OC_ProjectionItemContext ctx) {
        String text = "";
        if (ctx.children.size() > 1) {
            text = ctx.children.get(ctx.children.size() - 1).getText();
        } else text = ctx.children.get(0).getText();
        projections.add(text);
        return super.visitOC_ProjectionItem(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionItems(SeraphParser.OC_ProjectionItemsContext ctx) {
        return super.visitOC_ProjectionItems(ctx);
    }

    @Override
    public SeraphQuery visitOC_Return(SeraphParser.OC_ReturnContext ctx) {

        outputParameters.put("returnStatement", ctx.children.get(0).getText());
        String returnStatement = ctx.children.get(1).getText();


        for (StringBuilder r2r : relationParameters.get("r2r")) {
            r2r.append(" RETURN").append(returnStatement);
        }

        relationParameters.get("r2r").remove(relationParameters.get("r2r").size() - 1);

        return super.visitOC_Return(ctx);
    }


    @Override
    public SeraphQuery visitOC_Match(SeraphParser.OC_MatchContext ctx) {

        StringBuilder cypherMatch = new StringBuilder();

        for (ParseTree subTree : ctx.children) {
            if (subTree.getClass() == SeraphParser.OC_WithinContext.class) {
                String range = ((SeraphParser.OC_WithinContext) subTree).ISO8601_DURATION().getText().trim();

                relationParameters.get("range").add(new StringBuilder(range));
            } else {
                cypherMatch.append(subTree.getText());

            }
        }


        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(cypherMatch);
        relationParameters.get("r2r").add(new StringBuilder());

        return super.visitOC_Match(ctx);
    }


    @Override
    public SeraphQuery visitOC_With(SeraphParser.OC_WithContext ctx) {
        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(ctx.getText());
        return super.visitOC_With(ctx);
    }

    public SeraphQuery visitOC_Unwind(SeraphParser.OC_UnwindContext ctx) {
        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(ctx.getText());
        return super.visitOC_Unwind(ctx);
    }

    //returns the parsed seraph query
    public SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> getQuery(String stream) {

        System.out.println("-----------------PARSING-----------------");

        System.out.println("input:    " + inputParameters);
        System.out.println("relation: " + relationParameters);
        System.out.println("output:   " + outputParameters);

        String query = relationParameters.get("r2r").get(0).toString();

        String startingTime = inputParameters.get("starting");
        Duration range = Duration.parse(relationParameters.get("range").get(0));
        Duration period = Duration.parse((CharSequence) outputParameters.get("period"));

        CSPARQLStreamToRelationOpImpl<PGraph, PGraph, PGraphOrResult> w1 = new CSPARQLStreamToRelationOpImpl<>(tick, instance, "w1", accumulatorContentFactory, report_grain, report, range.toMillis(), period.toMillis());


        Task<PGraph, PGraph, PGraphOrResult, Result> task = new TaskImpl<>();

        RelationToRelationOperator<PGraphOrResult> r2rOp1 = new FullQueryUnaryNeo(query, Collections.singletonList(w1.getName()), "partial_1");

        RelationToStreamOperator<PGraphOrResult, Result> r2sOp = new RelationToStreamOpNeo();

        PGStream dataStream = new PGStream(stream);

        task = task.addS2ROperator(w1, dataStream).addR2ROperator(r2rOp1).addR2SOperator(r2sOp).addDAG(new DAGImpl<>()).addSDS(new SDSNeo()).addTime(instance);

        task.initialize();

        return new SeraphQuery<>(inputParameters.get("identifier"), task, projections, new ResultStream(inputParameters.get("identifier")), dataStream);
    }

}
