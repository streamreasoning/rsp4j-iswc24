package org.streamreasoning.sld.processing.jena.operatorsimpl.r2r.jena;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.sld.processing.jena.datatypes.JenaGraphOrBindings;

import java.util.ArrayList;
import java.util.List;

public class FullQueryUnaryReasoning implements RelationToRelationOperator<JenaGraphOrBindings> {

    private Query q;
    private String query;

    private List<String> tvgNames;

    private String resName;

    Reasoner reasoner;

    public FullQueryUnaryReasoning(String query, List<String> tvgNames, String resName, String schemaURI) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;
        this.q = QueryFactory.create(query);

        Model schema = RDFDataMgr.loadModel(schemaURI);

        this.reasoner = ReasonerRegistry.getRDFSReasoner();
        reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel,
                ReasonerVocabulary.RDFS_FULL);
        reasoner = reasoner.bindSchema(schema);

    }

    public FullQueryUnaryReasoning(Query query, List<String> tvgNames, String resName) {
        this.tvgNames = tvgNames;
        this.resName = resName;
        this.q = query;
        this.query = query.toString();
    }

    @Override
    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {

        JenaGraphOrBindings dataset = datasets.get(0);

        q.getProjectVars();
        Node aDefault = NodeFactory.createURI(tvgNames.get(0));
        DatasetGraph dg = new DatasetGraphInMemory();
        Graph g = dataset.getContent();

        dg.addGraph(aDefault, g);
        dg.addGraph(NodeFactory.createURI("inf"), reasoner.bind(g));

        QueryExecution queryExecution = QueryExecutionFactory.create(q, DatasetImpl.wrap(dg));
        ResultSet resultSet = queryExecution.execSelect();

        List<Binding> res = new ArrayList<>();

        while (resultSet.hasNext()) {

            ResultBinding rb = (ResultBinding) resultSet.next();
            res.add(rb.getBinding());

        }

        dataset.setResult(res);
        return dataset;

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
