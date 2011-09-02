package org.gridkit.search.gemfire;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

public class LuceneQueryExecutor implements Function {

	@Override
	public void execute(FunctionContext context) {
		System.out.println("QUERY: " + context.getArguments());		
	}

	@Override
	public String getId() {
		return "LuceneSearch";
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return false;
	}

	@Override
	public boolean optimizeForWrite() {
		return false;
	}
}
