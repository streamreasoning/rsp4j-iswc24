package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public abstract class VOCALS extends Vocabulary {

    private static final String uri = "http://w3id.org/rsp/vocals#";

    public static final Node RDF_SSTREAM = resource("RDFStream");
    public static final Node STREAM_ = resource("Stream");
    public static final Node STREAM_DESCRIPTOR = resource("StreamDescriptor");
    public static final Node STREAM_DISTRIBUTION = resource("StreamDistribution");
    public static final Node STREAM_ENDPOINT = resource("StreamEndpoint");
    public static final Node STREAM_PARTITION = resource("StreamPartition");
    public static final Node FEATURE = resource("feature");
    public static final Node PREVIOUS = resource("previous");
    public static final Node HAS_STREAM = resource("stream");
    public static final Node HAS_ENDPOINT = resource("hasEndpoint");
    public static final Node HAS_PARTITION = resource("hasPartition");

    private static Node resource(String rdfStream) {
        return resource(uri, rdfStream);
    }

    public static Triple stream(Node s) {
        return triple(s, RDF.pTYPE, STREAM_);
    }

    public static Triple endpoint(Node s) {
        return triple(s, RDF.pTYPE, STREAM_ENDPOINT);
    }

    public static Triple endpoint(Node s, Node o) {
        return triple(s, VSD.pENDPOINT, o);
    }

    public static Triple descriptor() {
        return Triple.create(NodeFactory.createURI(""), RDF.pTYPE, STREAM_DESCRIPTOR);

    }

}
