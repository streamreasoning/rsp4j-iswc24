package graph.neo4j;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoBindingStream implements DataStream<Map<String, Object>> {
    protected String stream_uri;
    protected List<Consumer<Map<String, Object>>> consumers = new ArrayList<>();

    public NeoBindingStream(String stream_uri) {
        this.stream_uri = stream_uri;
    }

    @Override
    public void addConsumer(Consumer<Map<String, Object>> windowAssigner) {
        consumers.add(windowAssigner);
    }

    @Override
    public void put(Map<String, Object> e, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(this, e, ts));
    }

    @Override
    public String getName() {
        return stream_uri;
    }
}
