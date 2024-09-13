package graph.seraph.events;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class PGraphOrResult implements Iterable<Result> {

    private PGraph content;
    private List<Result> result;

    public PGraphOrResult(PGraph content) {
        this.content = content;
        this.result = new ArrayList<>();
    }

    public PGraphOrResult() {
        this.result = new ArrayList<>();
        this.content = PGraphImpl.createEmpty();
    }

    public PGraphOrResult(PGraphOrResult r1, PGraphOrResult r2) {
        this(PGraphOrResult.merge(r1.getContent(), r2.getContent()));
        this.result.addAll(r1.getResult());
        this.result.addAll(r2.getResult());
    }


    @Override
    public Iterator<Result> iterator() {
        return result.iterator();
    }

    @Override
    public void forEach(Consumer<? super Result> action) {
        result.forEach(action);
    }


    public PGraph getContent() {
        return this.content;
    }

    public List<Result> getResult() {
        return this.result;
    }

    public void setContent(PGraph content) {
        this.content = content;
    }

    public void setResult(List<Result> res) {
        this.result = res;
    }

    public static PGraph merge(PGraph g1, PGraph g2) {
        PGraphImpl.NodeImpl[] ns = (PGraphImpl.NodeImpl[]) ArrayUtils.addAll(g1.nodes(), g2.nodes());
        PGraphImpl.EdgeImpl[] es = (PGraphImpl.EdgeImpl[]) ArrayUtils.addAll(g1.edges(), g2.edges());
        return new PGraphImpl(ns, es);
    }
}