package org.infinispan.ensemble.cache.distributed;

import org.infinispan.commons.CacheException;
import org.infinispan.ensemble.cache.EnsembleCache;

import java.util.List;

/**
 * @author Pierre Sutra
 * @since 7.0
 */
public class DistributedEnsembleCache<K,V> extends EnsembleCache<K,V> {

    private Partitioner<K,V> partitioner;
    private boolean frontierMode;
    private EnsembleCache<K,V> frontierCache;

    public DistributedEnsembleCache(String name, List<? extends EnsembleCache<K, V>> caches, Partitioner<K, V> partitioner){
        this(name,caches,partitioner,false);
    }

    public DistributedEnsembleCache(String name, List<? extends EnsembleCache<K, V>> caches, Partitioner<K, V> partitioner, boolean fm){
        this(name,caches,caches.get(0),partitioner,fm);
    }

    /**
     *
     * Create a DistributedEnsembleCache.
     * If the cache operates in frontier mode, the frontier cache is defined as local SiteEnsembleCache.
     * In case no such cache exists, the frontier cache is the last cache in the list.
     *
     *
     * @param name
     * @param caches
     * @param cache
     * @param partitioner
     * @param fm
     */
    public DistributedEnsembleCache(String name, List<? extends EnsembleCache<K, V>> caches, EnsembleCache<K,V> cache, Partitioner<K, V> partitioner, boolean fm){
        super(name,caches);
        assert fm | (cache!=null);
        this.partitioner = partitioner;
        this.frontierMode = fm;

        if (frontierMode) {
            for (EnsembleCache<K, V> ensembleCache : caches)
                if (ensembleCache.isLocal())
                    frontierCache = ensembleCache;
            if (frontierCache == null)
                throw new CacheException("Invalid parameters");
        }

    }

    public EnsembleCache<K,V> getFrontierCache(){
        return frontierCache;
    }

    public boolean isFrontierMode(){
        return frontierMode;
    }

    @Override
    public int size() {
        int ret=0;
        for (EnsembleCache ensembleCache : caches){
            ret+=ensembleCache.size();
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        for (EnsembleCache ensembleCache : caches)
            if (!ensembleCache.isEmpty())
                return false;
        return true;
    }

    @Override
    public V get(Object key) {
        return frontierMode ? frontierCache.get(key) : partitioner.locate((K)key).get(key);
    }

    @Override
    public V put(K key, V value) {
        return partitioner.locate(key).put(key,value);
    }

    @Override
    public boolean containsKey(Object o) {
        for (EnsembleCache cache : caches){
            if (frontierMode) {
                if (cache.equals(frontierCache) && cache.containsKey(o)) {
                    return true;
                }
            } else if (cache.containsKey(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        for (EnsembleCache cache: caches)
            cache.clear();
    }

    @Override
    public V remove(Object o) {
        return partitioner.locate((K) o).remove(o);
    }

}
