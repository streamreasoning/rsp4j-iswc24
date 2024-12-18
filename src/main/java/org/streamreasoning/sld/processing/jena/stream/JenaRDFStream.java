package org.streamreasoning.sld.processing.jena.stream;

import org.apache.jena.graph.Graph;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class JenaRDFStream implements DataStream<Graph> {

    protected String stream_uri;
    protected List<Consumer<Graph>> consumers = new ArrayList<>();

    public JenaRDFStream(String stream_uri) {
        this.stream_uri = stream_uri;
    }

    @Override
    public void addConsumer(Consumer<Graph> c) {
        consumers.add(c);
    }

    @Override
    public void put(Graph e, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(this, e, ts));
    }

    @Override
    public String getName() {
        return stream_uri;
    }

    public String uri() {
        return stream_uri;
    }

}
