package graph.neo4j;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.*;
import java.util.stream.Stream;

public class SDSNeo implements SDS<PGraphOrBindings> {

    private final Set<TimeVarying<PGraphOrBindings>> defs = new HashSet<>();
    private final Map<Node, TimeVarying<PGraphOrBindings>> tvgs = new HashMap<>();
    private final Node def = NodeFactory.createURI("def");

    @Override
    public Collection<TimeVarying<PGraphOrBindings>> asTimeVaryingEs() {
        return tvgs.values();
    }


    @Override
    public void add(String iri, TimeVarying<PGraphOrBindings> tvg) {
        tvgs.put(NodeFactory.createURI(iri), tvg);
    }

    @Override
    public void add(TimeVarying<PGraphOrBindings> tvg) {
        defs.add(tvg);
    }


    @Override
    public SDS<PGraphOrBindings> materialize(final long ts) {
        //TODO here applies the consolidation strategies
        //Default consolidation coaleces all the current
        //content graphs and produces the SDS to who execute the query.

        //I need to re-add all the triples because the dataset works on quads
        //Altenrativelt one can wrap it into a graph interface and update it directly within the tvg
        // this way there's no need to readd after materialization

        defs.forEach(g -> {
            g.materialize(ts);
        });

        tvgs.entrySet().forEach(e -> {
            e.getValue().materialize(ts);
        });

        return this;
    }

    @Override
    public Stream<PGraphOrBindings> toStream() {
        return null;
    }

}

