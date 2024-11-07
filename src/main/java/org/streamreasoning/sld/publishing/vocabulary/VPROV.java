package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;

public class VPROV extends Vocabulary {

    private static final String uri = "http://w3id.org/rsp/vocals-prov#";

    public static final Node FILTER_OPERATOR = resource("FilterOperator");
    public static final Node OPERATOR = resource("Operator");
    public static final Node PHYSICAL_WINDOW = resource("PhysicalWindow");
    public static final Node R2R_OPERATOR = resource("R2ROperator");
    public static final Node R2S_OPERATOR = resource("R2SOperator");
    public static final Node RSPQL_OPERATOR = resource("RSPQLOperator");
    public static final Node REPLAY = resource("Replay");
    public static final Node S2R_OPERATOR = resource("S2ROperator");
    public static final Node S2S_OPERATOR = resource("S2SOperator");
    public static final Node SET_OPERATOR = resource("SetOperator");
    public static final Node STREAMING_AGENT = resource("StreamingAgent");
    public static final Node TASK = resource("Task");
    public static final Node TIME_BASED_WINDOW = resource("TimeBasedWindow");
    public static final Node TRANSFORM_OPERATOR = resource("TransformOperator");
    public static final Node WINDOW = resource("Window");
    public static final Node WINDOW_OPERATOR = resource("WindowOperator");

    public static final Node CONTAINED_IN = resource("containedIn");
    public static final Node CONTAINS = resource("contains");
    public static final Node ENDED_AT = resource("endedAt");
    public static final Node FOLLOWED_BY = resource("followedBy");
    public static final Node HAS_OUTPUT = resource("hasOutput");
    public static final Node PERFORMS = resource("performs");
    public static final Node PRECEDED_BY = resource("precededBy");
    public static final Node STARTED_AT = resource("startedAt");
    public static final Node STARTING_TIME = resource("startingTime");

    public static final Node QUERY = resource("query");
    public static final Node RANGE = resource("range");
    public static final Node STEP = resource("step");

    private static Node resource(String rdfStream) {
        return resource(uri, rdfStream);
    }

}
