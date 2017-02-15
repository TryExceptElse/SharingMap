package com.github.io.tryexceptelse.data;

import java.util.*;


/**
 * Map that minimizes memory used between many, substantially similar
 * copies, at the expense of increased operation time.
 * @param <K>
 * @param <V>
 */
public class SharingNavMap
        <K extends Comparable<K>, V>
        implements NavigableMap<K, V>{
    private static final double MAX_CHANGE_RATIO = 0.1;

    private BaseMap<K, V> baseMap;
    private ChangeMap<K, V> changeMap;
    private Set<K> removedKeys;
    private K lowerBound;
    private boolean lowerBoundInclusive;
    private K upperBound;
    private boolean upperBoundInclusive;

    /**
     * Default constructor
     */
    public SharingNavMap(){
        baseMap = new BaseMap<>();
        changeMap = null;
        removedKeys = new HashSet<>();
        lowerBound = null;
        upperBound = null;
    }

    /**
     * Constructor taking a Navigable map to copy as arg
     */
    public SharingNavMap(NavigableMap<K, V> other){
        baseMap = new BaseMap<>(other);
        changeMap = null;
        removedKeys = new HashSet<>();
        lowerBound = null;
        upperBound = null;
    }

    /**
     * Constructor taking another SharingNavMap to copy.
     * The base map is retained, and only the change map is
     * copied or applied to make a new map depending on size
     */
    public SharingNavMap(SharingNavMap<K, V> other){
        baseMap = other.baseMap;
        baseMap.refCount ++;
        changeMap = (other.changeMap != null)?
                new ChangeMap<>(other.changeMap) : null;
        removedKeys = new HashSet<>();
        lowerBound = null;
        upperBound = null;
    }

    /**
     * Returns map that is the product of applying changes to base map.
     * @return NavigableMap[K, V]
     */
    private NavigableMap<K, V> effectiveMap(){
        NavigableMap<K, V> effectiveMap = new TreeMap<>(
                // make new TreeMap that is a copy of view
                // there may be a much better way of doing this.
                baseMap.subMap(
                        lowerBound,
                        lowerBoundInclusive,
                        upperBound,
                        upperBoundInclusive
                )
        );
        changeMap.subMap(
                lowerBound,
                lowerBoundInclusive,
                upperBound,
                upperBoundInclusive
        ).forEach(effectiveMap::put);
        removedKeys.forEach(effectiveMap::remove);
        return effectiveMap;
    }

    /**
     * Returns map that is currently being edited.
     * This will be the base map if refCount == 1, otherwise
     * the changeMap
     * @return NavigableMap[K, V]
     */
    private NavigableMap<K, V> activeMap(){
        // if multiple maps reference base map, it must not be changed
        if (baseMap.refCount > 1){
            // check that a change map exists, if not, create it
            if (changeMap == null){
                changeMap = new ChangeMap<>();
                removedKeys = new HashSet<>();
            }
            return changeMap;
        } else {
            // otherwise, if this is the only map instance using the
            // base map...
            if (changeMap != null){
                // flatten things to a single map
                flatten();
            }
            return baseMap;
        }
    }

    /**
     * Flattens changeMap into baseMap.
     * Used when ref count of base map drops to 1.
     */
    private void flatten(){
        assert baseMap.refCount == 1;
        // clears all entries with key lower than lowerBound
        baseMap.headMap(lowerBound).clear();
        // clears all entries with key higher than upperBound
        baseMap.tailMap(upperBound).clear();
        changeMap.forEach(baseMap::put);
        removedKeys.forEach(baseMap::remove);
        upperBound = lowerBound = null;
        changeMap = null;
        removedKeys = null;
    }

    /**
     * Makes map data unique, removing reference to current baseMap
     * and creating a new one from base map and change map.
     * Used when change ratio exceeds set constant.
     */
    private void makeUniqueBase(){
        baseMap = new BaseMap<>(baseMap); // copy base map
        flatten(); // apply all changes
    }

    private double changeRatio(){
        return (changeMap != null)?
                (double)(changeMap.size() + removedKeys.size()) /
                        baseMap.size()
                :
                0;
    }

    /**
     * Returns greatest entry that is lower than passed key k
     * @param k: K
     * @return Entry[K, V]
     */
    @Override
    public Entry<K, V> lowerEntry(K k) {
        // unoptimized
        return effectiveMap().lowerEntry(k);
    }

    @Override
    public K lowerKey(K k) {
        // unoptimized
        return effectiveMap().lowerKey(k);
    }

    @Override
    public Entry<K, V> floorEntry(K k) {
        // unoptimized
        return effectiveMap().floorEntry(k);
    }

    @Override
    public K floorKey(K k) {
        // unoptimized
        return effectiveMap().floorKey(k);
    }

    @Override
    public Entry<K, V> ceilingEntry(K k) {
        // unoptimized
        return effectiveMap().ceilingEntry(k);
    }

    @Override
    public K ceilingKey(K k) {
        // unoptimized
        return effectiveMap().ceilingKey(k);
    }

    @Override
    public Entry<K, V> higherEntry(K k) {
        // unoptimized
        return effectiveMap().higherEntry(k);
    }

    @Override
    public K higherKey(K k) {
        // unoptimized
        return effectiveMap().higherKey(k);
    }

    @Override
    public Entry<K, V> firstEntry() {
        // unoptimized
        return effectiveMap().firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        // unoptimized
        return effectiveMap().lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        // unoptimized
        Entry<K, V> firstEntry = effectiveMap().pollFirstEntry();
        remove(firstEntry.getKey());
        return firstEntry;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        // unoptimized
        Entry<K, V> firstEntry = effectiveMap().pollFirstEntry();
        remove(lastEntry().getKey());
        return firstEntry;
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        // unoptimized
        return effectiveMap().descendingMap();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        // unoptimized
        return effectiveMap().navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        // unoptimized
        return effectiveMap().descendingKeySet();
    }

    @Override
    public NavigableMap<K, V> subMap(
            K lowerBound,
            boolean lowerBoundInclusive,
            K upperBound,
            boolean upperBoundInclusive
    ) {
        // unoptimized
        SharingNavMap<K, V> subMap = new SharingNavMap<>();
        subMap.baseMap = baseMap;
        subMap.changeMap = changeMap;
        subMap.removedKeys = removedKeys;
        subMap.lowerBound = lowerBound;
        subMap.lowerBoundInclusive = lowerBoundInclusive;
        subMap.upperBound = upperBound;
        subMap.upperBoundInclusive = upperBoundInclusive;
        return subMap;
    }

    @Override
    public NavigableMap<K, V> headMap(K k, boolean b) {
        // unoptimized
        return effectiveMap().headMap(k, b);
    }

    @Override
    public NavigableMap<K, V> tailMap(K k, boolean b) {
        // unoptimized
        return effectiveMap().tailMap(k, b);
    }

    @Override
    public SortedMap<K, V> subMap(K k, K k1) {
        // unoptimized
        return effectiveMap().subMap(k, k1);
    }

    @Override
    public SortedMap<K, V> headMap(K k) {
        // unoptimized
        return effectiveMap().headMap(k);
    }

    @Override
    public SortedMap<K, V> tailMap(K k) {
        // unoptimized
        return effectiveMap().tailMap(k);
    }

    @Override
    public Comparator<? super K> comparator() {
        // this should be common to all maps
        return baseMap.comparator();
    }

    @Override
    public K firstKey() {
        // unoptimized
        return effectiveMap().firstKey();
    }

    @Override
    public K lastKey() {
        // unoptimized
        return effectiveMap().lastKey();
    }

    @Override
    public Set<K> keySet() {
        // unoptimized
        return effectiveMap().keySet();
    }

    @Override
    public Collection<V> values() {
        // unoptimized
        return effectiveMap().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // unoptimized
        return effectiveMap().entrySet();
    }

    @Override
    public int size() {
        // unoptimized
        return effectiveMap().size();
    }

    @Override
    public boolean isEmpty() {
        // unoptimized
        return effectiveMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return !removedKeys.contains(o) &&
                (baseMap.containsKey(o) || changeMap.containsKey(o));
    }

    @Override
    public boolean containsValue(Object o) {
        return effectiveMap().containsValue(o);
    }

    @Override
    public V get(Object o) {
        return effectiveMap().get(o);
    }

    @Override
    public V put(K k, V v) {
        V existingVal;
        if (removedKeys != null && removedKeys.contains(k)){
            existingVal = null;
            removedKeys.remove(k);
        }else if (changeMap != null && changeMap.containsKey(k)){
            existingVal = changeMap.get(k);
        } else if ((lowerBound != null && k.compareTo(lowerBound) < 0) ||
                (upperBound != null && k.compareTo(upperBound) > 0)) {
            return null;
        } else {
            existingVal = baseMap.get(k);
        }
        activeMap().put(k, v);
        if (changeRatio() > MAX_CHANGE_RATIO){
            makeUniqueBase();
        }
        return existingVal;
    }

    @Override
    public V remove(Object o) {
        K k = (K) o;
        // remove is expected to return previous value stored with key
        V removed = activeMap().remove(o);
        if (removedKeys != null){
            // add key to removed keys if appropriate
            removedKeys.add(k);
        }
        // if a non-null is returned from change map remove, return
        // that. Otherwise try to get the value stored in baseMap,
        // since this is the value being replaced, as far as this map
        // is concerned.
        if (removed != null){
            return removed;
        } else if ((lowerBound != null && k.compareTo(lowerBound) >= 0) &&
                (upperBound != null && k.compareTo(upperBound) <= 0)){
            return baseMap.get(k);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        baseMap = new BaseMap<>();
        changeMap = null;
        removedKeys = null;
        lowerBound = upperBound = null;
    }

    @Override
    public String toString() {
        return "SharingNavMap{" +
                "baseMap=" + baseMap + System.identityHashCode(baseMap) +
                ", changeMap=" + changeMap + System.identityHashCode(changeMap) +
                ", removedKeys=" + removedKeys +
                ", lowerBound=" + lowerBound +
                ", lowerBoundInclusive=" + lowerBoundInclusive +
                ", upperBound=" + upperBound +
                ", upperBoundInclusive=" + upperBoundInclusive +
                '}';
    }

    /**
     * Essentially a tree map that also stores a reference counter.
     */
    private class BaseMap<K2, V2> extends TreeMap<K2, V2>{
        private int refCount;

        /**
         * Default Constructor
         */
        BaseMap(){
            super();
            refCount = 1;
        }

        /**
         * Constructor taking another nav map to copy
         */
        BaseMap(NavigableMap<K2, V2> other){
            super(other);
            refCount = 1;
        }
    }

    /**
     * Map holding changes made to map as compared to base.
     * Nothing special is done here yet, just future-proofing things
     */
    private class ChangeMap<K2, V2> extends TreeMap<K2, V2>{
        /**
         * Default constructor
         */
        private ChangeMap(){
            super();
        }

        /**
         * Copy Constructor
         */
        private ChangeMap(ChangeMap<K2, V2> other){
            super(other);
        }
    }
}