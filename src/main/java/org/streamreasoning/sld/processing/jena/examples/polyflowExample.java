package org.streamreasoning.sld.processing.jena.examples;

import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.jena.FullQueryUnaryJena;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import org.streamreasoning.sld.processing.jena.sds.SDSJena;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.processing.jena.stream.JenaStreamGenerator;
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
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.HoppingWindowOpImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class polyflowExample {

    public static void main(String[] args) throws InterruptedException {

        JenaStreamGenerator generator = new JenaStreamGenerator();

        DataStream<Graph> inputStream = generator.getStream("http://test/stream1");
        // define output stream
        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);

        JenaGraphOrBindings emptyContent = new JenaGraphOrBindings(GraphFactory.createGraphMem());

        AccumulatorContentFactory<Graph, Graph, JenaGraphOrBindings> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> g,
                JenaGraphOrBindings::new,
                JenaGraphOrBindings::new,
                emptyContent
        );


        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();

        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        RelationToRelationOperator<JenaGraphOrBindings> r2rOp1 = new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), "partial_1");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp2 = new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), "partial_2");
//        RelationToRelationOperator<JenaGraphOrBindings> r2rOp3 = new FullQueryBinaryJena("", List.of("partial_1", "partial_2"), "partial_3");

        RelationToStreamOperator<JenaGraphOrBindings, Binding> r2sOp = new RelationToStreamOpImpl();

        Task<Graph, Graph, JenaGraphOrBindings, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                .addR2ROperator(r2rOp1)
                .addR2ROperator(r2rOp2)
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
