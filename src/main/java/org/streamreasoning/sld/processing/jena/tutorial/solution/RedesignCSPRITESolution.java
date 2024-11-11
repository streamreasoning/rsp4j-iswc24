package org.streamreasoning.sld.processing.jena.tutorial.solution;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
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
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.csprite.CSpriteR2R;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.csprite.HierarchySchema;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.jena.TP;
import org.streamreasoning.sld.processing.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import org.streamreasoning.sld.processing.jena.sds.SDSJena;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.processing.jena.tutorial.JenaCovidStreamsGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedesignCSPRITESolution {

    public static void main(String[] args) throws InterruptedException {

        JenaCovidStreamsGenerator generator = new JenaCovidStreamsGenerator();

        DataStream<Graph> inputStream = generator.getStream("http://rsp4j.io/covid/observations");
        // define output stream
        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);

        JenaGraphOrBindings emptyContent = new JenaGraphOrBindings(GraphFactory.createGraphMem());



        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();

        CSpriteR2R csprite = new CSpriteR2R(Collections.singleton("http://rsp4j.io/covid/Update"), getHierarchySchema(), Collections.emptyList(), "partial_1");
        AccumulatorContentFactory<Graph, Graph, JenaGraphOrBindings> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> csprite.eval(Collections.singletonList(new JenaGraphOrBindings( g))).getContent(),
                JenaGraphOrBindings::new,
                JenaGraphOrBindings::new,
                emptyContent
        );

        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        RelationToRelationOperator<JenaGraphOrBindings> r2rOp2 = new TP(getTriple(), List.of(s2rOp.getName()), "partial_2");

        RelationToStreamOperator<JenaGraphOrBindings, Binding> r2sOp = new RelationToStreamOpImpl();




        Task<Graph, Graph, JenaGraphOrBindings, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                .addR2ROperator(r2rOp2)
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

    private static OpTriple getTriple() {
        Node s = Var.alloc("s");
        Node p = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node o = NodeFactory.createURI(JenaCovidStreamsGenerator.PREFIX + "Update");
        return new OpTriple(Triple.create(s, p, o));
    }

    private static HierarchySchema getHierarchySchema() {
        HierarchySchema hierarchySchema = new HierarchySchema();
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX_SOSA + "Observation", JenaCovidStreamsGenerator.PREFIX + "Update");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX_SIOC + "Post", JenaCovidStreamsGenerator.PREFIX + "Update");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "LocationObservation", JenaCovidStreamsGenerator.PREFIX_SOSA + "Observation");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "FacebookPost", JenaCovidStreamsGenerator.PREFIX_SIOC + "Post");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "LowFrequencyLocationObservation", JenaCovidStreamsGenerator.PREFIX + "LocationObservation");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "RFIDObservation", JenaCovidStreamsGenerator.PREFIX + "LowFrequencyLocationObservation");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "HighFrequencyLocationObservation", JenaCovidStreamsGenerator.PREFIX + "LocationObservation");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "BLEObservation", JenaCovidStreamsGenerator.PREFIX + "HighFrequencyLocationObservation");
        hierarchySchema.addSubClassOf(JenaCovidStreamsGenerator.PREFIX + "GPSObservation", JenaCovidStreamsGenerator.PREFIX + "HighFrequencyLocationObservation");
        return hierarchySchema;
    }
}
