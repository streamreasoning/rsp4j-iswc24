package graph.jena.iswc.tutorial;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import graph.ContinuousQuery;
import graph.jena.datatypes.JenaGraphOrBindings;
import graph.jena.stream.JenaBindingStream;
import graph.jena.syntax.RSPQLQueryFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class RSPQLExample {


    public static void main(String[] args) throws IOException, ConfigurationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

        String query = "PREFIX : <http://rsp4j.io/covid/>  " +
                       "REGISTER ISTREAM <http://out/stream> AS " +
                       "SELECT * " +
                       "FROM NAMED WINDOW :window1 ON <http://rsp4j.io/covid/observations> [RANGE PT10S STEP PT1S] " +

                        "WHERE {" +
                               "   WINDOW ?w { ?person <http://rsp4j.io/covid/isIn> ?room } ." +
                               "}";

        System.out.println(query);

        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse = RSPQLQueryFactory.parse(query);

        System.out.println(parse);

        JenaCovidStreamsGenerator generator = new JenaCovidStreamsGenerator();

        DataStream<Graph> inputStream = generator.getStream("http://rsp4j.io/covid/observations");
        // link the input streams
        generator.linkInputStreamByName(parse.instream());
        // define output stream
        JenaBindingStream outStream = (JenaBindingStream) parse.outstream();

       Task task =  parse.getTask();

        List<DataStream<Binding>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();
        cp.buildTask(task, parse.instream(), outputStreams);

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }


}

