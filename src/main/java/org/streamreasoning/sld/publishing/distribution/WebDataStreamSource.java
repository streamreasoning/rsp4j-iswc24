package org.streamreasoning.sld.publishing.distribution;


import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.sld.publishing.SLD;

import java.util.Objects;

public class WebDataStreamSource<E> implements SLD.WebDataStream<E> {

    private final SLD.Distribution d;
    private final SLD.Publisher p;
    private Graph description;
    private String stream_uri;

    public WebDataStreamSource(String stream_uri, Graph description, SLD.Distribution<E> d, SLD.Publisher p) {
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
    public SLD.Distribution[] distribution() {
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
        WebDataStreamSource<?> that = (WebDataStreamSource<?>) o;
        return Objects.equals(d, that.d) && Objects.equals(p, that.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), d, p, description);
    }

    @Override
    public void addConsumer(Consumer<E> windowAssigner) {

    }

    @Override
    public void put(E e, long ts) {

    }

    @Override
    public String getName() {
        return "";
    }
}
