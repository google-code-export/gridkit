package org.gridkit.lab.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

class CSVHeader {
	
	List<String> header1;
	List<String> header2;
	
	Map<String, Integer> columns = new HashMap<String, Integer>();

	public CSVHeader(List<String> h1, List<String> h2) {
		header1 = new ArrayList<String>(h1);
		header2 = new ArrayList<String>(h2);
		
		header1.removeAll(h2);
		
		int n = 0;
		for(String h: header1) {
			columns.put(h, n++);
		}
		n++;
		for(String h: header2) {
			columns.put(h, n++);
		}
		n++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((header1 == null) ? 0 : header1.hashCode());
		result = prime * result
				+ ((header2 == null) ? 0 : header2.hashCode());
		return result;
	}
	
	public CSVHeader merge(CSVHeader that) {
		LinkedHashSet<String> hh1 = new LinkedHashSet<String>();
		LinkedHashSet<String> hh2 = new LinkedHashSet<String>();
		
		hh1.addAll(header1);
		hh1.addAll(that.header1);
		hh2.addAll(header2);
		hh2.addAll(that.header2);
		return new CSVHeader(new ArrayList<String>(hh1), new ArrayList<String>(hh2));
	}
	
	public Sample read(String[] values) {
		if (header1.size() + header2.size() + 1 != values.length) {
			if (header2.size() == 0 && header1.size() == values.length) {
				// special case
			}
			else {
				throw new IllegalArgumentException("Invalid number of fields");
			}
		}
		Sample dp = new Sample();
		int n = 0;
		for(String field: header1) {
			String val = values[n++];
			if (val.length() > 0) {
				dp.setCoord(field, val);
			}
		}
		if (values.length > n) { 
			if (values[n++].trim().length() != 0) {
				throw new IllegalArgumentException("Illegal row format, '-' field is not empty");
			}
			for(String field: header2) {
				String val = values[n++];
				if (val.length() > 0) {
					dp.setResult(field, val);
				}
			}
		}
		return dp;
	}

	public String[] write(Sample dp) {
		if (header1.containsAll(dp.coordinateKeys()) && header2.containsAll(dp.resultsKeys())) {
			String[] row = new String[header1.size() + header2.size() + 1];
			int n = 0;
			for(String h: header1) {
				String v = dp.get(h);
				row[n++] = v == null ? "" : v;
			}
			row[n++] = "";
			for(String h: header2) {
				String v = dp.get(h);
				row[n++] = v == null ? "" : v;
			}
			
			return row;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSVHeader other = (CSVHeader) obj;
		if (header1 == null) {
			if (other.header1 != null)
				return false;
		} else if (!header1.equals(other.header1))
			return false;
		if (header2 == null) {
			if (other.header2 != null)
				return false;
		} else if (!header2.equals(other.header2))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String h: header1) {
			if (sb.length() == 0) {
				sb.append(' ');
			}				
			sb.append(h);
		}
		sb.append(" -");
		for(String h: header2) {
			sb.append(' ');
			sb.append(h);
		}
		return super.toString();
	}
}