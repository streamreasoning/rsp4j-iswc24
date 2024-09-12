package graph.neo4j;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PGraphOrBindings implements Iterable<Map<String, Object>> {

    private PGraph content;
    private List<Map<String, Object>> result;

    public PGraphOrBindings(PGraph content) {
        this.content = content;
        this.result = new ArrayList<>();
    }

    public PGraphOrBindings() {
        this.result = new ArrayList<>();
        this.content = PGraphImpl.createEmpty();
    }

    public PGraphOrBindings(PGraphOrBindings r1, PGraphOrBindings r2) {
        this(PGraphOrBindings.merge(r1.getContent(), r2.getContent()));
        this.result.addAll(r1.getResult());
        this.result.addAll(r2.getResult());
    }


    @Override
    public Iterator<Map<String, Object>> iterator() {
        return result.iterator();
    }

    @Override
    public void forEach(Consumer<? super Map<String, Object>> action) {
        result.forEach(action);
    }


    public PGraph getContent() {
        return this.content;
    }

    public List<Map<String, Object>> getResult() {
        return this.result;
    }

    public void setContent(PGraph content) {
        this.content = content;
    }

    public void setResult(List<Map<String, Object>> res) {
        this.result = res;
    }

    public static PGraph merge(PGraph g1, PGraph g2) {
        PGraphImpl.NodeImpl[] ns = (PGraphImpl.NodeImpl[]) ArrayUtils.addAll(g1.nodes(), g2.nodes());
        PGraphImpl.EdgeImpl[] es = (PGraphImpl.EdgeImpl[]) ArrayUtils.addAll(g1.edges(), g2.edges());
        return new PGraphImpl(ns, es);
    }
}