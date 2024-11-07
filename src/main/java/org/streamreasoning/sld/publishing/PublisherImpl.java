package org.streamreasoning.sld.publishing;


import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.streamreasoning.sld.publishing.vocabulary.VSD;

import java.util.Objects;

public class PublisherImpl implements SLD.Publisher {
    private final Node iri;
    private Graph graph;

    public PublisherImpl(Node iri, Graph g) {
        this.iri = iri;
        this.graph = g;
        graph.add(VSD.publisher(this.uri()));
    }

    @Override
    public Node uri() {
        return iri;
    }

    @Override
    public Graph describe() {
        return graph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublisherImpl publisher = (PublisherImpl) o;
        return Objects.equals(iri, publisher.iri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri, graph);
    }
}
