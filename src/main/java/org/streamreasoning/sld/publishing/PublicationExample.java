package org.streamreasoning.sld.publishing;



import org.streamreasoning.sld.publishing.builders.DistributionBuilder;
import org.streamreasoning.sld.publishing.builders.WebStreamBuilder;
import org.streamreasoning.sld.publishing.enums.Format;
import org.streamreasoning.sld.publishing.enums.License;
import org.streamreasoning.sld.publishing.enums.Protocol;

import java.util.Random;

public class PublicationExample {

    public static void main(String[] args) throws InterruptedException {

        String base = "http://localhost:3000";

        SLD.Publisher publisher = SLD.publisher(base);
        WebStreamBuilder wsb = new WebStreamBuilder(base);
        DistributionBuilder d = new DistributionBuilder(base);

        SLD.WebStream<String> ws = wsb
                .stream("colours", true)
                .title("Colour Stream")
                .description("stream of colours")
                .publisher(publisher)
                .distribution(d.access("colours", true) // defines if the distribution uri will be a fragment uri (need a proxy otherwise). (Can be used to change port)
                        .protocol(Protocol.HTTP)  // Determine what sink to choose
                        .license(License.CC) //mostly for documentation
                        .format(Format.STRING)) //relates with serialization strategy
                .<String>build();

        SLD.WebDataStream<String> stream = ws.serve();

        new Thread(() -> {
            try {
                Random r = new Random();
                String[] colors = new String[]{"Red", "Yellow", "Blue"};
                while (true) {
                    stream.put(colors[r.nextInt(colors.length - 1)], System.currentTimeMillis());
                    Thread.sleep(5 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }


}
