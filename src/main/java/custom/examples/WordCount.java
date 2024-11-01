package custom.examples;

import custom.DummyR2ROp;
import custom.customdatatypes.FruitBasket;
import custom.customoperators.CustomTumblingWindow;
import custom.customoperators.RelationToStreamOpImpl;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.contentimpl.factories.AccumulatorContentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.polyflow.base.sds.SDSDefault;

import java.util.*;

/*
 * This is the complete code of the step-by-step guide present on GitHub.
 */

public class WordCount {

    public static void main(String[] args) throws InterruptedException {

        /*------------Input and Output Stream definitions------------*/

        // Define a generator to create input elements
        // Define an input stream
        DataStream<String> inputStreamFruit = new StringDataStream("stream1");//

        // define an output stream
        DataStream<String> outStream = new StringDataStream("out");//

        /*------------Window Content------------*/

        //Entity that represents a neutral element for our operations on the 'R' data type

        // Factory object to manage the window content, more information on our GitHub guide!
        IterableMap emptyBasket = new IterableMap();
        ContentFactory<String, Map<String, Integer>, IterableMap> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (fruit) -> {
                    Map<String, Integer> entries = new HashMap<>();
                    for (String w : fruit.split("\s")) {
                        entries.computeIfPresent(w, (key, value) -> value + 1);
                        entries.computeIfAbsent(w, (key) -> 1);
                    }
                    return entries;
                },
                IterableMap::new,
                (m1, m2) -> {
                    m1.map.forEach((key, value) -> m2.map.merge(key, value, Integer::sum));
                    return m2;
                },
                emptyBasket
        );

//        ContentFactory<String, String[], IterableMap> accumulatorContentFactory2 = new AccumulatorContentFactory<>(
//                (fruit) -> fruit.split("\s"),
//                IterableMap::new,
//                (m1, m2) -> {
//                    m1.map.forEach((key, value) -> m2.map.merge(key, value, Integer::sum));
//                    return m2;
//                },
//                emptyBasket
//        );

        /*------------Window Properties-    -----------*/

        // Window properties (report)
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        //Time object used to represent the time in our application
        Time instance = new TimeImpl(0);


        /*------------S2R, R2R and R2S Operators------------*/

        //Define the Stream to Relation operator (blueprint of the windows)
        StreamToRelationOperator<String, Map<String, Integer>, IterableMap> fruit_s2r_one =
                new CustomTumblingWindow<>(
                        instance,
                        "TumblingWindow",
                        accumulatorContentFactory,
                        report,
                        1000);

        //Define Relation to Relation operators and chain them together. Here we filter out fruits that are underripe
        RelationToRelationOperator<IterableMap> r2r_filter_underripe = new DummyR2ROp<>(Collections.singletonList(fruit_s2r_one.getName()), "filtered_fruit");

        //Relation to Stream operator, take the final fruit basket and send out each fruit
        RelationToStreamOperator<IterableMap, String> r2sOp = new RelationToStreamOpImpl();


        /*------------Task definition------------*/

        //Define the Tasks, each of which represent a query
        Task<String, Map<String, Integer>, IterableMap, String> task = new TaskImpl<>();
        task = task
                .addS2ROperator(fruit_s2r_one, inputStreamFruit)
                .addR2ROperator(r2r_filter_underripe)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSDefault<>())
                .addTime(instance);
        task.initialize();




        /*------------Continuous Program definition------------*/

        //Define the Continuous Program, which acts as the coordinator of the whole system
        ContinuousProgram<String, Map<String, Integer>, IterableMap, String> cp = new ContinuousProgramImpl<>();

        List<DataStream<String>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStreamFruit);

        List<DataStream<String>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);


        cp.buildTask(task, inputStreams, outputStreams);


        /*------------Output Stream consumer------------*/

        outStream.addConsumer((out, el, ts) -> System.out.println("Output Element: [" + el + "]" + " @ " + ts));

    }


}
