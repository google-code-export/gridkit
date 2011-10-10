package org.gridkit.search.gemfire.benchmark.task;

import java.util.Collection;

import org.gridkit.search.gemfire.benchmark.model.Commitment;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.Query;

public class GemstoneLineDistributionTask extends LineDistributionTask {
    private Query query;
    
    public GemstoneLineDistributionTask(boolean departmentFirst, Region<String, Commitment> commitmentRegion) {
        String queryStr;
        
        if (departmentFirst)
            queryStr = String.format(
                "SELECT DISTINCT * FROM %s WHERE responsibleDepartment = $1 AND budgetLine = $2", commitmentRegion.getFullPath()
            );
        else
            queryStr = String.format(
                "SELECT DISTINCT * FROM %s WHERE budgetLine = $2 AND responsibleDepartment = $1", commitmentRegion.getFullPath()
            );

        this.query = commitmentRegion.getRegionService().getQueryService().newQuery(queryStr);
    }
    
    @Override
    protected Collection<Commitment> getCommitments(String dep, String line) throws Exception {
        return (Collection<Commitment>)query.execute(new Object[] {dep, line});
    }
}
