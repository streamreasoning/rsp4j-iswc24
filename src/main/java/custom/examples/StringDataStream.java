package custom.examples;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class StringDataStream implements DataStream<String> {

    List<Consumer<String>> consumerList = new ArrayList<>();
    String name;

    public StringDataStream(String name) {
        this.name = name;
    }

    @Override
    public void addConsumer(Consumer<String> windowAssigner) {
        this.consumerList.add(windowAssigner);
    }

    @Override
    public void put(String fruit, long ts) {
        consumerList.forEach(c -> c.notify(this, fruit, ts));
    }

    @Override
    public String getName() {
        return name;
    }
}
