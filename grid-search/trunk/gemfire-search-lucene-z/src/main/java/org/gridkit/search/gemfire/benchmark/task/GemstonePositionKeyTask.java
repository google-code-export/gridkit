package org.gridkit.search.gemfire.benchmark.task;

import java.util.concurrent.TimeUnit;

import org.gridkit.search.gemfire.benchmark.model.Commitment;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.google.common.base.Stopwatch;

public class GemstonePositionKeyTask extends PositionKeyTask {
    private Query query;
    private Region<String, Commitment> commitmentRegion;

    private Stopwatch hashGet = new Stopwatch();
    private Stopwatch keyQuery = new Stopwatch();

    public GemstonePositionKeyTask(Region<String, Commitment> commitmentRegion) {
        String queryStr = String.format(
            "SELECT DISTINCT * FROM %s WHERE positionKey = $1", commitmentRegion.getFullPath()
        );

        this.query = commitmentRegion.getRegionService().getQueryService().newQuery(queryStr);
        this.commitmentRegion = commitmentRegion;
    }

    @Override
    protected Commitment getCommitment(String positionKey) throws Exception {
        keyQuery.start();
        SelectResults result = (SelectResults)query.execute(new Object[] {positionKey});
        Commitment c1 = (Commitment)result.iterator().next();
        keyQuery.stop();

        hashGet.start();
        Commitment c2 = commitmentRegion.get(positionKey);
        hashGet.stop();

        return c1 == null ? c2 : c1;
    }

    @Override
    public void record() {
        super.record();
        
        statistics.get("hashGet").addValue(hashGet.elapsedTime(TimeUnit.MICROSECONDS));
        statistics.get("keyQuery").addValue(keyQuery.elapsedTime(TimeUnit.MICROSECONDS));

        hashGet.reset();
        keyQuery.reset();
    }
}
