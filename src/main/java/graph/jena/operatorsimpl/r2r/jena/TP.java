package graph.jena.operatorsimpl.r2r.jena;

import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class TP implements UnaryOperator<JenaGraphOrBindings>, RelationToRelationOperator<JenaGraphOrBindings> {

    private OpTriple bgp;
    private List<String> tvgNames;
    private String unaryOpName;

    public TP(OpTriple bgp, List<String> tvgNames, String unaryOpName) {
        this.bgp = bgp;
        this.tvgNames = tvgNames;
        this.unaryOpName = unaryOpName;

    }

    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {
        QueryIterator exec = Algebra.exec(bgp, datasets.get(0).getContent());
        List<Binding> res = new ArrayList<>();
        while (exec.hasNext()) {
            res.add(exec.next());
        }
        JenaGraphOrBindings bindings = new JenaGraphOrBindings();
        bindings.setResult(res);
        return bindings;
    }


    public List<String> getTvgNames() {
        return tvgNames;
    }

    public String getResName() {
        return unaryOpName;
    }

    @Override
    public JenaGraphOrBindings apply(JenaGraphOrBindings bindings) {
        return null;
    }

    public Node getProperty() {
        return bgp.getTriple().getPredicate();
    }

    public Node getObject() {
        return bgp.getTriple().getObject();
    }
}