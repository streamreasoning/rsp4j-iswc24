package org.streamreasoning.sld.processing.jena.syntax;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import org.streamreasoning.sld.processing.shared.ContinuousQuery;
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class ParsingExample {


    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String query = "PREFIX : <http://example.org/> . " +
                       "REGISTER ISTREAM <http://out/stream> AS " +
                       "SELECT * " +
//                       "FROM NAMED WINDOW :window1 ON :stream1 [RANGE PT10S STEP PT5S] " +
                       "FROM NAMED WINDOW :window1 ON :stream1 [RANGE PT10S STEP PT5S] " +
                       "FROM NAMED WINDOW :window2 ON :stream2 [RANGE PT10S STEP PT5S] " +
                       "WHERE {" +
                       "   WINDOW ?w { ?a ?b ?c } ." +
                       "}";

        System.out.println(query);

        ContinuousQuery<Graph, Graph, JenaGraphOrBindings, Binding> parse = RSPQLQueryFactory.parse(query);

        System.out.println(parse);

        parse.getTask().initialize();
    }


}

