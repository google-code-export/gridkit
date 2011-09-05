package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.internal.cache.PartitionAttributesImpl;

public class DemoFactory {
    public static final String authorRegionName = "author-region";
    public static final String authorHubName = "author-hub";
    public static final String authorGatewayName = "author-gateway";
    public static final String authorFunctionId = "author-function";

    public static <K, V> Region<K, V> createPartitionedRegion(Cache cache, String name, String hub,
                                                              boolean storeLocalData) {
        RegionFactory regionFactory = cache.<K, V>createRegionFactory(RegionShortcut.PARTITION);

        regionFactory.setEnableGateway(true);
        regionFactory.setGatewayHubId(hub);

        if (!storeLocalData) {
            PartitionAttributesImpl partitionAttributes = new PartitionAttributesImpl();
            partitionAttributes.setLocalMaxMemory(0);
            regionFactory.setPartitionAttributes(partitionAttributes);
        }

        return regionFactory.create(name);
    }

    public static Cache createCache() {
        System.setProperty("java.net.preferIPv4Stack", "true");

        CacheFactory cacheFactory = new CacheFactory();

        cacheFactory.set("name", "author-store-node")
					.set("bind-address", "127.0.0.1")
					.set("mcast-address", "239.192.81.1")
					.set("mcast-port", "10334");

        return cacheFactory.create();
    }
}
