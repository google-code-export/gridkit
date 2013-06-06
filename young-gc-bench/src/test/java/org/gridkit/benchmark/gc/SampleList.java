package org.gridkit.benchmark.gc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SampleList {

	private List<Map<String, Object>> samples;
	
	public SampleList(List<Map<String, Object>> samples) {
		this.samples = new ArrayList<Map<String,Object>>(samples);
	}

	public SampleList sort(String... fields) {
		List<Map<String, Object>> copy = new ArrayList<Map<String,Object>>(samples);
		for(int i = fields.length - 1; i >= 0; --i) {
			sort(copy, fields[i], false);
		}
		return new SampleList(copy);
	}

	public SampleList reverseSort(String... fields) {
		List<Map<String, Object>> copy = new ArrayList<Map<String,Object>>(samples);
		for(int i = fields.length - 1; i >= 0; --i) {
			sort(copy, fields[i], true);
		}
		return new SampleList(copy);
	}
	
	/**
	 * Retains only first sample in each group
	 */
	public SampleList filterFirst(String... fields) {
		Set<List<Object>> tags = new HashSet<List<Object>>();
		
		List<Map<String, Object>> samples = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> sample: this.samples) {
			Object[] tag = new Object[fields.length];
			for(int i = 0; i != fields.length; ++i) {
				tag[i] = sample.get(fields[i]);
			}
			if (tags.add(Arrays.asList(tag))) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}

	/**
	 * Retains only first sample in each group
	 */
	public SampleList filterMedian(String... fields) {
		Map<List<Object>, List<Map<String, Object>>> groups = new LinkedHashMap<List<Object>, List<Map<String,Object>>>();
		
		List<Map<String, Object>> samples = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> sample: this.samples) {
			Object[] tag = new Object[fields.length];
			for(int i = 0; i != fields.length; ++i) {
				tag[i] = sample.get(fields[i]);
			}
			List<Map<String, Object>> group = groups.get(Arrays.asList(tag));
			if (group == null) {
				group = new ArrayList<Map<String,Object>>();
				groups.put(Arrays.asList(tag), group);
			}
			group.add(sample);
		}
		for(List<Map<String, Object>> group: groups.values()) {
			samples.add(group.get(group.size() / 2));
		}
		return new SampleList(samples);
	}
	
	public SampleList filter(String field, Object value) {
		List<Map<String, Object>> samples = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> sample: this.samples) {
			if (value.equals(sample.get(field))) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}

	public SampleList filter(String field, double l, double h) {
		List<Map<String, Object>> samples = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> sample: this.samples) {
			double v = ((Number)sample.get(field)).doubleValue();					
			if (v >= l && v <= h) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}
	
	public Map<Object, SampleList> groupBy(String field) {
		Map<Object, SampleList> result = new HashMap<Object, SampleList>();
		for(Map<String, Object> sample: samples) {
			Object val = sample.get(field);
			SampleList s = result.get(val);
			if (s == null) {
				s = new SampleList(new ArrayList<Map<String,Object>>());
				result.put(val, s);
			}
			s.samples.add(sample);
		}
		return result;
	}
	
	public double[] numericSeries(String field) {
		double[] s = new double[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = ((Number)samples.get(i).get(field)).doubleValue();
		}
		return s;
	}

	public double[] scaledNumericSeries(String field, double factor) {
		double[] s = new double[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = factor * ((Number)samples.get(i).get(field)).doubleValue();
		}
		return s;
	}

	public Object[] objectSeries(String field) {
		Object[] s = new Object[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = samples.get(i).get(field);
		}
		return s;
	}
	
	private void sort(List<Map<String, Object>> samples, final String field, final boolean reverse) {
		Comparator<Map<String, Object>> cmp = new Comparator<Map<String,Object>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Object a1 = o1.get(field);
				Object a2 = o2.get(field);
				return (reverse ? -1 : 1) * ((Comparable)a1).compareTo((Comparable)a2);
			}
		};
		
		Collections.sort(samples, cmp);
	}
	
	public int size() {
		return samples.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Map<String, Object> sample: samples) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(sample.toString());
		}

		return sb.toString();
	}	
}
