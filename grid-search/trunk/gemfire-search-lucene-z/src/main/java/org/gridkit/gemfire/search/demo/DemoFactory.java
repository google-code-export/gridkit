package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.internal.cache.PartitionAttributesImpl;
import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.spi.InternalCompass;

import java.util.HashMap;
import java.util.Map;

public class DemoFactory {
    public static final int locatorPort = 5555;
    public static final String locatorHost = "127.0.0.1";

    public static final String authorRegionName = "authorRegion";

    public static Cache createServerCache(){
        return createServerCache("");
    }

    public static Cache createServerCache(String roles) {
        CacheFactory cacheFactory = new CacheFactory();

        cacheFactory.set("locators", String.format("%s[%d]", locatorHost, locatorPort))
					.set("bind-address", locatorHost)
		            .set("mcast-port", "0")
                    .set("roles", roles);

        return cacheFactory.create();
    }

    public static ClientCache createClientCache() {
        ClientCacheFactory clientCacheFactory = new ClientCacheFactory();

        clientCacheFactory.addPoolLocator(locatorHost, locatorPort);
        clientCacheFactory.setPoolSubscriptionEnabled(true);

        return clientCacheFactory.create();
    }

    public static <K, V> Region<K, V> createServerRegion(Cache cache, String name, boolean storeLocalData) {
        RegionFactory regionFactory = cache.<K, V>createRegionFactory(RegionShortcut.PARTITION);

        if (!storeLocalData) {
            PartitionAttributesImpl partitionAttributes = new PartitionAttributesImpl();
            partitionAttributes.setLocalMaxMemory(0);
            regionFactory.setPartitionAttributes(partitionAttributes);
        }

        return regionFactory.create(name);
    }

    public static <K, V> Region<K, V> createClientRegion(ClientCache cache, String name) {
        return cache.<K, V>createClientRegionFactory(ClientRegionShortcut.PROXY).create(name);
    }

    public static InternalCompass createCompass() {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");

        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.CORE_POOL_SIZE, "1");
        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.MAXIMUM_POOL_SIZE, "1");
        configuration.setSetting(CompassEnvironment.ExecutorManager.Concurrent.SCHEDULED_CORE_POOL_SIZE, "1");

        //configuration.setSetting(CompassEnvironment.Mapping.GLOBAL_STORE, Property.Store.NO.toString());
        configuration.setSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, "false");

        return (InternalCompass)configuration.buildCompass();
    }
}
