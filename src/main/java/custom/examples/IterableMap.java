package custom.examples;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

public class IterableMap implements Iterable<String> {

    Map<String, Integer> map;

    public IterableMap() {
        this.map = new HashMap<>();
    }


    public IterableMap(String[] fruit) {
        this.map = new HashMap<>();
        for (String w : fruit) {
            map.computeIfPresent(w, (key, value) -> value + 1);
            map.computeIfAbsent(w, (key) -> 1);
        }
    }

    public IterableMap(Map<String, Integer> fruit) {
        this.map = fruit;
    }

    @Override
    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Spliterator<String> spliterator() {
        return Iterable.super.spliterator();
    }
}
