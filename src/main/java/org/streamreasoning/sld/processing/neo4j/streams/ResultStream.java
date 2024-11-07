package org.streamreasoning.sld.processing.neo4j.streams;

import org.streamreasoning.sld.processing.neo4j.events.Result;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class ResultStream implements DataStream<Result> {
    protected String stream_uri;
    protected List<Consumer<Result>> consumers = new ArrayList<>();

    public ResultStream(String stream_uri) {
        this.stream_uri = stream_uri;
    }

    @Override
    public void addConsumer(Consumer<Result> windowAssigner) {
        consumers.add(windowAssigner);
    }

    @Override
    public void put(Result e, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(this, e, ts));
    }

    @Override
    public String getName() {
        return stream_uri;
    }
}
