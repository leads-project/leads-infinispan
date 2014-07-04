package org.infinispan.versioning.impl;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicObjectFactory;
import org.infinispan.versioning.utils.version.Version;
import org.infinispan.versioning.utils.version.VersionGenerator;
import org.jboss.logging.Logger;

import java.util.*;

/**
 *
 * @author Pierre Sutra
 * @since 7.0
 */
public class VersionedCacheHashMapImpl<K,V> extends VersionedCacheAbstractImpl<K,V> {

    AtomicObjectFactory factory;
    Logger logger;
    Class mapClass;

    public VersionedCacheHashMapImpl(Cache delegate, VersionGenerator generator, String name) {
        super(delegate,generator,name);
        factory = new AtomicObjectFactory((Cache<Object, Object>) delegate);
        this.logger  = Logger.getLogger(this.getClass());

    }

    @Override
    protected SortedMap<Version, V> versionMapGet(K key) {
        HashMap<Version,V> map = factory.getInstanceOf(HashMap.class,key,true,null,false);
        return new TreeMap<Version,V>(map);
    }

    @Override
    protected void versionMapPut(K key, V value, Version version) {
        HashMap hashMap = factory.getInstanceOf(HashMap.class, key, true, null, false);
        hashMap.put(version, value);
        factory.disposeInstanceOf(HashMap.class,key,true);
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
        for(Object k: delegate.keySet()){
            if(factory.getInstanceOf(HashMap.class,k,true,null,false).containsValue(o))
                return true;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Version> get(K key, Version first, Version last) {
        HashMap<Version,V> map = factory.getInstanceOf(HashMap.class,key,true,null,false);
        TreeMap<Version,V> treeMap = new TreeMap<Version,V>(map);
        return treeMap.subMap(first, last).keySet();
    }

    @Override
    public void putAll(K key, Map<Version,V> map){
        HashMap hashMap = factory.getInstanceOf(HashMap.class, key, true, null, false);
        hashMap.putAll(map);
        factory.disposeInstanceOf(HashMap.class,key,true);
    }

}

