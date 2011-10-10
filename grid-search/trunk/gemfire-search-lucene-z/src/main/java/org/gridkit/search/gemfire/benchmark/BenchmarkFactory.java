package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.internal.cache.PartitionAttributesImpl;
import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.spi.InternalCompass;

public class BenchmarkFactory {
    public static final String commitmentRegionName = "commitment";

    private Configuration config;

    public BenchmarkFactory(Configuration config) {
        this.config = config;
    }

    public Cache createCache(String roles) {
        CacheFactory cacheFactory = new CacheFactory();

        cacheFactory.set("locators", String.format("%s[%d]", config.locatorHost, config.locatorPort))
					.set("bind-address", config.bindAddress)
		            .set("mcast-port", "0")
                    .set("disable-tcp", "true")
                    .set("roles", roles);

        return cacheFactory.create();
    }

    public Cache createCache(){
        return createCache("");
    }

    public <K, V> Region<K, V> createRegion(Cache cache, String name, boolean storeLocalData) {
        RegionFactory regionFactory = cache.<K, V>createRegionFactory(RegionShortcut.PARTITION);

        if (!storeLocalData) {
            PartitionAttributesImpl partitionAttributes = new PartitionAttributesImpl();
            partitionAttributes.setLocalMaxMemory(0);
            regionFactory.setPartitionAttributes(partitionAttributes);
        }

        return regionFactory.create(name);
    }

    public InternalCompass createCompass() {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");

        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.CORE_POOL_SIZE, "1");
        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.MAXIMUM_POOL_SIZE, "1");
        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.SCHEDULED_CORE_POOL_SIZE, "1");

        configuration.setSetting(CompassEnvironment.Mapping.GLOBAL_STORE, Property.Store.NO.toString());
        configuration.setSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, "false");

        return (InternalCompass)configuration.buildCompass();
    }
}
