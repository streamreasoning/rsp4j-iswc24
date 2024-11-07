package org.streamreasoning.sld.processing.neo4j.events;

public interface PGraph {


    Node[] nodes();

    Edge[] edges();

    default long timestamp() {
        return System.currentTimeMillis();
    }

    interface Node {

        long id();

        String[] labels();

        String[] properties();

        Object property(String p);

    }

    interface Edge extends Node {

        long to();

        long from();

    }
}
