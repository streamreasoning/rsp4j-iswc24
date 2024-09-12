package graph.neo4j;

import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
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
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.coordinators.ContinuousProgramImpl;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;
import shared.querying.TaskImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/*
 * This class contains a fully functional example which reads data from two different input streams, applies windowing operators
 * over them and joins them using a "union" operator.
 * In particular, the components used are implementations from the Yasper module, and it uses the RDF-graph data model with Jena APIs.
 *
 * The purpose of this example is to give a general sense of how the system works, more specific examples with
 * custom-defined operators can be found in the same directory.
 *
 */

public class Neo4JQuickStart {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Begin");

        /*------------Input and Output Stream definitions------------*/

        // Define a generator to create input graphs
        PGraphStreamGenerator generator = new PGraphStreamGenerator();
        // Define input stream objects from the generator
        DataStream<PGraph> inputStreamColors = generator.getStream("http://test/stream1");
        // define an output stream
        NeoBindingStream outStream = new NeoBindingStream("out");

        /*------------Window Properties------------*/

        // Window properties (report, tick)
        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;

        //Time object used to represent the time in our application
        Time instance = new TimeImpl(0);


        /*------------Window Content------------*/

        //Entity that represents the window content. In particular, we create an instance that represents an empty content
        PGraphOrBindings emptyContent = new PGraphOrBindings();

        /*
        Factory used to create the window content. We provide 4 parameters:
        - Function to transform a type I to a type W
        - Function to transform a type W to a type R
        - Function to merge two types R together
        - Object representing the empty content
        The parameter 'I' is the type of the input data (Graph in this case).
        The parameter 'W' is the type of data that we store inside the window (might differ from 'I'), in this case is still Graph.
        The parameter 'R' is the type of data on which we perform our query operations (select, filter, join etc..). We used a custom data type JenaGraphOrBindings

        The logic behind the content can be customized by defining your own factory and content classes, this particular instance
        of content just accumulates what enters the window.
         */
        ContentFactory<PGraph, PGraph, PGraphOrBindings> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> g,
                PGraphOrBindings::new,
                PGraphOrBindings::new,
                emptyContent
        );


        /*------------S2R, R2R and R2S Operators------------*/

        //Define the Stream to Relation operators (blueprint of the windows), each with its own size and sliding parameters.
        StreamToRelationOperator<PGraph, PGraph, PGraphOrBindings> s2rOp_one =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report_grain,
                        report,
                        1000,
                        1000);

//        StreamToRelationOperator<PGraph, PGraph, PGraphOrBindings> s2rOp_two =
//                new CSPARQLStreamToRelationOpImpl<>(
//                        tick,
//                        instance,
//                        "w2",
//                        accumulatorContentFactory,
//                        report_grain,
//                        report,
//                        500,
//                        500);

        //Define Relation to Relation operators and chain them together. Here we select all the graphs from the input streams and perform a union
        RelationToRelationOperator<PGraphOrBindings> r2rOp1 = new FullQueryUnaryNeo("MATCH (b1:Bike)-[r1:rentedAt]->(s:Station) RETURN r1.user_id\n", Collections.singletonList(s2rOp_one.getName()), "partial_1");
//        RelationToRelationOperator<JenaGraphOrBindings> r2rOp2 = new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g {?s ?p ?o }}", Collections.singletonList(s2rOp_two.getName()), "partial_2");
//        RelationToRelationOperator<JenaGraphOrBindings> r2rBinaryOp = new FullQueryBinaryJena("", List.of("partial_1", "partial_2"), "partial_3");

        //Relation to Stream operator, used to transform the result of a query (type R) to a stream of output objects (type O)
        RelationToStreamOperator<PGraphOrBindings, Map<String, Object>> r2sOp = new RelationToStreamOpNeo();


        /*------------Task definition------------*/

        //Define the Tasks, each of which represent a query
        Task<PGraph, PGraph, PGraphOrBindings, Map<String, Object>> task = new TaskImpl<>();
        task = task
                .addS2ROperator(s2rOp_one, inputStreamColors)
//                .addS2ROperator(s2rOp_two, inputStreamNumbers)
                .addR2ROperator(r2rOp1)
//                .addR2ROperator(r2rOp2)
//                .addR2ROperator(r2rBinaryOp)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSNeo())
                .addTime(instance);
        task.initialize();


        List<DataStream<PGraph>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStreamColors);

        List<DataStream<Map<String, Object>>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        /*------------Continuous Program definition------------*/

        //Define the Continuous Program, which acts as the coordinator of the whole system
        ContinuousProgram<PGraph, PGraph, PGraphOrBindings, Map<String, Object>> cp = new ContinuousProgramImpl<>();
        cp.buildTask(task, inputStreams, outputStreams);


        /*------------Output Stream consumer------------*/

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }
}
