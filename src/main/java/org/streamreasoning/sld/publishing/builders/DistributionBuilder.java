package org.streamreasoning.sld.publishing.builders;


import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.sld.publishing.SLD;
import org.streamreasoning.sld.publishing.distribution.HTTPDistribution;
import org.streamreasoning.sld.publishing.distribution.WebSocketDistribution;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;
import org.streamreasoning.sld.publishing.enums.Protocol;
import org.streamreasoning.sld.publishing.enums.Security;
import org.streamreasoning.sld.web.publishing.enums.*;
import org.streamreasoning.sld.publishing.vocabulary.DCAT;
import org.streamreasoning.sld.publishing.vocabulary.VOCALS;

import java.time.Duration;

public class DistributionBuilder {

    private Graph dgraph = GraphFactory.createGraphMem();

    private License license;
    private Security security;
    private Protocol protocol;
    private Format format;
    private String base;
    private SLD.Publisher p;
    private String urlBody;
    private String id;
    private String access;
    private Node ep;
    private Node uri;

    public DistributionBuilder(String base) {
        this.base = base;
    }

    public Graph describe() {
        return dgraph;
    }

    public DistributionBuilder publisher(SLD.Publisher p) {
        this.p = p;
        this.base = p.uri().getURI();
        return this;
    }

    public DistributionBuilder access(String id) {
        return access(id, false);
    }

    public DistributionBuilder access(String id, boolean fragment) {
        this.id = id;
        this.uri = NodeFactory.createURI(((fragment) ? this.base + "/" + id : id));

        if (!fragment) {
            access = id;
        } else this.access = this.base.replace("http://", "");

        this.dgraph.add(VOCALS.endpoint(uri, this.ep = NodeFactory.createBlankNode()));
        this.dgraph.add(VOCALS.endpoint(this.ep));

        return this;
    }

    public DistributionBuilder protocol(Protocol protocol) {
        this.protocol = protocol;
        dgraph.add(ep, DCAT.pPROTOCOL, NodeFactory.createLiteral(protocol.name()));
        return this;
    }

    public DistributionBuilder security(Security security) {
        this.security = security;
        dgraph.add(ep, DCAT.pSECUTIRTY, NodeFactory.createLiteral(security.name()));
        return this;
    }

    public DistributionBuilder license(License license) {
        this.license = license;
        dgraph.add(DCAT.license(ep, license));
        return this;
    }

    public DistributionBuilder format(Format format) {
        this.format = format;
        dgraph.add(DCAT.format(ep, format));
        return this;
    }

    public <T> SLD.Distribution<T> buildSource(Graph sgraph) {
        switch (protocol) {
            case HTTP:
                return new HTTPDistribution<>(this.ep, access, license, format, p, sgraph, true, Duration.ofMillis(10000));
            case WebSocket:
            default:
                return new WebSocketDistribution<T>(this.ep, access, license, format, p, sgraph, true);


        }
    }

    public <T> SLD.Distribution<T> buildSink(Graph sgraph) {

        SLD.Distribution<T> d;
        switch (protocol) {
            case HTTP:
                d = new HTTPDistribution<>(this.ep, id, license, format, p, sgraph, 10);
                break;
            case WebSocket:
            default:
                d = new WebSocketDistribution<T>(this.ep, id, license, format, p, sgraph);
        }
        access = access.equals(id) ? access.replace(Protocol.HTTP.schema(), protocol.schema()) : protocol.schema() + access + "/access/" + id;
        this.dgraph.add(DCAT.access(this.ep, access));
        this.dgraph.stream().forEach(sgraph::add);

        return d;
    }


}
