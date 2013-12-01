package org.gridkit.lab.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleList {

	private List<Sample> samples;
	
	private Map<String, Object> typedValueCache = new HashMap<String, Object>();

	
	public SampleList(List<Sample> samples) {
		this.samples = new ArrayList<Sample>(samples);
	}

	public SampleList sort(String... fields) {
		return sort(fields, false);
	}

	public SampleList sortReverse(String... fields) {
		return sort(fields, true);
	}
	
	public SampleList sort(String[] fields, boolean reverse) {
		Integer[] order = new Integer[samples.size()];
		for(int i = 0; i != order.length; ++i) {
			order[i] = i;
		}
		for(int i = fields.length - 1; i >= 0; --i) {
			sort(order, fields[i], reverse);
		}
		Sample[] result = new Sample[order.length];
		for(int i = 0; i != result.length; ++i) {
			result[i] = samples.get(order[i]);
		}
		return new SampleList(Arrays.asList(result));
	}
	
	/**
	 * Retains only first sample in each group
	 */
	public SampleList filterFirst(String... fields) {
		Set<List<Object>> tags = new HashSet<List<Object>>();
		
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
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
	 * Retains only middle sample in each group
	 */
	public SampleList filterMedian(String... fields) {
		Map<List<Object>, List<Sample>> groups = new LinkedHashMap<List<Object>, List<Sample>>();
		
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			Object[] tag = new Object[fields.length];
			for(int i = 0; i != fields.length; ++i) {
				tag[i] = sample.get(fields[i]);
			}
			List<Sample> group = groups.get(Arrays.asList(tag));
			if (group == null) {
				group = new ArrayList<Sample>();
				groups.put(Arrays.asList(tag), group);
			}
			group.add(sample);
		}
		for(List<Sample> group: groups.values()) {
			samples.add(group.get(group.size() / 2));
		}
		return new SampleList(samples);
	}

	/**
	 * Retains only middle sample in each group
	 */
	public SampleList filterMedRange(int size, String... fields) {
		Map<List<Object>, List<Sample>> groups = new LinkedHashMap<List<Object>, List<Sample>>();
		
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			Object[] tag = new Object[fields.length];
			for(int i = 0; i != fields.length; ++i) {
				tag[i] = sample.get(fields[i]);
			}
			List<Sample> group = groups.get(Arrays.asList(tag));
			if (group == null) {
				group = new ArrayList<Sample>();
				groups.put(Arrays.asList(tag), group);
			}
			group.add(sample);
		}
		for(List<Sample> group: groups.values()) {
			while(group.size() > size) {
				group.remove(0);
				if (group.size() > size) {
					group.remove(group.size() - 1);
				}
			}
			samples.addAll(group);
		}
		return new SampleList(samples);
	}
	
	/**
	 * Groups sample by selected attributes and allows further aggregation
	 */
	public Aggregation aggregate(String... fields) {
		Map<List<String>, List<Sample>> groups = new LinkedHashMap<List<String>, List<Sample>>();
		
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			String[] tag = new String[fields.length];
			for(int i = 0; i != fields.length; ++i) {
				tag[i] = sample.get(fields[i]);
			}
			List<Sample> group = groups.get(Arrays.asList(tag));
			if (group == null) {
				group = new ArrayList<Sample>();
				groups.put(Arrays.asList(tag), group);
				Sample gg = new Sample();
				for(int i = 0; i != tag.length; ++i) {
					gg.setCoord(fields[i], tag[i]);
				}
				samples.add(gg);				
			}
			group.add(sample);
		}
		return new Aggregation(samples, new ArrayList<List<Sample>>(groups.values()));
	}
	
	
//	public SampleList retain(String field, String value) {
//		List<Sample> samples = new ArrayList<Sample>();
//		for(Sample sample: this.samples) {
//			if (value.equals(sample.get(field))) {
//				samples.add(sample);
//			}
//		}
//		return new SampleList(samples);
//	}

	public SampleList retain(String field, String... anyOf) {
		if (anyOf.length == 0) {
			throw new IllegalArgumentException("Value list is empty");
		}
		Set<Object> values = new HashSet<Object>(Arrays.asList(anyOf));
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			if (values.contains(sample.get(field))) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}

	public SampleList retain(String field, double... anyOf) {
		if (anyOf.length == 0) {
			throw new IllegalArgumentException("Value list is empty");
		}
		Arrays.sort(anyOf);
		List<Sample> result = new ArrayList<Sample>();
		for(int i = 0; i != samples.size(); ++i) {
			Object v = getTyped(i, field);
			if (v instanceof Number) {
				double d = ((Number)v).doubleValue(); 
				if (Arrays.binarySearch(anyOf, d) >= 0) {
					result.add(samples.get(i));
				}
			}
		}
		return new SampleList(result);
	}

	public SampleList retain(String field, int[] anyOf) {
		if (anyOf.length == 0) {
			throw new IllegalArgumentException("Value list is empty");
		}
		Arrays.sort(anyOf);
		List<Sample> result = new ArrayList<Sample>();
		for(int i = 0; i != samples.size(); ++i) {
			Object v = getTyped(i, field);
			if (v instanceof Number) {
				int d = ((Number)v).intValue(); 
				if (Arrays.binarySearch(anyOf, d) >= 0) {
					result.add(samples.get(i));
				}
			}
		}
		return new SampleList(result);
	}

	public SampleList retain(String field, Collection<String> anyOf) {
		Set<Object> values = new HashSet<Object>(anyOf);
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			if (values.contains(sample.get(field))) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}

	public SampleList retainRange(String field, double l, double h) {
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			double v = sample.getDouble(field);					
			if (v >= l && v <= h) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}

	public SampleList retainRegEx(String field, String pattern) {
		Pattern re = Pattern.compile(pattern);
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			String val = sample.get(field);
			if (val != null && re.matcher(val).matches()) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}
	

//	public SampleList remove(String field, String value) {
//		List<Sample> samples = new ArrayList<Sample>();
//		for(Sample sample: this.samples) {
//			if (!(value.equals(sample.get(field)))) {
//				samples.add(sample);
//			}
//		}
//		return new SampleList(samples);
//	}
//	
	public SampleList remove(String field, String... anyOf) {
		Set<Object> values = new HashSet<Object>(Arrays.asList(anyOf));
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			if (!(values.contains(sample.get(field)))) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}
	
	public SampleList remove(String field, double l, double h) {
		List<Sample> samples = new ArrayList<Sample>();
		for(Sample sample: this.samples) {
			double v = sample.getDouble(field);					
			if (!(v >= l && v <= h)) {
				samples.add(sample);
			}
		}
		return new SampleList(samples);
	}
	
	public SampleList replace(String field, String oldValue, String newValue) {
		List<Sample> result = new ArrayList<Sample>();
		for(Sample sample: samples) {
			Sample sample2 = sample.clone();
			if (areEqual(oldValue, sample2.get(field))) {
				sample2.set(field, newValue);
			}
			result.add(sample2);
		}
		return new SampleList(result);
	}

	private boolean areEqual(Object v1, Object v2) {
		if (v1 == null) {
			return v2 == null;
		}
		else {
			return v1.equals(v2);
		}
	}
	
	public SampleList withField(String field, String val) {
		return withFields(field, val);
	}

	public SampleList times(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n cannot be negative");
		}
		List<Sample> result = new ArrayList<Sample>();
		for(Sample sample: samples) {
			for(int i = 0; i != n; ++i) {
				Sample s = sample.clone();
				result.add(s);
			}
		}
		return new SampleList(result);
	}

	public SampleList withField(String field, double val) {
		return withFields(field, val);
	}

	public SampleList withFields(String field, String... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("value list is empty");
		}
		List<Sample> result = new ArrayList<Sample>();
		for(Sample sample: samples) {
			for(String v: values) {
				Sample s = sample.clone();
				s.setCoord(field, v);
				result.add(s);
			}
		}
		return new SampleList(result);
	}

	public SampleList withFields(String field, double... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("value list is empty");
		}
		List<Sample> result = new ArrayList<Sample>();
		for(Sample sample: samples) {
			for(double v: values) {
				Sample s = sample.clone();
				s.setCoord(field, v);
				result.add(s);
			}
		}
		return new SampleList(result);
	}
	
	public SampleList transformRegEx(String field, String pattern, String format) {
		Pattern re = Pattern.compile(pattern);
		List<Sample> result = new ArrayList<Sample>();
		for(Sample sample: samples) {
			Sample sample2 = sample.clone();
			String value = sample2.get(field);
			if (value != null) {
				Matcher m = re.matcher(value);
				if (m.matches()) {
					Object[] gg = new Object[m.groupCount()];
					for (int i = 0; i != gg.length; ++i) {
						gg[i] = m.group(i + 1);
					}
					value = String.format(format, gg);
					sample2.set(field, value);
					result.add(sample2);
				}
			}
		}
		return new SampleList(result);
	}
	
	public Map<String, SampleList> groupBy(String field) {
		Map<String, SampleList> result = new HashMap<String, SampleList>();
		for(Sample sample: samples) {
			String val = sample.get(field);
			SampleList s = result.get(val);
			if (s == null) {
				s = new SampleList(new ArrayList<Sample>());
				result.put(val, s);
			}
			s.samples.add(sample);
		}
		return result;
	}

	public Set<String> distinct(String field) {
		Set<String> result = new LinkedHashSet<String>();
		for(Sample sample: samples) {
			String val = sample.get(field);
			result.add(val);
		}
		return result;
	}

	public Set<List<String>> distinct(String first, String... rest) {
		String[] fields = new String[rest.length + 1];
		fields[0] = first;
		for(int i = 0; i != rest.length; ++i) {
			fields[i + 1] = rest[i];
		}
		Set<List<String>> result = new HashSet<List<String>>();
		for(Sample sample: samples) {
			String[] val = new String[fields.length];
			for(int i = 0; i != val.length; ++i) {
				val[i] = sample.get(fields[i]); 
			}
			result.add(Arrays.asList(val));
		}
		return result;
	}
	
	public double[] numericSeries(String field) {
		double[] s = new double[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = ((Number)getTyped(i, field)).doubleValue();
		}
		return s;
	}

	public long[] integerSeries(String field) {
		long[] s = new long[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = ((Number)getTyped(i, field)).longValue();
		}
		return s;
	}

	public SampleList scaleFieldValue(String field, double f, double s) {
		List<Sample> result = new ArrayList<Sample>(samples.size());
		for(Sample sample: samples) {
			sample = sample.clone();
			double v = sample.getDouble(field);
			v = f * v + s;
			sample.set(field, v);
			result.add(sample);
		}
		return new SampleList(result);
	}
	
	public double[] scaledNumericSeries(String field, double factor) {
		double[] s = new double[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = factor * ((Number)getTyped(i, field)).doubleValue();
		}
		return s;
	}

	public String[] textSeries(String field) {
		String[] s = new String[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = samples.get(i).get(field);
		}
		return s;
	}

	public Object[] objectSeries(String field) {
		Object[] s = new Object[samples.size()];
		for(int i = 0; i != s.length; ++i) {
			s[i] = getTyped(i, field);
		}
		return s;
	}
	
	public void exportTabSeparated(Writer writer, String... fields) throws IOException {
		for(Sample sample: samples) {
			for(String field: fields) {
				String val = sample.get(field);
				if (val == null || val.length() == 0) {
					val = "\"\"";
				}
				else if (val.indexOf(' ') >= 0 || val.indexOf('\t') >= 0) {
					val = "\"" + val + "\"";
				}
				writer.append(val).append('\t');
			}
			writer.append('\n');
		}
	}
	
	private Object deriveType(String field) {
		if (samples.size() == 0) {
			return String.class;
		}
		double[] dv = new double[samples.size()];
		long[] dl = new long[samples.size()];
		for(int i = 0; i != samples.size(); ++i) {
			String v = samples.get(i).get(field);
			if (v == null) {
				return String.class;
			}
			try {
				if (dl != null) {
					dl[i] = Long.parseLong(v);
					dv[i] = dl[i];
					continue;
				}
			} catch (NumberFormatException e) {
				dl = null;
				// not a long
			}
			try {
				if (dv != null) {
					dv[i] = Double.parseDouble(v);
					continue;
				}
			} catch (NumberFormatException e) {
				dv = null;
				// not a decimal
			}
			return String.class;
		}
		return dl != null ? dl : dv != null ? dv : String.class; 
	}

	private Object getTyped(int row, String field) {
		Object col = typedValueCache.get(field); 
		if (col == null) {
			col = deriveType(field);
			typedValueCache.put(field, col);
		}
		
		if (col == String.class) {
			return samples.get(row).get(field);
		}
		else if (col instanceof long[]) {
			return ((long[])col)[row];
		}
		else if (col instanceof double[]) {
			return ((double[])col)[row];
		}
		else {
			throw new RuntimeException("Broken typed cache");
		}
	}
	
	private void sort(final Integer[] order, final String field, final boolean reverse) {
		Comparator<Integer> cmp = new Comparator<Integer>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Integer o1, Integer o2) {
				Object a1 = getTyped(o1, field);
				Object a2 = getTyped(o2, field);
				int n;
				if (a1 == a2) {
					n = 0;
				}
				else if (a1 == null) {
					n = -1;
				}
				else if (a2 == null) {
					n = 1;
				}
				else {
					n = ((Comparable)a1).compareTo(a2);
				}
				
				return (reverse ? -1 : 1) * n;
			}
		};
		
		Arrays.sort(order, cmp);
	}
	
	public int size() {
		return samples.size();
	}
	
	public List<Sample> asList() {
		return Collections.unmodifiableList(samples);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Sample sample: samples) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(sample.toString());
		}

		return sb.toString();
	}	
	
	public class Aggregation {
		
		List<Sample> result = new ArrayList<Sample>();
		List<List<Sample>> groups = new ArrayList<List<Sample>>();

		private Aggregation(List<Sample> result, List<List<Sample>> groups) {
			this.result = result;
			this.groups = groups;
		}

		public Aggregation average(String sourceField) {
			return average(sourceField, sourceField);
		}

		public Aggregation average(String sourceField, String targetField) {
			for(int i = 0; i != result.size(); ++i) {
				double avg = average(groups.get(i), sourceField);
				result.get(i).setResult(targetField, avg);
			}
			
			return this;
		}

		public Aggregation count(String targetField) {
			for(int i = 0; i != result.size(); ++i) {
				result.get(i).setResult(targetField, groups.get(i).size());
			}
			
			return this;
		}
		
		private double average(List<Sample> list, String sourceField) {
			double sum = 0;
			for(Sample s: list) {
				sum += s.getDouble(sourceField);
			}
			return sum / list.size();
		}

		public SampleList toList() {
			return new SampleList(result);
		}
	}
}
