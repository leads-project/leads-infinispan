package org.infinispan.versioning.impl;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicObjectFactory;
import org.infinispan.versioning.utils.collections.ShardedTreeMap;
import org.infinispan.versioning.utils.version.Version;
import org.infinispan.versioning.utils.version.VersionGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 *
 * This class implements a sharded tree to store the versions.
 * More precisely, the map is a tree of trees, where the
 *
 * @author Pierre Sutra
 * @since 6.0
 */
public class VersionedCacheAtomicShardedTreeMapImpl<K,V> extends VersionedCacheAbstractImpl<K,V> {

    private AtomicObjectFactory factory;

    public VersionedCacheAtomicShardedTreeMapImpl(Cache delegate, VersionGenerator generator, String name) {
        super(delegate,generator,name);
        factory = new AtomicObjectFactory((Cache<Object, Object>) delegate);
    }

    @Override
    protected SortedMap<Version, V> versionMapGet(K key) {
        return factory.getInstanceOf(ShardedTreeMap.class,key,false,null,false);
    }

    @Override
    protected void versionMapPut(K key, V value, Version version) {
        factory.getInstanceOf(ShardedTreeMap.class,key,false,null,false).put(version, value);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return delegate.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        for(Object k: keySet()){
            if(factory.getInstanceOf(ShardedTreeMap.class, k, true, null, false).containsValue(o))
                return true;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public V get(Object key){
        ShardedTreeMap<Version,V> shardedMap= factory.getInstanceOf(ShardedTreeMap.class,key,false,null,false);
        return shardedMap.get(key);
    }

    @Override
    public Collection<V> get(K key, Version first, Version last) {
        ShardedTreeMap<Version,V> shardedMap= factory.getInstanceOf(ShardedTreeMap.class,key,false,null,false);
        Collection<V> ret = shardedMap.subMap(first,last).values();
        try {
            factory.disposeInstanceOf(ShardedTreeMap.class,key,true);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }
        return ret;
    }

   @Override
    public void putAll(K key, Map<Version,V> map){
        ShardedTreeMap<Version,V> shardedMap= factory.getInstanceOf(ShardedTreeMap.class,key,false,null,false);
        shardedMap.putAll(map);
        try {
            factory.disposeInstanceOf(ShardedTreeMap.class,key,true);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }
    }

}
