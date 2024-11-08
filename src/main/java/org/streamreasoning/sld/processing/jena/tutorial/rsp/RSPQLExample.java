package org.streamreasoning.sld.processing.jena.tutorial.rsp;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;
import org.streamreasoning.sld.processing.jena.tutorial.JenaCovidStreamsGenerator;
import org.streamreasoning.sld.processing.jena.stream.JenaBindingStream;
import org.streamreasoning.sld.processing.jena.syntax.RSPQLQueryFactory;
import org.streamreasoning.sld.processing.shared.ContinuousQuery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class RSPQLExample {


    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

        // Task 1: Write a query to find the location (?room) of the person (?person)
        String queryWhoIsWhere = "PREFIX : <http://rsp4j.io/covid/>  " +
                       "REGISTER ISTREAM <http://out/stream> AS " +
                       "SELECT * " +
                       "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +
                       "WHERE {" +
                       "   WINDOW ?w { ?person <http://rsp4j.io/covid/isIn> ?room } ." +
                       "}";

        // Task 2: Write a query to find out the COVID positive persons (?person) and their location (?room)
        String queryWhoIsPositiveWhere = "PREFIX : <http://rsp4j.io/covid/>  " +
                "REGISTER ISTREAM <http://out/stream> AS " +
                "SELECT * " +
                "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT10S] " +
                "WHERE {" +
                "   WINDOW ?w { " +
                "?person <http://rsp4j.io/covid/isIn> ?room .\n" +
                "?testObservation <http://www.w3.org/ns/sosa/hasFeatureOfInterest> ?person. \n" +
                "?testObservation <http://www.w3.org/ns/sosa/hasResult> <http://rsp4j.io/covid/positive> . " +
                "} ." +
                "}";
        // Task 3: Write a query to find the person at risk of
        String queryPersonAtRisk = "PREFIX : <http://rsp4j.io/covid/>  " +
                "REGISTER ISTREAM <http://out/stream> AS " +
                "SELECT * " +
                "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +
                "WHERE {" +
                "   WINDOW ?w { " +
                "?person <http://rsp4j.io/covid/isIn> ?room .\n" +
                "?testObservation <http://www.w3.org/ns/sosa/hasFeatureOfInterest> ?person. \n" +
                "?testObservation <http://www.w3.org/ns/sosa/hasResult> <http://rsp4j.io/covid/positive> . " +
                "?personAtRisk <http://rsp4j.io/covid/isWith> ?person ." +
                "} ." +
                "}";

        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse = RSPQLQueryFactory.parse(queryWhoIsPositiveWhere);

        System.out.println(parse);

        JenaCovidStreamsGenerator generator = new JenaCovidStreamsGenerator();

        // link the input streams
        generator.linkAndCoalescStreams(parse.instream());
        // define output stream
        JenaBindingStream outStream = (JenaBindingStream) parse.outstream();

        Task task = parse.getTask();

        List<DataStream<Binding>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();
        cp.buildTask(task, parse.instream(), outputStreams);

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(30_000);
        generator.stopStreaming();
    }


}

