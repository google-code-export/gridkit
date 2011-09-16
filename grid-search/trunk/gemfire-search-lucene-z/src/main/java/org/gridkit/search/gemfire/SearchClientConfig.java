package org.gridkit.search.gemfire;

public class SearchClientConfig {
    private int indexDiscoveryRetries = 3;
    private long indexDiscoveryTimeoutMs = 1024;

    public int getIndexDiscoveryRetries() {
        return indexDiscoveryRetries;
    }

    public SearchClientConfig setIndexDiscoveryRetries(int indexDiscoveryRetries) {
        this.indexDiscoveryRetries = indexDiscoveryRetries;
        return this;
    }

    public long getIndexDiscoveryTimeoutMs() {
        return indexDiscoveryTimeoutMs;
    }

    public SearchClientConfig setIndexDiscoveryTimeoutMs(long indexDiscoveryTimeoutMs) {
        this.indexDiscoveryTimeoutMs = indexDiscoveryTimeoutMs;
        return this;
    }
}
