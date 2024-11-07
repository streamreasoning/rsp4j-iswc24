package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;

public class XSD extends Vocabulary {

    private static final String uri = "http://www.w3.org/2001/XMLSchema#";
    public static final Node tString = resource("string");

    private static Node resource(String rdfStream) {
        return resource(uri, rdfStream);
    }


}
