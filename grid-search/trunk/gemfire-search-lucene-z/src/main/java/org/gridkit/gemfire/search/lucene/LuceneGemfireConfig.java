package org.gridkit.gemfire.search.lucene;

public class LuceneGemfireConfig {
    private int changesBeforeCommit = 8192;
    private String keyFieldName = "cache.key";
    private String searchFunctionId;

    public LuceneGemfireConfig(String searchFunctionId) {
        this.searchFunctionId = searchFunctionId;
    }

    public int getChangesBeforeCommit() {
        return changesBeforeCommit;
    }

    public LuceneGemfireConfig setChangesBeforeCommit(int changesBeforeCommit) {
        this.changesBeforeCommit = changesBeforeCommit;
        return this;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public LuceneGemfireConfig setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
        return this;
    }

    public String getSearchFunctionId() {
        return searchFunctionId;
    }

    public LuceneGemfireConfig setSearchFunctionId(String searchFunctionId) {
        this.searchFunctionId = searchFunctionId;
        return this;
    }
}
