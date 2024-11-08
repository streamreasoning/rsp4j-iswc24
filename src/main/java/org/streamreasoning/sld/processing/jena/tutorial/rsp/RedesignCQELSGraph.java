package org.streamreasoning.sld.processing.jena.tutorial.rsp;

import org.streamreasoning.polyflow.base.contentimpl.factories.AccumulatorContentFactoryFlat;
import org.streamreasoning.polyflow.base.operatorsimpl.r2r.R2RBase;
import org.streamreasoning.polyflow.base.operatorsimpl.r2s.RStream;
import org.streamreasoning.sld.processing.jena.sds.SDSBinding;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.processing.jena.stream.JenaStreamGenerator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
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
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.HoppingWindowOpImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedesignCQELSGraph {

    public static void main(String[] args) throws InterruptedException {

        ContinuousProgram<Graph, Binding, List<Binding>, Binding> cp = new ContinuousProgramImpl<>();

        JenaStreamGenerator generator = new JenaStreamGenerator();

        DataStream<Graph> inputStream = generator.getStream("http://test/stream1");
        // define output stream
        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);

//        JenaGraphOrBindings emptyContent = new JenaGraphOrBindings(GraphFactory.createGraphMem());

        Triple op = getTriple();
        ArrayList<Binding> emptyContent = new ArrayList<>();
        AccumulatorContentFactoryFlat<Graph, Binding, List<Binding>> accumulatorContentFactory = new AccumulatorContentFactoryFlat<>(
                (g) -> g.stream().map(t -> createBinding(op, t)),
                (b) -> {
                    List<Binding> bs = new ArrayList<>();
                    bs.add(b);
                    return bs;
                },
                (l1, l2) -> {
                    l2.addAll(l1);
                    return l2;
                },
                emptyContent
        );

        StreamToRelationOperator<Graph, Binding, List<Binding>> s2rOp =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        RelationToRelationOperator<List<Binding>> r2rOp1 = new R2RBase(Collections.singletonList(s2rOp.getName()), "partial_1");//new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), "partial_1");
//        RelationToRelationOperator<Bindings> r2rOp2 = new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), "partial_2");
//        RelationToRelationOperator<JenaGraphOrBindings> r2rOp3 = new FullQueryBinaryJena("", List.of("partial_1", "partial_2"), "partial_3");

        RelationToStreamOperator<List<Binding>, Binding> r2sOp = new RStream<>();

        Task<Graph, Binding, List<Binding>, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                .addR2ROperator(r2rOp1)
//                .addR2ROperator(r2rOp2)
//                .addR2ROperator(r2rOp3)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSBinding())
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

    private static Binding createBinding(Triple op, Triple t) {
        BindingBuilder bindingBuilder = BindingBuilder.create();
        boolean match = true;
        if (op.getSubject().isVariable())
            bindingBuilder.set((Var) op.getSubject(), t.getSubject());
        else match = match & op.getSubject().matches(t.getSubject());
        if (op.getPredicate().isVariable())
            bindingBuilder.set((Var) op.getPredicate(), t.getPredicate());
        else match = match & op.getPredicate().matches(t.getPredicate());
        if (op.getObject().isVariable())
            bindingBuilder.set((Var) op.getObject(), t.getObject());
        else match = match & op.getObject().matches(t.getObject());

        if (match) return bindingBuilder.build();
        else return bindingBuilder.reset().build();
    }

    private static Triple getTriple() {
        Node s = Var.alloc("s");
        Node p = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Node o = NodeFactory.createURI(JenaStreamGenerator.PREFIX + "Black");
        return Triple.create(s, p, o);
    }
}
