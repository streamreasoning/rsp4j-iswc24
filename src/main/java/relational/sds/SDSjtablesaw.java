package relational.sds;


import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SDSjtablesaw implements SDS<Table> {

    private final List<TimeVarying<Table>> tvgs = new ArrayList<>();

    @Override
    public Collection<TimeVarying<Table>> asTimeVaryingEs() {
        return tvgs;
    }

    @Override
    public void add(String iri, TimeVarying<Table> tvg) {

    }

    @Override
    public void add(TimeVarying<Table> tvg) {
        tvgs.add(tvg);
    }

    @Override
    public SDS<Table> materialize(long ts) {

        for (TimeVarying<Table> tvg : tvgs) {
            tvg.materialize(ts);
        }
        return this;
    }

    @Override
    public Stream<Table> toStream() {
        return tvgs.stream().map(TimeVarying::get);
    }
}
