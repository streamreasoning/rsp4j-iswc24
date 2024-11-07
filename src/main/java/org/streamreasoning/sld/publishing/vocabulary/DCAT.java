package org.streamreasoning.sld.publishing.vocabulary;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;


public class DCAT extends Vocabulary {

    private static final String uri = "http://www.w3.org/ns/dcat#";
    public static final Node pNAME = resource("name");
    public static final Node pPUBLISHER = resource("publisher");
    public static final Node pDESCRIPTION = resource("description");
    public static final Node pLICENSE = resource("license");
    public static final Node pFORMAT = resource("format");
    public static final Node pDATASET = resource("dataset");
    public static Node pACCESS = resource("access");
    public static Node pSECUTIRTY = resource("security");
    public static Node pPROTOCOL = resource("protocol");

    private static Node resource(String rdfStream) {
        return resource(uri, rdfStream);
    }

    public static Triple format(Node s, Format o) {
        return triple(s, pFORMAT, s);
    }

    public static Triple name(Node s, String name) {
        return triple(s, pNAME, NodeFactory.createLiteral(name));
    }

    public static Triple publisher(Node s, Node o) {
        return triple(s, pPUBLISHER, o);
    }

    public static Triple description(Node s, String description) {
        return triple(s, pDESCRIPTION, NodeFactory.createLiteral(description));
    }

    public static Triple dataset(Node s, Node o) {
        return triple(s, pDATASET, o);
    }

    public static Triple license(Node s, License l) {
        return triple(s, pLICENSE, l.url());
    }

    public static Triple access(Node uri, String s) {
        return triple(uri, pACCESS, NodeFactory.createLiteral(s));

    }
}
