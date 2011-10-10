package org.gridkit.search.gemfire.benchmark.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gridkit.search.gemfire.GemfireIndexSearcher;
import org.gridkit.search.gemfire.benchmark.model.Commitment;

import com.gemstone.gemfire.cache.Region;
import com.google.common.base.Stopwatch;

public class LuceneLineDistributionTask extends LineDistributionTask {
    private boolean departmentFirst;
    private GemfireIndexSearcher indexSearcher;
    private Region<String, Commitment> commitmentRegion;
    
    private Stopwatch keySearch = new Stopwatch();
    private Stopwatch hashGet = new Stopwatch();
    
    public LuceneLineDistributionTask(boolean departmentFirst,
                                      GemfireIndexSearcher indexSearcher,
                                      Region<String, Commitment> commitmentRegion) {
        this.departmentFirst = departmentFirst;
        this.indexSearcher = indexSearcher;
        this.commitmentRegion = commitmentRegion;
    }
    
    @Override
    protected Collection<Commitment> getCommitments(String dep, String line) throws Exception {
        Query dq = new TermQuery(new Term("responsibleDepartment", dep));
        Query lq = new TermQuery(new Term("budgetLine", line));
        BooleanQuery fq = new BooleanQuery();
        
        if (departmentFirst) {
            fq.add(dq, BooleanClause.Occur.MUST);
            fq.add(lq, BooleanClause.Occur.MUST);
        } else {
            fq.add(lq, BooleanClause.Occur.MUST);
            fq.add(dq, BooleanClause.Occur.MUST);    
        }
        
        keySearch.start();
        List<Object> keys = indexSearcher.search(commitmentRegion.getFullPath(), fq);
        keySearch.stop();
        
        hashGet.start();
        Map<String, Commitment> result = commitmentRegion.getAll(keys);
        hashGet.stop();
        
        return result.values();
    }
    
    @Override
    public void record() {
        super.record();
        
        getStatistics("keySearch", count).addValue(keySearch.elapsedTime(TimeUnit.MICROSECONDS));
        getStatistics("hashGet", count).addValue(hashGet.elapsedTime(TimeUnit.MICROSECONDS));

        keySearch.reset();
        hashGet.reset();
    }
}
