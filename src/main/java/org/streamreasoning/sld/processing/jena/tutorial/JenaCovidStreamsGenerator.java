package org.streamreasoning.sld.processing.jena.tutorial;

import org.streamreasoning.sld.processing.jena.stream.JenaRDFStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class JenaCovidStreamsGenerator {
    public static final String PREFIX = "http://rsp4j.io/covid/";
    public static final String PREFIX_SOSA = "http://www.w3.org/ns/sosa/";
    public static final String PREFIX_SIOC = "http://rdfs.org/sioc/ns#";


    private static final Long TIMEOUT = 1000l;

    private final String[] colors = new String[]{"Blue", "Green", "Red", "Yellow", "Black", "Grey", "White"};
    private final Map<String, DataStream<Graph>> activeStreams;
    private final AtomicBoolean isStreaming;
    private final Random randomGenerator;
    private AtomicLong streamIndexCounter;

    private String prefixes;
    private List<Scanner> scanners = new ArrayList<>();

    private DataStream<Graph> observationStream;
    private DataStream<Graph> covidStream;
    private DataStream<Graph> contactStream;

    private enum Person {Alice, Bob, Elena, Carl, David, John};
    private enum Room {RedRoom, BlueRoom};
    private enum EventType {RFID, Facebook, ContactTracing, HospitalResult};

    Map<Person, EventType> personsEventTypesMap =
            Map.of(Person.Alice, EventType.RFID,
                    Person.John, EventType.RFID,
                    Person.Bob, EventType.Facebook,
                    Person.Elena, EventType.Facebook);

    Map<Person, Person> isWithMap =
            Map.of(Person.Carl, Person.Bob,
                    Person.David, Person.Elena);

    private Person[] isWithPersons = new Person[]{Person.Carl, Person.David};
    private Person[] evenTyptesPersons = new Person[]{Person.Alice, Person.John, Person.Bob, Person.Elena};


    public JenaCovidStreamsGenerator() {
        this.streamIndexCounter = new AtomicLong(0);
        this.activeStreams = new HashMap<String, DataStream<Graph>>();
        this.isStreaming = new AtomicBoolean(false);

        this.observationStream = new JenaRDFStream(PREFIX+"observations");
        activeStreams.put(PREFIX+"observations", observationStream);

        this.covidStream = new JenaRDFStream(PREFIX+"testResults");
        activeStreams.put(PREFIX+"testResults", covidStream);


        this.contactStream = new JenaRDFStream(PREFIX+"tracing");
        activeStreams.put(PREFIX+"tracing", contactStream);


        this.streamIndexCounter = new AtomicLong(0);
        randomGenerator = new Random(1337);

    }

    public static String getPREFIX() {
        return JenaCovidStreamsGenerator.PREFIX;
    }

    public DataStream<Graph> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            JenaRDFStream stream = new JenaRDFStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }


    public void startStreaming() {
        if (!this.isStreaming.get()) {
            this.isStreaming.set(true);
            Runnable task =
                    () -> {
                        long ts = 0;
                        while (this.isStreaming.get()) {
                            long finalTs = ts;
                            observationStream.put(createRandomObservationEvent(), ts);
                            if (randomGenerator.nextDouble() > 0.4) {
                                covidStream.put(createRandomCovidEvent(), ts);
                            }
                            if (randomGenerator.nextDouble() >= 0.4) {
                                contactStream.put(createRandomContactTracingEvent(), ts);
                            }
                            ts += 5 * 60 * 1000;
                            try {
                                Thread.sleep(TIMEOUT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private Person selectRandomPerson(Person[] persons){
        int randomIndex = randomGenerator.nextInt((persons.length));
        return persons[randomIndex];
    }
    private Person selectRandomPerson(){
        return selectRandomPerson(Person.values());
    }
    private Room selectRandomRoom(){
        int randomIndex = randomGenerator.nextInt((Room.values().length));
        return Room.values()[randomIndex];
    }
    public Graph createRandomObservationEvent(){
        Person randomPerson = selectRandomPerson(evenTyptesPersons);
        Room randomRoom = selectRandomRoom();
        EventType selectedType = personsEventTypesMap.get(randomPerson);

        Graph graph = GraphMemFactory.createGraphMem();

        Node a = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        long eventID = streamIndexCounter.incrementAndGet();
        switch (selectedType){
            case RFID:
                graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), a, NodeFactory.createURI(PREFIX + "RFIDObservation"));
                graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), NodeFactory.createURI(PREFIX_SOSA + "madeBySensor"), NodeFactory.createURI(PREFIX + randomRoom+"_sensor"));
                graph.add(NodeFactory.createURI(PREFIX + randomRoom+"_sensor"), NodeFactory.createURI(PREFIX_SOSA + "hasLocation"), NodeFactory.createURI(PREFIX + randomRoom));
                graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), NodeFactory.createURI(PREFIX_SOSA + "hasFeatureOfInterest"), NodeFactory.createURI(PREFIX + randomPerson));
                graph.add(NodeFactory.createURI(PREFIX + randomPerson), NodeFactory.createURI(PREFIX + "isIn"), NodeFactory.createURI(PREFIX + randomRoom));
                break;
            case Facebook:
                graph.add(NodeFactory.createURI(PREFIX + "_post" + eventID), a, NodeFactory.createURI(PREFIX + "FacebookPost"));
                graph.add(NodeFactory.createURI(PREFIX + "_post" + eventID), NodeFactory.createURI(PREFIX_SIOC + "has_location"), NodeFactory.createURI(PREFIX + randomRoom));
                graph.add(NodeFactory.createURI(PREFIX + "_post" + eventID), NodeFactory.createURI(PREFIX_SIOC + "has_creator"), NodeFactory.createURI(PREFIX + randomPerson));
                graph.add(NodeFactory.createURI(PREFIX + randomPerson), NodeFactory.createURI(PREFIX + "isIn"), NodeFactory.createURI(PREFIX + randomRoom));
                break;
            default:
                break;
        }
        return graph;
    }
    public Graph createRandomCovidEvent(){
        Person randomPerson = selectRandomPerson();


        Graph graph = GraphMemFactory.createGraphMem();

        Node a = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        long eventID = streamIndexCounter.incrementAndGet();

        graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), a, NodeFactory.createURI(PREFIX + "TestResultPost"));
        graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), NodeFactory.createURI(PREFIX_SOSA + "hasFeatureOfInterest"), NodeFactory.createURI(PREFIX + randomPerson));
        graph.add(NodeFactory.createURI(PREFIX + "_observation" + eventID), NodeFactory.createURI(PREFIX_SOSA + "hasResult"), NodeFactory.createURI(PREFIX + "positive"));
        return graph;
    }

    public Graph createRandomContactTracingEvent(){
        Person randomPerson = selectRandomPerson(isWithPersons);

        Graph graph = GraphMemFactory.createGraphMem();

        Node a = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        long eventID = streamIndexCounter.incrementAndGet();

        graph.add(NodeFactory.createURI(PREFIX + "_post" + eventID), a, NodeFactory.createURI(PREFIX + "ContactTracingPost"));
        graph.add(NodeFactory.createURI(PREFIX + "_post" + eventID), NodeFactory.createURI(PREFIX_SIOC + "has_creator"), NodeFactory.createURI(PREFIX + randomPerson));
        graph.add(NodeFactory.createURI(PREFIX + randomPerson), NodeFactory.createURI(PREFIX + "isWith"), NodeFactory.createURI(PREFIX + isWithMap.get(randomPerson)));

        return graph;
    }


    public void stopStreaming() {
        this.isStreaming.set(false);
    }

    public static void main(String[] args){
        JenaCovidStreamsGenerator gen = new JenaCovidStreamsGenerator();
        for (int i = 0; i < 10; i++) {
            System.out.println("New event:");
            System.out.println(gen.createRandomContactTracingEvent());
        }
    }
    public void linkInputStreamByName(List<DataStream<Graph>> inputStreams){
        for(DataStream<Graph> s : inputStreams){
            if(this.activeStreams.containsKey(s.getName())){
                this.activeStreams.get(s.getName()).addConsumer((out, el, ts) -> s.put(el,ts));
            }else{
                System.out.println("Stream not found: " + s.getName());
            }
        }
    }
    public void linkAndCoalescStreams(List<DataStream<Graph>> inputStreams){
        DataStream<Graph> inputStream = inputStreams.get(0);
        for(DataStream<Graph> s : this.activeStreams.values()){
                s.addConsumer((out, el, ts) -> inputStream.put(el,ts));

        }
    }
}
