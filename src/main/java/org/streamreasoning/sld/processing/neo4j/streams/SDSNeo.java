package org.streamreasoning.sld.processing.neo4j.streams;

import org.streamreasoning.sld.processing.neo4j.events.PGraphOrResult;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SDSNeo implements SDS<PGraphOrResult> {

    private final Map<String, TimeVarying<PGraphOrResult>> tvgs = new HashMap<>();
    private final String def = "def";

    @Override
    public Collection<TimeVarying<PGraphOrResult>> asTimeVaryingEs() {
        return tvgs.values();
    }


    @Override
    public void add(String iri, TimeVarying<PGraphOrResult> tvg) {
        tvgs.put(iri, tvg);
    }

    @Override
    public void add(TimeVarying<PGraphOrResult> tvg) {
        tvgs.put(def, tvg);
    }


    @Override
    public SDS<PGraphOrResult> materialize(final long ts) {
        //TODO here applies the consolidation strategies
        //Default consolidation coaleces all the current
        //content graphs and produces the SDS to who execute the query.

        //I need to re-add all the triples because the dataset works on quads
        //Altenrativelt one can wrap it into a graph interface and update it directly within the tvg
        // this way there's no need to readd after materialization
        tvgs.entrySet().forEach(e -> {
            e.getValue().materialize(ts);
        });

        return this;
    }

    @Override
    public Stream<PGraphOrResult> toStream() {
        return tvgs.values().stream().map(TimeVarying::get);
    }

}

