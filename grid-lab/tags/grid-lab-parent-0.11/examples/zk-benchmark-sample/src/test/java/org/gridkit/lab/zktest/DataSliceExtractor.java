package org.gridkit.lab.zktest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gridkit.nimble.metering.SampleReader;
import org.gridkit.nimble.pivot.Filters;
import org.gridkit.nimble.pivot.SampleFilter;

public class DataSliceExtractor {
	
	private List<SampleFilter> filters = new ArrayList<SampleFilter>();
	private List<Object> extract = new ArrayList<Object>();
	
	private List<double[]> data = new ArrayList<double[]>();

	public void filter(Object key, Object value) {
		filters.add(Filters.equals(key, value));
	}

	public void filter(SampleFilter filter) {
		filters.add(filter);
	}
	
	public void feed(SampleReader reader) {
		if (reader.isReady() || reader.next()) {
			while(true) {
				process(reader);
				if (!reader.next()) {
					break;
				}
			}
		}
	}
	
	public List<double[]> getData() {
		return data;
	}
	
	public void clear() {
		data.clear();
	}
	
	private void process(SampleReader reader) {
		for(SampleFilter f: filters) {
			if (!f.match(reader)) {
				return;
			}
		}
		double[] row = new double[extract.size()];
		for(int i = 0; i != row.length; ++i) {
			Object v = reader.get(extract.get(i));
			row[i] = ((Number)v).doubleValue();
		}
		data.add(row);
	}

	public void addField(Object key) {
		extract.add(key);
	}
	
	public void sort(int n) {
		Collections.sort(data, new DoubleArrayComparator(n));
	}
	
	public void shift(int n, double value) {
		for(double[] row: data) {
			row[n] += value;
		}
	}

	private static class DoubleArrayComparator implements Comparator<double[]> {

		private final int n;
		
		private DoubleArrayComparator(int n) {
			this.n = n;
		}

		@Override
		public int compare(double[] o1, double[] o2) {			
			return Double.compare(o1[n], o2[n]);
		}
	}

	public void extract(Object attrKey) {
		extract.add(attrKey);
	}	
}
