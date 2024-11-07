package org.streamreasoning.sld.publishing.distribution;


import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.sld.publishing.SLD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebDataStreamSink<E> implements SLD.WebDataStream<E> {

    private final SLD.Distribution<E> d;
    private final SLD.Publisher p;
    private String stream_uri;
    private Graph description;
    private List<Consumer> consumers = new ArrayList<>();

    public WebDataStreamSink(String stream_uri, Graph description, SLD.Distribution<E> d, SLD.Publisher p) {
        this.description = description;
        this.d = d;
        this.p = p;
        this.stream_uri = stream_uri;
    }

    @Override
    public Graph describe() {
        return description;
    }

    @Override
    public SLD.Publisher publisher() {
        return p;
    }

    @Override
    public SLD.Distribution<E>[] distribution() {
        return new SLD.Distribution[]{d};
    }

    @Override
    public Node uri() {
        return NodeFactory.createURI(stream_uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WebDataStreamSink<?> that = (WebDataStreamSink<?>) o;
        return Objects.equals(d, that.d) && Objects.equals(p, that.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), d, p);
    }

    @Override
    public void addConsumer(Consumer<E> windowAssigner) {
        consumers.add(windowAssigner);
    }

    @Override
    public void put(E e, long ts) {
        consumers.forEach(c -> c.notify(this, e, ts));
    }

    @Override
    public String getName() {
        return uri().getLocalName();
    }
}
