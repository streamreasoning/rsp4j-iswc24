package graph;


import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.List;

//Seraph query
public class ContinuousQuery<I, W, R extends Iterable<?>, O> {

    private final List<String> projections;
    private final String id;
    private final Task<I, W, R, O> task;
    private DataStream<O> output;
    private List<DataStream<I>> input;

    public ContinuousQuery(String id, Task task, List<String> projections, DataStream<O> output, List<DataStream<I>> input) {
        this.projections = projections;
        this.id = id;
        this.task = task;
        this.output = output;
        this.input = input;
    }

    public String id() {
        return this.id;
    }

    public List<String> getResultVars() {
        return projections;
    }

    public Task<I, W, R, O> getTask() {
        return task;
    }

    public void setOutput(DataStream<O> output) {
        this.output = output;
    }

    public DataStream<O> outstream() {
        return output;
    }

    public void setInput(DataStream<I> output) {
        this.input = input;
    }

    public List<DataStream<I>> instream() {
        return input;
    }
}
