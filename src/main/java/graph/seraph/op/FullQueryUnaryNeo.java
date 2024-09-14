package graph.seraph.op;

import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphOrResult;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestDatabaseManagementServiceBuilder;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;

import java.util.*;

public class FullQueryUnaryNeo implements RelationToRelationOperator<PGraphOrResult> {

    protected GraphDatabaseService db;
    private final String query;
    private final List<String> tvgNames;
    private final String resName;

    public FullQueryUnaryNeo(String query, List<String> tvgNames, String resName) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;
        TestDatabaseManagementServiceBuilder builder = new TestDatabaseManagementServiceBuilder();
        DatabaseManagementService dbm = builder.impermanent().build();
        this.db = dbm.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
    }

    @Override
    public PGraphOrResult eval(List<PGraphOrResult> datasets) {

        PGraphOrResult dataset = datasets.get(0);
        List<graph.seraph.events.Result> res = new ArrayList<>();

        //populate the database
        Transaction txd = db.beginTx();
        txd.getAllNodes().forEach(node -> {
            node.getRelationships().forEach(Relationship::delete);
            node.delete();
        });
        txd.commit();
        //TODO First run query (delete n when n.prov == stream(name)) | added the execute delete query
        //TODO create a query that adds all the information into the elements
        final Transaction tx = db.beginTx();
        PGraph g = dataset.getContent();
        Map<Long, Node> ids = new HashMap<>();
        Arrays.stream(g.nodes()).forEach(n1 -> {
            Node n = tx.createNode();
            Arrays.stream(n1.labels()).forEach(s -> n.addLabel(Label.label(s)));
            Arrays.stream(n1.properties()).forEach(p -> {
                Object property = getProperty(n1, p);
                n.setProperty(p, property);
            });
            ids.put(n1.id(), n);
        });
        //TODO Assumption on EDGES, they only refer to nodes in the current graph because we better use internal ids
        Arrays.stream(g.edges()).forEach(e -> {
            Node from = ids.computeIfAbsent(e.from(), l -> tx.createNode());
            Node to = ids.computeIfAbsent(e.to(), l -> tx.createNode());
            Arrays.stream(e.labels()).forEach(l -> {
                Relationship r = from.createRelationshipTo(to, RelationshipType.withName(l));
                Arrays.stream(e.properties()).forEach(p -> {
                    Object property = getProperty(e, p);
                    r.setProperty(p, property);
                });
            });
        });
        ids.clear();

        //execute the query
        tx.commit();

        final Transaction txe = db.beginTx();

        Result result = txe.execute(query);
        while (result.hasNext()) {
            Map<String, Object> next = result.next();
            res.add(new graph.seraph.events.Result(next));
//        |name-->Fred
//        |age-->22
        }
        txe.commit();

        txe.close();
        tx.close();

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

    private Object getProperty(PGraph.Node n1, String p) {
        Object property = n1.property(p);
        if (property instanceof Map) {
            return ((Map<?, ?>) property).entrySet().stream().findFirst().get().getValue();
        }
        return property;
    }
}
