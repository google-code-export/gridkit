package org.gridkit.search.gemfire;

public class SearchServerConfig {
    private int changesBeforeCommit = 8192;
    private String keyFieldName = "gemfire.region.key";

    public int getChangesBeforeCommit() {
        return changesBeforeCommit;
    }

    public SearchServerConfig setChangesBeforeCommit(int changesBeforeCommit) {
        this.changesBeforeCommit = changesBeforeCommit;
        return this;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public SearchServerConfig setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
        return this;
    }
}
