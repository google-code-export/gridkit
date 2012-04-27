package org.gridkit.coherence.misc.bulletproof;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.ParallelAwareAggregator;

public class HardenedAggregator implements ParallelAwareAggregator, PortableObject {

	private EntryAggregator nestedAggregator;
	
	@Override
	public Object aggregate(Set paramSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntryAggregator getParallelAggregator() {
		return this;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object aggregateResults(Collection parResults) {
		return null;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeExternal(PofWriter out) throws IOException {
		// TODO Auto-generated method stub
		
	}	
	
	public static class PartialResult implements PortableObject {
		
		private int[] canaryKeys;
		private int keyCount;		
		
	}
	
	public static class PartialAggregator implements PortableObject {
		
		private EntryAggregator delegate;
		
	}
}
