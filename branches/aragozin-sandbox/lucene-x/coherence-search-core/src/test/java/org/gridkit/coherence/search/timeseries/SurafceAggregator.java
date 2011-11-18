package org.gridkit.coherence.search.timeseries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;

import com.tangosol.util.InvocableMap;
import com.tangosol.util.aggregator.AbstractAggregator;
import com.tangosol.util.extractor.IdentityExtractor;

@Ignore
@SuppressWarnings("serial")
public class SurafceAggregator extends AbstractAggregator implements Serializable {

	private transient Map<Object, SampleValue> buffer = new HashMap<Object, SampleValue>();
	
	private long timestamp;
	
	public SurafceAggregator(long timestamp) {
		super(IdentityExtractor.INSTANCE);
		this.timestamp = timestamp;
	}

	@Override
	protected void init(boolean second) {
	}

	protected void processEntry(InvocableMap.Entry entry) {
		process(entry, false);
	}	
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void process(Object value, boolean second) {
		if (second) {
			buffer.putAll((Map<Object, SampleValue>) value);
		}
		else {
			Entry e = (Entry) value;
			SampleKey key = (SampleKey) e.getKey();
			SampleValue val = (SampleValue) e.getValue();
			
			if (val.getTimestamp() <= timestamp) {
				Object skey = key.getSerieKey();
				SampleValue other = buffer.get(skey);
				if (other == null || val.getTimestamp() > other.getTimestamp()) {
					buffer.put(skey, val);
				}
			}
		}		
	}

	@Override
	protected Object finalizeResult(boolean second) {
		if (second) {
			List<Object> result = new ArrayList<Object>();
			for(SampleValue v : buffer.values()) {
				result.add(v.getValue());
			}
			return result;
		}
		else {
			return buffer;
		}
	}
}
