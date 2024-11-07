package org.streamreasoning.sld.processing.jena.sds;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SDSBinding implements SDS<List<Binding>> {

    private final Map<Node, TimeVarying<List<Binding>>> tvgs = new HashMap<>();
    private final Node def = NodeFactory.createURI("def");


    @Override
    public Collection<TimeVarying<List<Binding>>> asTimeVaryingEs() {
        return tvgs.values();
    }

    @Override
    public void add(String s, TimeVarying<List<Binding>> timeVarying) {
        tvgs.put(NodeFactory.createURI(s), timeVarying);
    }

    @Override
    public void add(TimeVarying<List<Binding>> timeVarying) {
        tvgs.put(def, timeVarying);
    }

    @Override
    public SDS<List<Binding>> materialize(long ts) {
        tvgs.values().stream().forEach(tvg -> tvg.materialize(ts));
        return this;
    }

    @Override
    public Stream<List<Binding>> toStream() {
        return tvgs.values().stream().map(TimeVarying::get);
    }
}

