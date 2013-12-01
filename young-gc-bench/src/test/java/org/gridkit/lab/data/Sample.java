package org.gridkit.lab.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Sample implements Serializable, Comparable<Sample>, Cloneable {

	public static Comparator<Sample> comparator(final String... fields) {
		return new Comparator<Sample>() {
			@Override
			public int compare(Sample o1, Sample o2) {
				for(String f: fields) {
					int n = compare(o1.get(f), o2.get(f));
					if (n != 0) {
						return n;
					}
				}
				return o1.compareTo(o2);
			}

			private int compare(String v1, String v2) {
				if (v1 == v2) {
					return 0;
				}
				else if (v1 == null) {
					return -1;
				}
				else if (v2 == null) {
					return 1;
				}
				else {
					return v1.compareTo(v2);
				}
			}
		};
	}

	public static Comparator<Sample> reverseComparator(String... fields) {
		return Collections.reverseOrder(comparator(fields));
	}
	
	private static final long serialVersionUID = 20130702L;
	
	public static String TOTAL = "total";
	public static String COUNT = "count";
	public static String SQ_TOTAL = "sq.total";
	
	private Set<String> coordinates = new TreeSet<String>();
	private Set<String> results = new TreeSet<String>();
	
	private Map<String, String> data = new TreeMap<String, String>();
	
	public Collection<String> coordinateKeys() {
		return Collections.unmodifiableSet(coordinates);
	}

	public Collection<String> resultsKeys() {
		return Collections.unmodifiableSet(results);
	}
	
	public Sample retainFields(Collection<String> fields) {
		Sample that = clone();
		that.data.keySet().retainAll(fields);
		that.coordinates.retainAll(fields);
		that.results.retainAll(fields);
		return that;
	}

	public String get(String key) {
		return data.get(key);
	}

	public void setCoord(String key, String value) {
		coordinates.add(key);
		data.put(key, value);
	}

	private static String toString(long value) {
		return String.valueOf(value);
	}

	private static String toString(double value) {
		if (value == (double)((long)value)) {
			return String.valueOf((long)value);
		}
		else {
			return String.valueOf(value);
		}
	}
	
	public void setCoord(String key, long value) {
		coordinates.add(key);
		data.put(key, toString(value));
	}
	
	public void setCoord(String key, double value) {
		coordinates.add(key);
		data.put(key, toString(value));
	}
	
	public void setResult(String key, String value) {
		results.add(key);
		data.put(key, value);
	}

	public void setResult(String key, long value) {
		results.add(key);
		data.put(key, toString(value));
	}
	
	public void setResult(String key, double value) {
		results.add(key);
		data.put(key, toString(value));
	}

	public void set(String key, String value) {
		if (!data.containsKey(key)) {
			throw new IllegalArgumentException("Use setCoord or setResult for non existing fields");
		}
		data.put(key, value);
	}

	public void set(String key, double value) {
		if (!data.containsKey(key)) {
			throw new IllegalArgumentException("Use setCoord or setResult for non existing fields");
		}
		data.put(key, toString(value));
	}

	public void set(String key, long value) {
		if (!data.containsKey(key)) {
			throw new IllegalArgumentException("Use setCoord or setResult for non existing fields");
		}
		data.put(key, toString(value));
	}

	public Sample clone() {
		try {
			Sample that = (Sample) super.clone();
			that.coordinates = new TreeSet<String>(that.coordinates);
			that.results = new TreeSet<String>(that.results);
			that.data = new TreeMap<String, String>(that.data);
			return that;
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public int getInteger(String key) {
		return Integer.parseInt(data.get(key));
	}

	public long getLong(String key) {
		return Long.parseLong(data.get(key));
	}
	
	public double getDouble(String key) {
		return Double.parseDouble(data.get(key));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sample other = (Sample) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public int compareTo(Sample o) {
		return data.toString().compareTo(o.data.toString());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String c: coordinates) {
			sb.append(c).append(": ").append(data.get(c)).append(' ');
		}
		sb.append("-");
		for(String c: results) {
			sb.append(' ').append(c).append(": ").append(data.get(c));
		}
		return sb.toString();
	}
}
