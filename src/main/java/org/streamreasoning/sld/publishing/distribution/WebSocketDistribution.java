package org.streamreasoning.sld.publishing.distribution;


import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.sld.publishing.SLD;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;

import java.io.StringWriter;


public class WebSocketDistribution<E> extends AbstractDistribution<E> {
    protected WebSocketHandler<E> wsh;


    public WebSocketDistribution(Node uri, String access, License license, Format format, SLD.Publisher p, Graph graph) {
        super(uri, access, license, format, p, graph);
    }

    public WebSocketDistribution(Node uri, String access, License license, Format format, SLD.Publisher p, Graph graph, boolean source) {
        super(uri, access, license, format, p, graph, source);
    }

    @Override
    public SLD.WebDataStream<E> serve() {
        if (!source) {
            //if the uri is a fragment, we can spawn two different services
            //use abstract class to distinguish
            this.wsh = new WebSocketHandler<>();
//            webSocket("/access/" + access, this.wsh);
            StringWriter writer = new StringWriter();
            Graph graph = GraphFactory.createGraphMem();
            this.graph.stream().forEach(graph::add);
//            ModelFactory.createModelForGraph(graph.write(writer, "TTL");TODO
            String s = writer.toString();
            System.out.println(s);
//            get(ac?cess, (request, response) -> s);
//            init();
            //TODO actually, we need to pass the subset that interests the stream
//            dataStream.addConsumer(wsh);
            return dataStream;
        }
        throw new UnsupportedOperationException("Read-Only Distribution");
    }

    @Override
    public SLD.WebDataStream<E> start() {
        if (source) {
//            WebsocketClientSource<E> websocketSource = new WebsocketClientSource<E>(access, parsingStrategy);
//            websocketSource.startSocket();
//            websocketSource.addConsumer((arg, ts) -> dataStream.put(arg, ts));
        }
        return dataStream;
    }

}

