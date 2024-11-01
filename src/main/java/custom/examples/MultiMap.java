package custom.examples;

import java.util.*;

/*
 * This class is our 'R' data type, which is algebraically a monoid:
 *  - The identity element is the Empty Basket
 *  - The associative binary operation is the union of two fruit baskets
 */
public class MultiMap<K, W> implements Iterable<Map<K, W>> {

    private List<Map<K, W>> maps = new ArrayList<>();

    public void addMap(Map<K, W> f) {
        this.maps.add(f);
    }

    public void addAll(MultiMap<K, W> basket) {
        basket.forEach(f -> maps.add(f));
    }

    public int getSize() {
        return maps.size();
    }

    @Override
    public Iterator<Map<K, W>> iterator() {
        return maps.iterator();
    }
}
