package org.gridkit.search.gemfire;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.distributed.DistributedMember;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LuceneKeySetQueryTask implements Function {

	private static final long serialVersionUID = 20110901L;

	@SuppressWarnings("unchecked")
	static List<Object> execute(DistributedMember member, String regionName, Query query) {
		Object[] args = {regionName, query};
		ResultCollector<?, ?> collector = FunctionService.onMember(CacheFactory.getAnyInstance().getDistributedSystem(), member)
			.withArgs(args).execute(new LuceneKeySetQueryTask());
		List<Object> result = (List<Object>) collector.getResult();
		return result;
	}
	
	LuceneKeySetQueryTask() {		
	}
	
	@Override
	public void execute(FunctionContext context) {
		Object[] params = (Object[]) context.getArguments();
		String regionName = (String) params[0];
		Query query = (Query) params[1];
		
		final IndexSearcher searcher = LuceneIndexManager.getInstance().getSearcherForRegion(regionName);
		
		if (searcher == null) {
			context.getResultSender().lastResult(IndexError.NO_INDEX);
		}
		else {
			final ResultSender<String> sender = context.getResultSender();
			try {
				searcher.search(query, new Collector() {

					@Override
					public void setScorer(Scorer scorer) throws IOException {
					}
					
					@Override
					public void setNextReader(IndexReader reader, int docBase)	throws IOException {
					}
					
					@Override
					public void collect(int docId) throws IOException {
						Document doc = searcher.doc(docId, DocKeyFieldSelector.INSTANCE);
						String key = doc.get(LuceneIndexManager.DOCUMENT_KEY);
						sender.sendResult(key);
					}
					
					@Override
					public boolean acceptsDocsOutOfOrder() {
						return true;
					}
				});
				sender.lastResult(null);
			} catch (IOException e) {
				sender.sendException(e);
			}
		}		
	}

	@Override
	public boolean hasResult() {
		return true;
	}
	
	@Override
	public String getId() {
		return this.getClass().getName();
	}

	@Override
	public boolean optimizeForWrite() {
		return false;
	}

	@Override
	public boolean isHA() {
		return false;
	}
}
