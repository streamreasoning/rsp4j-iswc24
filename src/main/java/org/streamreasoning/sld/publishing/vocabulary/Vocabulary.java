package org.streamreasoning.sld.publishing.vocabulary;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public class Vocabulary {

    public static Node resource(String uri, String local) {
        return NodeFactory.createURI(uri + local);
    }

    public static Triple triple(Node s, Node p, Node o) {
        return Triple.create(s, p, o);
    }

}
