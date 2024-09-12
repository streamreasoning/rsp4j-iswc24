package graph.neo4j;

import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class PGraphStreamGenerator {
    private static final String PREFIX = "http://test/";
    private static final Long TIMEOUT = 1000l;

    private final String[] colors = new String[]{"Blue", "Green", "Red", "Yellow", "Black", "Grey", "White"};
    private final Map<String, DataStream<PGraph>> activeStreams;
    private final AtomicBoolean isStreaming;
    private final Random randomGenerator;
    private AtomicLong streamIndexCounter;

    private String prefixes;
    private String[] fileNames = {"/activity.trig", "/location.trig", "/heart.trig", "/breathing.trig", "/oxygen.trig"};
    private List<Scanner> scanners = new ArrayList<>();


    public PGraphStreamGenerator() {
        this.streamIndexCounter = new AtomicLong(0);
        this.activeStreams = new HashMap<String, DataStream<PGraph>>();
        this.isStreaming = new AtomicBoolean(false);
        randomGenerator = new Random(1336);
        try {
            for (int i = 0; i < fileNames.length; i++) {
                scanners.add(new Scanner(new File(PGraphStreamGenerator.class.getResource(fileNames[i]).getPath())));
                //Read prefixes from all files
                prefixes = scanners.get(i).nextLine();
            }
        } catch (FileNotFoundException ignored) {
        }

    }

    public static String getPREFIX() {
        return PGraphStreamGenerator.PREFIX;
    }

    public DataStream<PGraph> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            PGStream stream = new PGStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }

    public void startStreaming() {
        if (!this.isStreaming.get()) {
            this.isStreaming.set(true);
            Runnable task = () -> {
                long ts = 0;
                while (this.isStreaming.get()) {
                    long finalTs = ts;
                    activeStreams.entrySet().forEach(e -> generateDataAndAddToStream(e.getValue(), finalTs));
                    ts += 200;
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

    private void generateDataAndAddToStream(DataStream<PGraph> stream, long ts) {
        String event = "testGraph" + (randomGenerator.nextInt(10));
        System.out.println(event);
        URL url = PGraphStreamGenerator.class.getClassLoader().getResource(event + ".json");
        try (FileReader fileReader = new FileReader(url.getPath())) {
            PGraph pGraph2 = PGraphImpl.fromJson(fileReader);
            stream.put(pGraph2, System.currentTimeMillis());
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private String selectRandomColor() {
        int randomIndex = randomGenerator.nextInt((colors.length));
        return colors[randomIndex];
    }

    public void stopStreaming() {
        this.isStreaming.set(false);
    }
}
