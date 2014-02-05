package org.apache.versioning;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;
import org.infinispan.container.versioning.IncrementableEntryVersion;
import org.infinispan.container.versioning.VersionGenerator;
import org.infinispan.query.SearchManager;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Pierre Sutra
 * @since 6.0
 */
public class VersionedCacheHibernateImpl<K,V> implements VersionedCache<K,V> {

    private VersionGenerator generator;
    private Cache delegate;
    private String name;
    private SearchManager searchManager;
    private QueryFactory qf;

    public VersionedCacheHibernateImpl(Cache delegate, VersionGenerator generator, String name) {
        this.delegate = delegate;
        // TODO check that the delegate is correct
        this.generator = generator;
        this.name = name;
        searchManager = org.infinispan.query.Search.getSearchManager(delegate);
        qf = searchManager.getQueryFactory();
    }

    @Override
    public void put(K key, V value, IncrementableEntryVersion version) {
        HibernateProxy<K,V> proxy = new HibernateProxy<K, V>(key,value,version);
        delegate.put(proxy.getId(),proxy);
    }

    @Override
    public Collection<V> get(K key, IncrementableEntryVersion first, IncrementableEntryVersion last) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.subMap(first, last).values();

    }

    @Override
    public V get(K key, IncrementableEntryVersion version) {
        TreeMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        return map.pollLastEntry().getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public V put(K key, V value) {
        TreeMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        IncrementableEntryVersion lversion;
        V lval;

        if(map.isEmpty()){
            lversion = generator.generateNew();
            lval = null;
        }else{
            lversion =  map.lastKey();
            lval = map.get(lversion);
        }
        IncrementableEntryVersion entryVersion = generator.increment(lversion);
        put(key,value,entryVersion);
        return lval;
    }

    @Override
    public V put(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public V putIfAbsent(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit unit) {
        // TODO: Customise this generated block
    }

    @Override
    public V replace(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public boolean replace(K key, V oldValue, V value, long lifespan, TimeUnit unit) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        // TODO: Customise this generated block
    }

    @Override
    public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public boolean replace(K key, V oldValue, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public V remove(Object key) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        // TODO: Customise this generated block
    }

    @Override
    public void clear() {
        // TODO: Customise this generated block
    }

    @Override
    public Set<K> keySet() {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public Collection<V> values() {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public boolean containsKey(Object o) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public boolean containsValue(Object o) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public V get(Object k) {
        TreeMap m = getVersionMap((K)k);
        if(m.isEmpty())
            return null;
        return (V) m.get(m.lastKey());
    }

    @Override
    public V getLatest(K key, IncrementableEntryVersion upperBound) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.get(map.headMap(upperBound).lastKey());
    }

    @Override
    public V getEarliest(K key, IncrementableEntryVersion lowerBound) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.get(map.firstKey());
    }

    @Override
    public IncrementableEntryVersion getLatestVersion(K key) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.lastKey();
    }

    @Override
    public IncrementableEntryVersion getLatestVersion(K key, IncrementableEntryVersion upperBound) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.tailMap(upperBound).firstKey();
    }

    @Override
    public IncrementableEntryVersion getEarliestVersion(K key) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.firstKey();
    }

    @Override
    public IncrementableEntryVersion getEarliestVersion(K key, IncrementableEntryVersion lowerBound) {
        SortedMap<IncrementableEntryVersion,V> map = getVersionMap(key);
        if(map.isEmpty())
            return null;
        return map.firstKey();
    }

    @Override
    public NotifyingFuture<V> putAsync(K key, V value) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> putAsync(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Void> putAllAsync(Map<? extends K, ? extends V> data) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Void> clearAsync() {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> putIfAbsentAsync(K key, V value) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> removeAsync(Object key) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Boolean> removeAsync(Object key, Object value) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> replaceAsync(K key, V value) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit unit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public NotifyingFuture<V> getAsync(K key) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public V putIfAbsent(K k, V v) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public boolean remove(Object o, Object o2) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public boolean replace(K k, V v, V v2) {
        return false;  // TODO: Customise this generated block
    }

    @Override
    public V replace(K k, V v) {
        return null;  // TODO: Customise this generated block
    }

    @Override
    public void start() {
        // TODO: Customise this generated block
    }

    @Override
    public void stop() {
        // TODO: Customise this generated block
    }

    //
    // INNER METHODS
    //

    private TreeMap<IncrementableEntryVersion,V> getVersionMap(K key){
        Query query = (Query) qf.from(HibernateProxy.class)
                .having("k").eq(key.toString())
                .toBuilder()
                .build();
        List<HibernateProxy<K,V>> list = query.list();
        TreeMap<IncrementableEntryVersion,V> map = new TreeMap<IncrementableEntryVersion, V>(
                new EntryVersionComparator());
        for(HibernateProxy<K,V> proxy : list)
            map.put(proxy.version,proxy.v);
        return map;
    }

//    private TreeMap<IncrementableEntryVersion,V> getVersionMap(K key,
//                                                               IncrementableEntryVersion v1,
//                                                               IncrementableEntryVersion v2){
//        Query query = (Query) qf.from(HibernateProxy.class)
//                .having("k").eq(key.toString())
//                .and().having("version").gte(v1)
//                .and().having("version").lte(v2)
//                .toBuilder()
//                .build();
//        List<HibernateProxy<K,V>> list = query.list();
//        TreeMap<IncrementableEntryVersion,V> map = new TreeMap<IncrementableEntryVersion, V>(
//                new EntryVersionComparator());
//        for(HibernateProxy<K,V> proxy : list)
//            map.put(proxy.version,proxy.v);
//        return map;
//
//    }

}
