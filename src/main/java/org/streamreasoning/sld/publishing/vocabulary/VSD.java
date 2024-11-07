package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public class VSD extends Vocabulary {

    private static String uri = "http://w3id.org/rsp/vocals-sd#";

    public final static Node URI_PARAM = resource("uri_param");
    public final static Node BODY_PARAM = resource("body_param");

    public final static Node CATALOG_SERVICE = resource("CatalogService");
    public final static Node OPERATION = resource("Operation");
    public final static Node PHYSICAL_WINDOW = resource("PhysicalWindow");
    public final static Node PROCESSING_SERVICE = resource("ProcessingService");
    public final static Node PUBLISHING_SERVICE = resource("PublishingService");
    public final static Node R2R_OPERATION = resource("R2ROperation");
    public final static Node R2S_OPERATION = resource("R2SOperation");
    public final static Node RDF_STREAM_SERVICE = resource("RDFStreamService");
    public final static Node RDF_STREAMING_FEATURE = resource("RDFStreamingFeature");
    public final static Node REPORTING_POLICY = resource("ReportingPolicy");
    public final static Node S2R_OPERATION = resource("S2ROperation");
    public final static Node S2S_OPERATION = resource("S2SOperation");
    public final static Node SET_OPERATION = resource("SetOperation");
    public final static Node STREAMING_DATASET = resource("StreamingDataset");
    public final static Node STREAMING_SERVICE = resource("StreamingService");
    public final static Node TIME_BASED_WINDOW = resource("TimeBasedWindow");
    public final static Node TIME_CONTROL = resource("TimeControl");
    public final static Node TIME_SEMANTICS = resource("TimeSemantics");
    public final static Node TIME_VARYING_GRAPH = resource("TimeVaryingGraph");
    public final static Node WINDOW = resource("Window");


    public final static Node HAS_SERVICE = resource("hasService");
    public final static Node pENDPOINT = resource("endpoint");
    public final static Node METHOD = resource("method");
    public final static Node BASE = resource("base");
    public final static Node PARAMS = resource("params");
    public final static Node INDEX = resource("index");


    public final static Node NAME = resource("name");
    public final static Node AVAILABLE_GRAPH = resource("availableGraph");
    public final static Node AVAILABLE_STREAM = resource("availableStream");
    public final static Node DEFAULT_GRAPH = resource("defaultGraph");
    public final static Node DEFAULT_STREAMING_DATASET = resource("defaultStreamingDataset");
    public final static Node DESCRIBED_BY = resource("describedBy");
    public final static Node ENDED_AT = resource("endedAt");
    public final static Node GRAPH = resource("graph");
    public final static Node HAS_FEATURE = resource("hasFeature");
    public final static Node NAMED_TIME_VARYING_GRAPH = resource("namedTimeVaryingGraph");
    public final static Node PUBLISHED_BY = resource("publishedBy");
    public final static Node REGISTERED_TASK = resource("registeredTask");
    public final static Node REGISTERED_BY = resource("registeredBy");
    public final static Node REGISTERED_STREAMS = resource("registeredStreams");
    public final static Node RESULT_FORMAT = resource("resultFormat");
    public final static Node RSP_ENDPOINT = resource("rspEndpoint");
    public final static Node STARTED_AT = resource("startedAt");

    private static Node resource(String rdfStream) {
        return NodeFactory.createURI(uri);
    }


    public static Triple publisher(Node s) {
        return triple(s, RDF.pTYPE, PUBLISHING_SERVICE);
    }


}
