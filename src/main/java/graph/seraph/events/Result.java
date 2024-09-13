package graph.seraph.events;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Result implements Map<String, Object> {

    private final Map<String, Object> innerMap;

    public Result(Map<String, Object> innerMap) {
        this.innerMap = innerMap;
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return innerMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return innerMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        innerMap.putAll(m);
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return innerMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return innerMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return innerMap.entrySet();
    }

    @Override
    public String toString() {
        return innerMap.toString();
    }
}
