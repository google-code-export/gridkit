package org.gridkit.lab.zktest;

import java.util.List;

import org.gridkit.nimble.metering.SampleReader;
import org.gridkit.nimble.pivot.SampleFilter;

public class SievedReader implements SampleReader {

	private final SampleReader nested;	
	private final SampleFilter[] filters;

	public SievedReader(SampleReader nested, SampleFilter... filters) {
		this.nested = nested;
		this.filters = filters;
		if (nested.isReady() && !match()) {
			next();
		}
	}

	@Override
	public boolean isReady() {
		return nested.isReady();
	}

	@Override
	public boolean next() {
		while(nested.next()) {
			if (match()) {
				return true;
			}
		}
		return false;
	}

	private boolean match() {
		for(SampleFilter f: filters) {
			if (!f.match(nested)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Object> keySet() {
		return nested.keySet();
	}

	@Override
	public Object get(Object key) {
		return nested.get(key);
	}
}
