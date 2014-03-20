package org.infinispan.versioning;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.versioning.impl.VersionedCacheNaiveImpl;
import org.infinispan.versioning.utils.version.VersionGenerator;
import org.testng.annotations.Test;

/**
 * @author marcelo pasin, pierre sutra
 * @since 7.0
 */
@Test(testName = "container.versioning.VersionedCacheNaiveTest", groups = "functional")
public class VersionedCacheNaiveTest extends VersionedCacheAbstractTest {

    @Override
    protected void setBuilder(ConfigurationBuilder builder) {
    }

    @Override
    protected <K, V> VersionedCache<K, V> getCache(Cache cache, VersionGenerator generator, String name) {
        return new VersionedCacheNaiveImpl<K,V>(cache,generator,name);
    }

}
