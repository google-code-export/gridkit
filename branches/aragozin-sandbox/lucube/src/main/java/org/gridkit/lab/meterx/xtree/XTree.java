package org.gridkit.lab.meterx.xtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.gridkit.lab.meterx.EntityNode;
import org.gridkit.lab.meterx.HierarchyNode;
import org.gridkit.lab.meterx.ObserverNode;
import org.gridkit.lab.meterx.SampleReader;

public class XTree {

	private Entity root;
	private Map<String, Node> nodes = new HashMap<String, Node>();
	
	public XTree() {
		this.root = new Entity();
		this.root.id = "";
		this.nodes.put("", root);
	}
	
	public EntityNode getRoot() {
		return root;
	}
	
	private static class Node implements HierarchyNode {

		String id;
		Entity parent;
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		Set<String> traits = new LinkedHashSet<String>();

		@Override
		public String id() {
			return id;
		}

		@Override
		public EntityNode getParent() {
			return parent;
		}

		@Override
		public Map<String, String> getAttributes() {
			return Collections.unmodifiableMap(attributes);
		}

		@Override
		public Set<String> getTraits() {
			return Collections.unmodifiableSet(traits);
		}
	}

	private static class Entity extends Node implements EntityNode {

		private List<Entity> entries = new ArrayList<Entity>();
		private List<Observer> observers = new ArrayList<Observer>();
		
		@Override
		public Collection<EntityNode> childrenEntities() {
			return Collections.<EntityNode>unmodifiableCollection(entries);
		}

		@Override
		public Collection<ObserverNode> childrenObservers() {
			return Collections.<ObserverNode>unmodifiableCollection(observers);
		}
	}
	
	private static class Observer extends Node implements ObserverNode {

		private Map<String, Class<?>> types = new HashMap<String, Class<?>>();
		private Map<String, SampleColumn> columns = new HashMap<String, SampleColumn>();
		private int size;
		
		@Override
		public Map<String, Class<?>> getColumnTypes() {
			return Collections.unmodifiableMap(types);
		}

		@Override
		public SampleReader readSamples() {
			return null;
		}
	}
		
	private static class Reader implements SampleReader {

		private Observer observer;
		private int pos = -1;
		
		@Override
		public ObserverNode getObserverNode() {
			return observer;
		}

		@Override
		public boolean isReady() {
			return pos >= 0;
		}

		@Override
		public boolean next() {
			++pos;
			if (pos < 0 || pos > observer.size) {
				pos = Integer.MIN_VALUE;
				return false;
			}
			return true;
		}

		@Override
		public Object get(String field) {
			SampleColumn c = observer.columns.get(field);
			if (c == null) {
				throw new NoSuchElementException("No such field [" + field + "]");
			}					
			return c.get(pos);
		}

		@Override
		public boolean getBoolean(String field) {
			SampleColumn c = observer.columns.get(field);
			if (c == null) {
				throw new NoSuchElementException("No such field [" + field + "]");
			}					
			return c.getBoolean(pos);
		}

		@Override
		public long getLong(String field) {
			SampleColumn c = observer.columns.get(field);
			if (c == null) {
				throw new NoSuchElementException("No such field [" + field + "]");
			}					
			return c.getLong(pos);
		}

		@Override
		public double getDouble(String field) {
			SampleColumn c = observer.columns.get(field);
			if (c == null) {
				throw new NoSuchElementException("No such field [" + field + "]");
			}					
			return c.getDouble(pos);
		}

		@Override
		public String getString(String field) {
			SampleColumn c = observer.columns.get(field);
			if (c == null) {
				throw new NoSuchElementException("No such field [" + field + "]");
			}					
			return c.getString(pos);
		}
	}
	
	private static interface SampleColumn {
		
		public String getName();
		
		public Class<?> getType();
		
		public Object get(int n);

		public boolean getBoolean(int n);

		public long getLong(int n);

		public double getDouble(int n);

		public String getString(int n);

	}
	
	private static class BooleanColumn implements SampleColumn {
		
		private String name;
		private boolean[][] segments = new boolean[16][];
		private int blockSize = 1 << 20;
		private int size;
		
		@Override
		public String getName() {
			return name;
		}
		@Override
		public Class<?> getType() {
			return boolean.class;
		}
		@Override
		public Object get(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public boolean getBoolean(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}

		@Override
		public long getLong(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}

		@Override
		public double getDouble(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}

		@Override
		public String getString(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return String.valueOf(segments[n / blockSize][n % blockSize]);
		}
	}

	private static class LongColumn implements SampleColumn {
		
		private String name;
		private long[][] segments = new long[16][];
		private int blockSize = 8 << 10;
		private int size;
		
		@Override
		public String getName() {
			return name;
		}
		@Override
		public Class<?> getType() {
			return long.class;
		}
		@Override
		public Object get(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public boolean getBoolean(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}

		@Override
		public long getLong(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public double getDouble(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public String getString(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return String.valueOf(segments[n / blockSize][n % blockSize]);
		}
	}

	private static class DoubleColumn implements SampleColumn {
		
		private String name;
		private double[][] segments = new double[16][];
		private int blockSize = 8 << 10;
		private int size;
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public Class<?> getType() {
			return double.class;
		}
		
		@Override
		public Object get(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public boolean getBoolean(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}

		@Override
		public long getLong(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return (long)segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public double getDouble(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public String getString(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return String.valueOf(segments[n / blockSize][n % blockSize]);
		}
	}

	private static class StringColumn implements SampleColumn {
		
		private String name;
		private String[][] segments = new String[16][];
		private int blockSize = 8 << 10;
		private int size;
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public Class<?> getType() {
			return String.class;
		}
		
		@Override
		public Object get(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
		
		@Override
		public boolean getBoolean(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}
		
		@Override
		public long getLong(int n) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public double getDouble(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			throw new UnsupportedOperationException();
		}

		@Override
		public String getString(int n) {
			if (n < 0 || n >= size) {
				throw new IndexOutOfBoundsException("[0, " + size + ") requested [" + n + "]");
			}			
			return segments[n / blockSize][n % blockSize];
		}
	}
}
