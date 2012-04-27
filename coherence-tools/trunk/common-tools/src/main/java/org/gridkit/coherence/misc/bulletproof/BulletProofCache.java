package org.gridkit.coherence.misc.bulletproof;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;

public class BulletProofCache extends AbstractCacheWrapper {

	private CacheVerificationStrategy.Instance strategy;
	
	public BulletProofCache(NamedCache cache, CacheVerificationStrategy strategy) {
		super(cache);
		this.strategy = strategy.bind(cache);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set keySet(Filter filter) {
		Filter cf = strategy.includingCanery(filter);
		Set keys = super.keySet(cf);
		MaskedKeySet mkes = strategy.maskKeySet(keys);
		
		return super.keySet(filter);
	}

	@Override
	public Set entrySet(Filter filter) {
		// TODO Auto-generated method stub
		return super.entrySet(filter);
	}

	@Override
	public Set entrySet(Filter filter, Comparator comparator) {
		// TODO Auto-generated method stub
		return super.entrySet(filter, comparator);
	}

	@Override
	public void addIndex(ValueExtractor attributeExtractor, boolean ordered,
			Comparator comparator) {
		// TODO Auto-generated method stub
		super.addIndex(attributeExtractor, ordered, comparator);
	}

	@Override
	public Map invokeAll(Filter filter, EntryProcessor agent) {
		// TODO Auto-generated method stub
		return super.invokeAll(filter, agent);
	}

	@Override
	public void removeIndex(ValueExtractor attributeExtractor) {
		// TODO Auto-generated method stub
		super.removeIndex(attributeExtractor);
	}

	@Override
	public Object aggregate(Filter filter, EntryAggregator aggregator) {
		// TODO Auto-generated method stub
		return super.aggregate(filter, aggregator);
	}

	@Override
	public int size() {
		return ;
	}

	@Override
	public Set keySet() {
		return super.keySet();
	}

	@Override
	public Collection values() {
		return super.values();
	}

	@Override
	public Set entrySet() {
		return super.entrySet();
	}
}
