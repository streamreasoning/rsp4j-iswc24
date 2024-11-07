package org.streamreasoning.sld.publishing.distribution;


import com.sun.net.httpserver.HttpServer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.streamreasoning.sld.publishing.SLD;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HTTPDistribution<E> extends AbstractDistribution<E> {

    private Duration poll;
    private Integer retention;
    private LimitedList<E> state;

    public HTTPDistribution(Node uri, String access, License license, Format format, SLD.Publisher p, Graph graph, Integer retention) {
        super(uri, access, license, format, p, graph);
        this.retention = retention;
        this.state = new LimitedList<E>(retention);
    }

    public HTTPDistribution(Node uri, String access, License license, Format format, SLD.Publisher p, Graph graph, boolean source, Duration poll) {
        super(uri, access, license, format, p, graph, source);
        this.poll = poll;
    }

    @Override
    public SLD.WebDataStream<E> serve() {
        if (!source) {
            try {
//            if the uri is a fragment, we can spawn two different services
//            use abstract class to distinguish
//            get(access, (request, response) -> getString());
//
//            get("/access/" + access, (request, response) -> state);
//
//            init();
//            TODO actually, we need to pass the subset that interests the stream
                dataStream.addConsumer((inputStream, element, timestamp) -> {
                    System.out.println(element);
                    state.add(new Pair<>(element, timestamp));
                });
                HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

                server.createContext("/", t -> {
                    String response = HTTPDistribution.this.getString();
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                });

                HttpServer server2 = HttpServer.create(new InetSocketAddress(4040), 0);
                server2.createContext("/access/", t -> {
                    String response = Arrays.deepToString(state.toArray());
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                });

                server.setExecutor(Executors.newCachedThreadPool());
                server2.setExecutor(Executors.newCachedThreadPool());
                server.start();
                server2.start();
                return dataStream;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("Read-Only Distribution");
    }

    protected String getString() {
        StringWriter writer = new StringWriter();
        ModelFactory.createModelForGraph(graph).write(writer, "TTL");
        return writer.toString();
    }

    @Override
    public SLD.WebDataStream<E> start() {
        if (source) {
            Runnable task = () -> {
                while (true) {
                    URL urlCon;
                    try {
                        urlCon = new URL(access);
                        URLConnection conn = urlCon.openConnection();
                        InputStream is = conn.getInputStream();
                        String result = "";
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
                            result = br.lines().collect(Collectors.joining(System.lineSeparator()));
                            dataStream.put((E) result, System.currentTimeMillis());
                        }
                        Thread.sleep(this.poll.toMillis());
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(task);
            thread.start();
        }
        return dataStream;
    }

}

