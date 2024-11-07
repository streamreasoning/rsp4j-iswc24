package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;

public class RDF extends Vocabulary {

    private static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final Node pTYPE = resource("type");

    private static Node resource(String rdfStream) {
        return resource(uri, rdfStream);
    }


}
