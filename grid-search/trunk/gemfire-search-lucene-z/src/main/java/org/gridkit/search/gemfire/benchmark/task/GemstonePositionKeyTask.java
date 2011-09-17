package org.gridkit.search.gemfire.benchmark.task;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.Query;
import org.gridkit.search.gemfire.benchmark.model.Commitment;

public class GemstonePositionKeyTask extends PositionKeyTask {
    private Query query;
    private Region<String, Commitment> commitmentRegion;

    public GemstonePositionKeyTask(Region<String, Commitment> commitmentRegion) {
        String queryStr = String.format(
            "SELECT DISTINCT * FROM %s WHERE positionKey = $1", commitmentRegion.getFullPath()
        );

        this.query = commitmentRegion.getRegionService().getQueryService().newQuery(queryStr);
        this.commitmentRegion = commitmentRegion;
    }

    @Override
    protected Commitment getCommitment(String positionKey) throws Exception {
        //SelectResults result = (SelectResults)query.execute(new Object[] {positionKey});
        //return (Commitment)result.iterator().next();

        return commitmentRegion.get(positionKey);
    }
}
