package graph.jena.operatorsimpl.r2r.csprite;


import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class R2RUpwardExtension implements RelationToRelationOperator<JenaGraphOrBindings> {

    private final Node RDFTYPE = RDF.type.asNode();
    private final List<String> tvgNames;
    private final String unaryOpName;
    private UpwardExtension extension;

    public R2RUpwardExtension(UpwardExtension extension, List<String> tvgNames, String unaryOpName) {
        this.extension = extension;
        this.tvgNames = tvgNames;
        this.unaryOpName = unaryOpName;
    }

    private List<Triple> performUpwardExtension(Triple t) {
        if (isTypeAssertion(t)) {
            Node type = t.getObject();
            //reasoning step
            List<Triple> upward = extension.getUpwardExtension(type.getURI()).stream()
                    .map(parent -> Triple.create(t.getSubject(), RDFTYPE, NodeFactory.createURI(parent)))
                    .collect(Collectors.toList());
            upward.add(t);
            return upward;
        } else {
            return Collections.singletonList(t);
        }
    }

    private boolean isTypeAssertion(Triple t) {
        return RDFTYPE.equals(t.getPredicate());
    }

    public TimeVarying<Collection<Graph>> apply(SDS<Graph> sds) {
        return null;
    }

    @Override
    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {
        Graph graphMem = GraphFactory.createGraphMem();
        datasets.forEach(
                g -> g.getContent().stream()
                        .map(this::performUpwardExtension)
                        .flatMap(Collection::stream)
                        .forEach(graphMem::add)
        );
        return new JenaGraphOrBindings(graphMem);
    }

    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return unaryOpName;
    }
}
