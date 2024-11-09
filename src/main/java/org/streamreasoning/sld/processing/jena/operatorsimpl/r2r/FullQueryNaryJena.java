package org.streamreasoning.sld.processing.jena.operatorsimpl.r2r;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;

import java.util.ArrayList;
import java.util.List;

public class FullQueryNaryJena implements RelationToRelationOperator<JenaGraphOrBindings> {

    private final Query q;
    private String query;
    private List<String> tvgNames;
    private String resName;

    public FullQueryNaryJena(String query, List<String> tvgNames, String resName) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;
        this.q = QueryFactory.create(query);

    }

    public FullQueryNaryJena(Query q, List<String> tvgNames, String resName) {
        this.query = q.toString();
        this.tvgNames = tvgNames;
        this.resName = resName;
        this.q = q;

    }

    @Override
    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {
        JenaGraphOrBindings ds1 = datasets.get(0);
        JenaGraphOrBindings ds2 = datasets.get(1);

        if (query == null || query.isEmpty()) {
            JenaGraphOrBindings res = new JenaGraphOrBindings();
            List<Binding> resBinding = new ArrayList<>();
            datasets.forEach(ds -> resBinding.addAll(ds.getResult()));
            res.setResult(resBinding);
            //  res.setContent(new Union(ds1.getContent(), ds2.getContent()));
            return res;
        } else {
            DatasetGraph dg = new DatasetGraphInMemory();
            for (int i = 0; i < tvgNames.size(); i++) {
                JenaGraphOrBindings bindings = datasets.get(i);
                if (bindings.getContent() != null) {
                    dg.addGraph(NodeFactory.createURI(tvgNames.get(i)), bindings.getContent());
                }
            }

            ResultSet resultSet = QueryExecutionFactory.create(q, DatasetImpl.wrap(dg)).execSelect();

            List<Binding> res = new ArrayList<>();
            while (resultSet.hasNext()) {
                ResultBinding rb = (ResultBinding) resultSet.next();
                res.add(rb.getBinding());
            }

            JenaGraphOrBindings bindings = new JenaGraphOrBindings(new Union(ds1.getContent(), ds2.getContent()));
            bindings.setResult(res);
            return bindings;
        }
    }


    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return resName;
    }
}
