package org.streamreasoning.sld.tutorial.processing.solution;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.contentimpl.factories.AccumulatorContentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.r2r.R2RBase;
import org.streamreasoning.polyflow.base.operatorsimpl.r2s.RStream;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.LogicalSlidingWindow;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.sld.processing.jena.sds.SDSBinding;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.tutorial.processing.custom.JenaCovidStreamsGenerator;
import org.streamreasoning.sld.tutorial.processing.custom.CQELSReporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedesignCQELS {

    /*
    * CQELS engine is the first native RSP engine.
    * It is designed to push the evaluation of triple patterns directly on stream elements
    * It also include a number of adpative optimisation that keep the query execution efficient.
    * */
    public static void main(String[] args) throws InterruptedException {

        ContinuousProgram<Triple, Binding, List<Binding>, Binding> cp = new ContinuousProgramImpl<>();

        JenaCovidStreamsGenerator generator = new JenaCovidStreamsGenerator();


        // define in/out streams
        /*
         * CQELS consumes streams of triples. This is different from CSPARQL
         * and it is required by the query semantics.
         * */
        DataStream<Triple> inputStream =  generator.getStreamTriple("http://rsp4j.io/covid/observations");


        //The output stream contains the query bindings
        DataStream<Binding> outStream = new JenaBindingStream("out");

        // Reporting Policy
        /*
        * CQELS reporting policy is called "On content Change"
        * it is a reactive-oriented policy that aims at reducing the latency of the result
        * the reporting happens only when the content of the window differs
        * */

        Report report = new ReportImpl();
        report.add(new CQELSReporting());

        Time instance = new TimeImpl(0);

        ArrayList<Binding> emptyList = new ArrayList<>();

        /*
         * CQELS pushes the application of the triple pattern in the window
         * this means that every element of the input triple stream is
         * transformed into a binding (or empty).
         *
         * Below we define its content factory
         *
         * */
        AccumulatorContentFactory<Triple, Binding, List<Binding>> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (t) -> createBinding(getTriple(), t),
                (b) -> {
                    List<Binding> bs = new ArrayList<>();
                    bs.add(b);
                    return bs;
                },
                (l1, l2) -> {
                    l2.addAll(l1);
                    return l2;
                },
                emptyList
        );

        StreamToRelationOperator<Triple, Binding, List<Binding>> s2rOp =
                new LogicalSlidingWindow<>(
                        instance,
                        JenaCovidStreamsGenerator.PREFIX + "w1",
                        accumulatorContentFactory,
                        report,
                        1000);

        RelationToRelationOperator<List<Binding>> r2rOp1 = new R2RBase(Collections.singletonList(s2rOp.getName()), "partial_1");//new FullQueryUnaryJena("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), "partial_1");
        RelationToStreamOperator<List<Binding>, Binding> r2sOp = new RStream<>();

        Task<Triple, Binding, List<Binding>, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                .addR2ROperator(r2rOp1)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSBinding())
                .addTime(instance);
        task.initialize();

        List<DataStream<Triple>> inputStreams = new ArrayList<>();
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
        Node o = Var.alloc("o");
//        NodeFactory.createURI(JenaCovidStreamsGenerator.PREFIX + "RFIDObservation");
        return Triple.create(s, p, o);
    }
}
