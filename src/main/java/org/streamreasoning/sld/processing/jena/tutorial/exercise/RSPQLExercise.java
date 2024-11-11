package org.streamreasoning.sld.processing.jena.tutorial.exercise;

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


public class RSPQLExercise {


    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

        /***
         * Observation Stream contains both RFID Observations and Facebook Posts:
         * URI: http://rsp4j.io/covid/observations
         * RFID Observation:
         <http://rsp4j.io/covid/_observation16> <http://www.w3.org/ns/sosa/hasFeatureOfInterest> <http://rsp4j.io/covid/Alice>;
                                                <http://www.w3.org/ns/sosa/madeBySensor> <http://rsp4j.io/covid/BlueRoom_senso>;
                                                <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rsp4j.io/covid/RFIDObservation>;
         <http://rsp4j.io/covid/BlueRoom_sensor> <http://www.w3.org/ns/sosa/hasLocation> <http://rsp4j.io/covid/BlueRoom>.
         <http://rsp4j.io/covid/Alice> <http://rsp4j.io/covid/isIn> <http://rsp4j.io/covid/BlueRoom>.
         *
         * Facebook Post:
         <http://rsp4j.io/covid/_post15> <http://rdfs.org/sioc/ns#has_creator> <http://rsp4j.io/covid/Elena>;
                                         <http://rdfs.org/sioc/ns#has_location> <http://rsp4j.io/covid/RedRoom>;
                                         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rsp4j.io/covid/FacebookPost>.
         <http://rsp4j.io/covid/Elena> <http://rsp4j.io/covid/isIn> <http://rsp4j.io/covid/RedRoom>.
         */

        /***
         * Test Results stream
         * URI: http://rsp4j.io/covid/testResults
         *
         * Test Result Observation:
         <http://rsp4j.io/covid/_observation9> <http://www.w3.org/ns/sosa/hasResult> <http://rsp4j.io/covid/positive>;
                                               <http://www.w3.org/ns/sosa/hasFeatureOfInterest> <http://rsp4j.io/covid/Bob>;
                                               <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rsp4j.io/covid/TestResultPost>.

         */

        /***
         * Contact tracing Stream
         * URI: http://rsp4j.io/covid/tracing
         <http://rsp4j.io/covid/_post8> <http://rdfs.org/sioc/ns#has_creator> <http://rsp4j.io/covid/David>;
                                        <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rsp4j.io/covid/ContactTracingPost>.
         <http://rsp4j.io/covid/David> <http://rsp4j.io/covid/isWith> <http://rsp4j.io/covid/Elena>.
         */

        // Task 1: Write a query to find the location (?room) of the person (?person)
        String queryWhoIsWhere = "PREFIX : <http://rsp4j.io/covid/>  " +
                "REGISTER ISTREAM <http://out/stream> AS " +
                "SELECT * " +
                "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +
                "WHERE {" +
                "   WINDOW ?w { ?s <http://rsp4j.io/covid/isIn> ?o } ." +
                "}";

        // Task 2: Write a query to find out the COVID positive persons (?person) and their location (?room)
        String queryWhoIsPositiveWhere = "PREFIX : <http://rsp4j.io/covid/>  " +
                "REGISTER ISTREAM <http://out/stream> AS " +
                "SELECT * " +
                "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +
                "WHERE {" +
                "   WINDOW ?w { ?s ?p ?o } ." +
                "}";
        // Task 3: Write a query to find the person at risk of
        String queryPersonAtRisk = "PREFIX : <http://rsp4j.io/covid/>  " +
                "REGISTER ISTREAM <http://out/stream> AS " +
                "SELECT * " +
                "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +
                "WHERE {" +
                "   WINDOW ?w { ?s ?p ?o } ." +
                "}";
        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse = RSPQLQueryFactory.parse(queryWhoIsWhere);

        System.out.println(parse);

        JenaCovidStreamsGenerator generator = new JenaCovidStreamsGenerator();

        // link the input streams
        generator.linkInputStreamByName(parse.instream());
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

