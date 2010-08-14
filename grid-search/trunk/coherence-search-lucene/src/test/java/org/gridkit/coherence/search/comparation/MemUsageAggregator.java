package org.gridkit.coherence.search.comparation;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Set;

import com.tangosol.util.aggregator.AbstractAggregator;

@SuppressWarnings("serial")
public class MemUsageAggregator extends AbstractAggregator implements Serializable {

	@Override
	protected void init(boolean paramBoolean) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object aggregate(Set setEntries) {
		System.gc();
		return ManagementFactory.getRuntimeMXBean().getName() + " -> " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object aggregateResults(Collection collResults) {
		StringBuilder buf = new StringBuilder();
		for(Object result: collResults) {
			if (buf.length() > 0) {
				buf.append("\n");
			}
			buf.append(result);
		}
		
		return buf.toString();
	}
	
	@Override
	protected Object finalizeResult(boolean paramBoolean) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void process(Object paramObject, boolean paramBoolean) {
		throw new UnsupportedOperationException();
	}
}
