package org.streamreasoning.sld.publishing.enums;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public enum License {

    CC("https://creativecommons.org/licenses/by-nc/4.0/"), MIT("https://opensource.org/licenses/MIT"), Apache2("https://opensource.org/licenses/Apache-2.0");
    private final Node url;

    License(String s) {
        this.url = NodeFactory.createURI(s);
    }

    public Node url() {
        return url;
    }
}
