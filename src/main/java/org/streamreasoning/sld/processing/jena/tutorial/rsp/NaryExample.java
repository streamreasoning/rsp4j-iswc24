package org.streamreasoning.sld.processing.jena.tutorial.rsp;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.contentimpl.factories.AccumulatorContentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.r2s.RStream;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.HoppingWindowOpImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;
import org.streamreasoning.sld.processing.jena.sds.SDSJena;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.processing.jena.stream.JenaStreamGenerator;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.FullQueryNaryJena;

import java.util.ArrayList;
import java.util.List;

public class NaryExample {

    public static void main(String[] args) throws InterruptedException {

        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();

        JenaStreamGenerator generator = new JenaStreamGenerator();
        // define output stream
        DataStream<Graph> inputStream = generator.getStream("http://test/stream1");

        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);

        JenaGraphOrBindings emptyContent = new JenaGraphOrBindings(GraphFactory.createGraphMem());

        AccumulatorContentFactory<Graph, Graph, JenaGraphOrBindings> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> g,
                (Graph content) -> {return new JenaGraphOrBindings(content);},
                (r1, r2) -> new JenaGraphOrBindings(r1, r2),
                emptyContent
        );


        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp1 =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp2 =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp3 =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "http://windows.org/w3",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        RelationToRelationOperator<JenaGraphOrBindings> r2rOp1 = new FullQueryNaryJena("PREFIX : <http://windows.org/> " +
                                                                                       "SELECT * WHERE { GRAPH :w3 {?s ?p ?o } }", List.of(s2rOp1.getName(), s2rOp2.getName(), s2rOp3.getName()), "partial_1");
//        RelationToRelationOperator<JenaGraphOrBindings> r2rOp3 = new FullQueryBinaryJena("", List.of("partial_1", "partial_2"), "partial_3");

        RelationToStreamOperator<JenaGraphOrBindings, Binding> r2sOp = new RStream<>();

        Task<Graph, Graph, JenaGraphOrBindings, Binding> task = new TaskImpl<>();
        task = task
                .addS2ROperator(s2rOp1, inputStream)
                .addS2ROperator(s2rOp2, inputStream)
                .addS2ROperator(s2rOp3, inputStream)
                .addR2ROperator(r2rOp1)
//                .addR2ROperator(r2rOp3)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSJena())
                .addTime(instance);
        task.initialize();

        List<DataStream<Graph>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream);


        List<DataStream<Binding>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }
}
