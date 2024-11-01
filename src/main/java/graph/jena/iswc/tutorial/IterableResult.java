package graph.jena.iswc.tutorial;

import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class IterableResult implements Iterable<Binding> {

    private final Iterable<Binding> iterator;

    public IterableResult(QueryIterator exec) {
        this.iterator = (Iterable<Binding>) exec;
    }

    @Override
    public Iterator<Binding> iterator() {
        return iterator.iterator();
    }

    @Override
    public void forEach(Consumer<? super Binding> action) {
        iterator.forEach(action);
    }

    @Override
    public Spliterator<Binding> spliterator() {
        return iterator.spliterator();
    }
}
