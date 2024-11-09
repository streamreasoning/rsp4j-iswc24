package org.streamreasoning.sld.publishing.builders;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.sld.publishing.SLD;
import org.streamreasoning.sld.publishing.vocabulary.DCAT;
import org.streamreasoning.sld.publishing.vocabulary.VOCALS;

import java.util.ArrayList;
import java.util.List;

public class WebStreamBuilder {

    private SLD.Publisher p;
    private String base;
    private Node uri;
    private String name;
    private String description;
    private List<DistributionBuilder> distributionBuilders = new ArrayList<>();
    private Graph graph = GraphFactory.createGraphMem();
    private String id;
    private boolean fragment;

    public WebStreamBuilder(String p) {
        this.base = p;

    }

    public String base() {
        return base;
    }

    public WebStreamBuilder publisher(SLD.Publisher p) {
        this.p = p;
        p.describe().stream().forEach(graph::add);
        return this;
    }

    public WebStreamBuilder stream(String id, boolean fragment) {
        this.fragment = fragment;
        this.uri = fragment ? NodeFactory.createURI(this.base + "/" + id) : NodeFactory.createURI(id);
        this.id = id;
        Triple descriptor = VOCALS.descriptor();
        this.graph.add(descriptor);
        this.graph.add(VOCALS.stream(uri));
        this.graph.add(DCAT.dataset(descriptor.getSubject(), uri));
        return this;
    }

    public WebStreamBuilder title(String name) {
        this.name = name;
        graph.add(DCAT.name(uri, name));
        return this;
    }

    public WebStreamBuilder description(String description) {
        this.description = description;
        graph.add(DCAT.description(uri, name));
        return this;
    }

    public WebStreamBuilder distribution(DistributionBuilder db) {
        db.publisher(p);
        distributionBuilders.add(db);
        db.describe().stream().forEach(t -> graph.add(t));
        return this;
    }

    public <T> SLD.WebStream<T> build() {
        return new SLD.WebStream<T>() {
            @Override
            public SLD.Publisher publisher() {
                return p;
            }

            @Override
            public SLD.WebDataStream<T> serve() {
                DistributionBuilder distributionBuilder = distributionBuilders.get(0);
                return distributionBuilder.<T>buildSink(graph).serve();
            }

            @Override
            public Graph describe() {
                return graph;
            }

            @Override
            public Node uri() {
                return uri;
            }
        };
    }

}