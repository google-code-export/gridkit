package org.gridkit.lab.meterx.xtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gridkit.lab.meterx.EntityAppender;
import org.gridkit.lab.meterx.EntityNode;
import org.gridkit.lab.meterx.HierarchyAppender;
import org.gridkit.lab.meterx.HierarchyNode;
import org.gridkit.lab.meterx.ObserverAppender;
import org.gridkit.lab.meterx.ObserverNode;
import org.gridkit.lab.meterx.SampleReader;

public class XTree {

	private static List<Class<?>> FIELD_TYPES = Arrays.<Class<?>>asList(double.class, long.class, String.class, boolean.class);
	
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
	public EntityAppender getAppender() {
		EAppender ra = new EAppender();
		ra.mergeNode = root;
		ra.metaDone = true;
		return ra;
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

	private abstract class HAppender implements HierarchyAppender {

		Entity parent;
		Node mergeNode;
        String id;
        Map<String, String> attributes;
        Set<String> traits;
        boolean metaDone;
        		
        @Override
		public void addAttribute(String name, String value) {
			if (metaDone) {
				throw new IllegalStateException("Meta is finalized");
			}
			if (attributes == null) {
				attributes = new LinkedHashMap<String, String>();
			}
			attributes.put(name, value);
		}
        
		@Override
		public void addTrait(String trait) {
            if (metaDone) {
                throw new IllegalStateException("Meta is finalized");
            }
            if (traits == null) {
            	traits = new LinkedHashSet<String>();
            }
            traits.add(trait);
		}
		
		@Override
		public void metaSkip() {
			if (metaDone) {
				throw new IllegalStateException("Meta is finalized");
			}
			if (!isMetaEmpty()) {
				throw new IllegalStateException("Meta is set, cannot skip");
			}
			if (mergeNode == null) {
				throw new IllegalStateException("Node is not initialized, cannot skip meta definition");
			}
		}

		protected boolean isMetaEmpty() {
			return attributes == null && traits == null;
		}
		
		@Override
		public void metaDone() {
            if (metaDone) {
                throw new IllegalStateException("Meta is finalized");
            }
            initMergeNode();
            metaDone = true;
		}

		public void initMergeNode() {
			if (mergeNode == null) {
				mergeNode = createMergeNode();
			}
			else {
				verifyMeta();
			}
		}

		protected void verifyMeta() {			
			if (new TreeMap<String, String>(mergeNode.attributes).equals(new TreeMap<String, String>(attributes))) {
				throw new IllegalArgumentException("Node merge failed, metadata are not identical. ID: [" + id + "]");
			}
			if (new TreeSet<String>(mergeNode.traits).equals(new TreeSet<String>(traits))) {
				throw new IllegalArgumentException("Node merge failed, metadata are not identical. ID: [" + id + "]");
			}			
		}

		protected abstract Node createMergeNode();
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
	
	private class EAppender extends HAppender implements EntityAppender {

		protected Entity node() {
			if (!metaDone) {
				throw new IllegalStateException("Should finalize meta first");
			}
			return (Entity)mergeNode;
		}
		
		@Override
		public EntityAppender addChildEntity(String entityId) {
			EAppender appender = new EAppender();
			appender.parent = node();
			appender.id = entityId;
			
			Node me = nodes.get(entityId);
			if (me instanceof Observer) {
				throw new IllegalArgumentException("Cannot append entity, observer [" + entityId + "] is already exists");
			}
			
			appender.mergeNode = me;
			
			return appender;
		}

		@Override
		public ObserverAppender addChildObserver(String observerId) {
            OAppender appender = new OAppender();
            appender.parent = node();
            appender.id = observerId;
            
            Node me = nodes.get(observerId);
            if (me instanceof Entity) {
                throw new IllegalArgumentException("Cannot append observer, entity [" + observerId + "] is already exists");
            }
            
            appender.mergeNode = me;
            
            return appender;
		}

		@Override
		protected Node createMergeNode() {
			Entity entity = new Entity();
			entity.id = id;
			entity.parent = parent;
			entity.attributes.putAll(attributes);
			entity.traits.addAll(traits);
			nodes.put(id, entity);
			parent.entries.add(entity);
			return entity;
		}
	}
	
	private static class Observer extends Node implements ObserverNode {

		private Map<String, Class<?>> types;
		private Map<String, SampleColumn> columns;
		private int size;
		
		@Override
		public Map<String, Class<?>> getColumnTypes() {
			return Collections.unmodifiableMap(types);
		}

		@Override
		public SampleReader readSamples() {
			return new Reader();
		}

		void initColumns() {
			columns = new HashMap<String, SampleColumn>();
			for(String field: types.keySet()) {
				Class<?> c = types.get(field);
				if (c == double.class) {
					columns.put(field, new DoubleColumn(field));
				}
				else if (c == long.class) {
					columns.put(field, new LongColumn(field));
				}
				else if (c == String.class) {
					columns.put(field, new StringColumn(field));
				}
				else if (c == boolean.class) {
					columns.put(field, new BooleanColumn(field));
				}
				else {
					throw new IllegalArgumentException("Unsupported type for field [" + field + "] - " + c.getName());
				}
			}
		}
	}
	
	private class OAppender extends HAppender implements ObserverAppender {

		Map<String, Class<?>> types;
		
        protected Observer node() {
            if (!metaDone) {
                throw new IllegalStateException("Should finalize meta first");
            }
            return (Observer)mergeNode;
        }		
		
		@Override
		public void addField(String name, Class<?> type) {
            if (metaDone) {
                throw new IllegalStateException("Meta is finalized");            
            }
            if (!FIELD_TYPES.contains(type)) {
            	throw new IllegalArgumentException("Class [" + type.getName() + "] is not valid field type");
            }
            if (types == null) {
            	types = new LinkedHashMap<String, Class<?>>();
            }
            types.put(name, type);
		}

		@Override
		public void importSamples(SampleReader reader) {
			Observer obs = node();
			if (!reader.isReady()) {
				reader.next();
			}
			Collection<SampleColumn> cols = obs.columns.values();
			while(reader.isReady()) {
				for(SampleColumn c: cols) {
					c.append(reader);
				}
				obs.size++;
				if (!reader.next()) {
					break;
				}
			}
		}

		@Override
		protected boolean isMetaEmpty() {
			return super.isMetaEmpty() && types == null;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void verifyMeta() {
			super.verifyMeta();
			if (!new TreeMap(node().types).equals(new TreeMap(types))) {
                throw new IllegalArgumentException("Node merge failed, metadata are not identical. ID: [" + id + "]");
			}
		}

		@Override
		protected Node createMergeNode() {
			if (types == null || types.isEmpty()) {
				throw new IllegalArgumentException("Observer should have at least one field defined");
			}
			Observer obs = new Observer();
			obs.id = id;
			obs.parent = parent;
			obs.attributes.putAll(attributes);
			obs.traits.addAll(traits);
			obs.types = types;
			obs.initColumns();
            nodes.put(id, obs);
            parent.observers.add(obs);
            return obs;
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

		public void append(SampleReader reader);
		
	}
	
	private static class BooleanColumn implements SampleColumn {
		
		private String name;
		private boolean[][] segments = new boolean[0][];
		private int blockSize = 1 << 20;
		private int size;
		
		public BooleanColumn(String name) {
			this.name = name;
		}
		
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
		
		public void append(boolean b) {
			int n = size++;
			int bucket = n / blockSize;
			if (bucket >= segments.length) {
				boolean[][] ns = Arrays.copyOf(segments, bucket + 1);
				ns[bucket] = new boolean[blockSize];
				synchronized (ns) {
					// memory barrier
					segments = ns;
				}
			}
			segments[n / blockSize][n % blockSize] = b;
		}
		
        @Override
        public void append(SampleReader reader) {
            append(reader.getBoolean(name));
        }		
	}

	private static class LongColumn implements SampleColumn {
		
		private String name;
		private long[][] segments = new long[0][];
		private int blockSize = 8 << 10;
		private int size;
		
        public LongColumn(String name) {
            this.name = name;
        }

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
		
        public void append(long l) {
            int n = size++;
            int bucket = n / blockSize;
            if (bucket >= segments.length) {
                long[][] ns = Arrays.copyOf(segments, bucket + 1);
                ns[bucket] = new long[blockSize];
                synchronized (ns) {
                    // memory barrier
                    segments = ns;
                }
            }
            segments[n / blockSize][n % blockSize] = l;
        }		
        
        @Override
        public void append(SampleReader reader) {
            append(reader.getLong(name));
        }        
	}

	private static class DoubleColumn implements SampleColumn {
		
		private String name;
		private double[][] segments = new double[0][];
		private int blockSize = 8 << 10;
		private int size;
		
        public DoubleColumn(String name) {
            this.name = name;
        }
		
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
		
		public void append(double d) {
            int n = size++;
            int bucket = n / blockSize;
            if (bucket >= segments.length) {
                double[][] ns = Arrays.copyOf(segments, bucket + 1);
                ns[bucket] = new double[blockSize];
                synchronized (ns) {
                    // memory barrier
                    segments = ns;
                }
            }
            segments[n / blockSize][n % blockSize] = d;
        }
		
        @Override
        public void append(SampleReader reader) {
            append(reader.getDouble(name));
        }		
	}

	private static class StringColumn implements SampleColumn {
		
		private String name;
		private String[][] segments = new String[0][];
		private int blockSize = 8 << 10;
		private int size;
		
		public StringColumn(String name) {
			this.name = name;
		}

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

		public void append(String s) {
            int n = size++;
            int bucket = n / blockSize;
            if (bucket >= segments.length) {
                String[][] ns = Arrays.copyOf(segments, bucket + 1);
                ns[bucket] = new String[blockSize];
                synchronized (ns) {
                    // memory barrier
                    segments = ns;
                }
            }
            segments[n / blockSize][n % blockSize] = s;
        }

		@Override
		public void append(SampleReader reader) {
			append(reader.getString(name));
		}
	}
}
