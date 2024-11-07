package org.streamreasoning.sld.publishing;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.sld.publishing.builders.DistributionBuilder;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;
import org.streamreasoning.sld.publishing.enums.Protocol;
import org.streamreasoning.sld.publishing.vocabulary.DCAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.streamreasoning.sld.publishing.vocabulary.RDF.pTYPE;
import static org.streamreasoning.sld.publishing.vocabulary.VOCALS.STREAM_ENDPOINT;
import static org.streamreasoning.sld.publishing.vocabulary.VSD.PUBLISHING_SERVICE;


public class SLD {

    public static Query publisherQuery;

    static {
        String queryString = "SELECT * " +
                             "WHERE { " +
                             " ?endpoint <" + pTYPE + "> <" + STREAM_ENDPOINT + ">;" +
                             "<" + DCAT.pACCESS + "> ?access ; " +
                             "<" + DCAT.pPROTOCOL + "> ?protocol ; " +
                             "<" + DCAT.pLICENSE + "> ?license ; " +
                             "<" + DCAT.pFORMAT + "> ?format . } ";
        System.out.println(queryString);
        publisherQuery = QueryFactory.create(queryString);
    }

    public static <T> WebDataStream<T> fetch(String s) {
        Distribution<T>[] distributions = extractDistributions(fetchStreamDescriptor(s));
        return distributions[0].getWebStream();
    }

    public static Graph fetchStreamDescriptor(String uri) {
        return RDFDataMgr.loadModel(uri).getGraph();
    }

    public static <E> Distribution<E>[] extractDistributions(Graph descriptor) {

        List<DistributionBuilder> dbs = new ArrayList<>();

        Graph g = (Graph) descriptor;
        ResultSet resultSet = QueryExecutionFactory.create(publisherQuery, ModelFactory.createModelForGraph(g)).execSelect();

        while (resultSet.hasNext()) {
            Binding binding = resultSet.nextBinding();
            DistributionBuilder d = new DistributionBuilder("");
            d.access(binding.get(Var.alloc("access")).toString());
            d.protocol(Protocol.valueOf(binding.get(Var.alloc("protocol")).toString()));
            d.format(Format.valueOf(binding.get(Var.alloc("format")).toString()));
            String license1 = binding.get(Var.alloc("license")).getURI();
            Arrays.stream(License.values()).forEach(license -> {
                if (license.url().getURI().equals(license1))
                    d.license(license);
            });
            d.publisher(extractPublisher(descriptor));
            dbs.add(d);
        }


        return dbs.stream().map(db -> db.buildSource(descriptor)).collect(Collectors.toList()).toArray(new Distribution[dbs.size()]);
    }

    public static Publisher extractPublisher(Graph descriptor) {

        Query publisherQuery = QueryFactory.create("SELECT ?publisher  WHERE {?publisher a " + PUBLISHING_SERVICE + " } ");
        Graph g = descriptor;
        ResultSet resultSet = QueryExecutionFactory.create(publisherQuery, ModelFactory.createModelForGraph(g)).execSelect();

        while (resultSet.hasNext()) {
            Var publisher = Var.alloc("publisher");
            return SLD.publisher(resultSet.nextBinding().get(publisher).getURI());
        }
        return null;
    }


    public static Publisher publisher(String s) {
        return new PublisherImpl(NodeFactory.createURI(s), GraphFactory.createGraphMem());
    }


    public interface Distribution<T> extends Describable {

        Node uri();

        WebDataStream<T> start();

        WebDataStream<T> serve();

        WebDataStream<T> getWebStream();
    }


    public interface WebStream<E> extends Describable, Named {

        Publisher publisher();

        WebDataStream<E> serve();

    }


    public interface WebDataStream<E> extends DataStream<E>, Named, Describable {

        Publisher publisher();

        Distribution<E>[] distribution();

    }


    public interface Publisher extends Describable {

        Node uri();

    }


    protected interface Describable {

        Graph describe();

    }


    protected interface Named {

        Node uri();

    }

}
