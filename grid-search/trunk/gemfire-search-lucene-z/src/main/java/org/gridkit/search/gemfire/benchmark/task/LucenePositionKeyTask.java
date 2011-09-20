package org.gridkit.search.gemfire.benchmark.task;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gridkit.search.gemfire.GemfireIndexSearcher;
import org.gridkit.search.gemfire.benchmark.model.Commitment;

import com.gemstone.gemfire.cache.Region;
import com.google.common.base.Stopwatch;

public class LucenePositionKeyTask extends PositionKeyTask {
    private GemfireIndexSearcher indexSearcher;
    private Region<String, Commitment> commitmentRegion;

    private Stopwatch keySearch = new Stopwatch();
    private Stopwatch hashGet = new Stopwatch();

    public LucenePositionKeyTask(GemfireIndexSearcher indexSearcher, Region<String, Commitment> commitmentRegion) {
        this.indexSearcher = indexSearcher;
        this.commitmentRegion = commitmentRegion;
    }

    @Override
    public void reset() {
        super.reset();
        statistics.put("keySearch", new DescriptiveStatistics());
        statistics.put("hashGet", new DescriptiveStatistics());
    }

    @Override
    protected Commitment getCommitment(String positionKey) {
        Query query = new TermQuery(new Term("positionKey", positionKey));

        keySearch.start();
        Object key = indexSearcher.search(commitmentRegion.getFullPath(), query).get(0);
        keySearch.stop();

        hashGet.start();
        Commitment result = commitmentRegion.get(key);
        hashGet.stop();

        return result;
    }

    @Override
    public void record() {
        statistics.get("keySearch").addValue(keySearch.elapsedTime(TimeUnit.MICROSECONDS));
        statistics.get("hashGet").addValue(hashGet.elapsedTime(TimeUnit.MICROSECONDS));

        keySearch.reset();
        hashGet.reset();
    }
}
