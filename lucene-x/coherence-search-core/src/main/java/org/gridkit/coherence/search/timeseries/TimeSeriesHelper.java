package org.gridkit.coherence.search.timeseries;

import java.util.Collections;
import java.util.Comparator;

import com.tangosol.util.Filter;
import com.tangosol.util.QueryMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.KeyAssociatedFilter;

public class TimeSeriesHelper<K, V, T> {

	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor affinityExtractor;;
	private ValueExtractor timestampExtractor;
	private Comparator<?> timestampComparator;
	
	public TimeSeriesHelper(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor) {
		this(seriesKeyExtractor, timestampExtractor, null);
	}

	public TimeSeriesHelper(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor, Comparator<?> timestampComparator) {
		this.seriesKeyExtractor = seriesKeyExtractor;
		this.affinityExtractor = null;
		this.timestampExtractor = timestampExtractor;
		this.timestampComparator = timestampComparator;
	}
	
	public ValueExtractor getAffinityExtractor() {
		return affinityExtractor;
	}

	public void setAffinityExtractor(ValueExtractor affinityExtractor) {
		this.affinityExtractor = affinityExtractor;
	}

	public ValueExtractor getSeriesKeyExtractor() {
		return seriesKeyExtractor;
	}

	public ValueExtractor getTimestampExtractor() {
		return timestampExtractor;
	}

	public Comparator<?> getTimestampComparator() {
		return timestampComparator;
	}

	public Filter floor(K key, T ts) {
		Object akey = affinityExtractor == null ? key : affinityExtractor.extract(key);
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setSeriesSet(Collections.singleton((Object)key));
		KeyAssociatedFilter kf = new KeyAssociatedFilter(f, akey);
		return kf;
	}

	public Filter floor(T ts) {
		Filter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		return f;
	}

	public Filter ceiling(T ts) {
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setAbove(true);
		return f;
	}

	public Filter ceiling(K key, T ts) {
		Object akey = affinityExtractor == null ? key : affinityExtractor.extract(key);
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setSeriesSet(Collections.singleton((Object)key));
		f.setAbove(true);
		KeyAssociatedFilter kf = new KeyAssociatedFilter(f, akey);
		return kf;
	}	
	
	public void createIndex(QueryMap cache) {
		cache.addIndex(new TimeSeriesExtractor(seriesKeyExtractor, timestampExtractor, timestampComparator), false, null);
	}

	public void destroyIndex(QueryMap cache) {
		cache.removeIndex(new TimeSeriesExtractor(seriesKeyExtractor, timestampExtractor, timestampComparator));
	}
}
