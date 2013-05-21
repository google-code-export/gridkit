package org.gridkit.lab.mcube;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.common.netty.util.internal.ConcurrentHashMap;
import org.gridkit.lab.tentacle.Sample;
import org.gridkit.lab.tentacle.SampleSink;
import org.gridkit.lab.tentacle.Samples;
import org.gridkit.lab.tentacle.SourceInfo;

public class NaiveSampleStore implements DirectQuery {

	
	private ConcurrentMap<Class<?>, SampleSchema> schemas = new ConcurrentHashMap<Class<?>, NaiveSampleStore.SampleSchema>();
	private Source root = newRootSource();
	private SampleSinkAdapter rootAdapter = new SampleSinkAdapter(root);
	
	public SampleSink getRoot() {
		return rootAdapter;
	}
	
	public Cube getCube() {
		
	}
	
	@Override
	public Iterator<Row> query(Cube cube, Value... values) {
		return null;
	}

	@Override
	public Iterator<Row> query(Cube cube, List<Value> values, List<Value> sortOrder) {
		return null;
	}

	private Source newRootSource() {
		Source source = new Source();
		source.id = "ROOT.";
		return source;
	}


	private class Source {
		
		private String id;
		private Source parent;
		
		private List<Source> children = new ArrayList<Source>();
		
		private SSample sourceDescription;
		private SampleTable staticSamples = new SampleTable();
		
		private SampleTable dynamicSamples = new SampleTable();
		
	}
	
	private static class SSample {
		
		private Source source;
		private SampleSchema schema;
		private double captureTimestamp;
		
		private Object[] value;		
	}
	
	private class SampleTable {
		
		List<SSample> samples = new ArrayList<SSample>(); 
		
		public void add(SSample sample) {
			samples.add(sample);
		}
	}
	
	private static class SampleSchema {
		
		private Set<Class<?>> types;
		private Map<Method, Integer> fields;
		private Method[] methods;
		
		public SampleSchema(Class<?> type) {
			types = new HashSet<Class<?>>();
			fields = new HashMap<Method, Integer>();
			collectSampleDefinitions(types, type);
			List<Method> ms = new ArrayList<Method>();
			for(Method m: type.getMethods()) {
				if (isSampleAttribute(m)) {
					ms.add(m);
				}
			}
			methods = ms.toArray(new Method[ms.size()]);
			Map<String, Integer> names = new HashMap<String, Integer>(); 
			for(int i = 0; i != methods.length; ++i) {
				names.put(methods[i].getName(), i);
				fields.put(methods[i], i);
			}
			for(Class<?> cc: types) {
				for(Method m: cc.getDeclaredMethods()) {
					if (isSampleAttribute(m)) {
						int idx = names.get(m.getName());
						fields.put(m, idx);
					}
				}
			}
		}

		private boolean isSampleAttribute(Method m) {
			if (m.getParameterTypes().length == 0 && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
				if (Sample.class.isAssignableFrom(m.getDeclaringClass())) {
					return true;
				}
			}
			return false;
		}
		
		private void collectSampleDefinitions(Set<Class<?>> types, Class<?> type) {
			if (type.isInterface())  {
				types.add(type);
			}
			for(Class<?> i: type.getInterfaces()) {
				if (Sample.class.isAssignableFrom(i)) {
					collectSampleDefinitions(types, i);
				}
			}
			Class<?> s = type.getSuperclass();
			if (s != null && Sample.class.isAssignableFrom(s)) {
				collectSampleDefinitions(types, s);
			}
		}

		public Object[] capture(Object sample) {
			Object[] data = new Object[methods.length];
			for(int i = 0; i != data.length; ++i) {
				try {
					data[i] = methods[i].invoke(sample);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return data;
		}
	}	
	
	private SSample readSample(Object sample) {
		SampleSchema schema = getSchemaFor(sample.getClass());
		Object[] data = schema.capture(sample);
		SSample ssample = new SSample();
		ssample.schema = schema;
		ssample.captureTimestamp = Samples.wallclockTime();
		ssample.value = data;
		return ssample;
	}
	
	private SampleSchema getSchemaFor(Class<? extends Object> cls) {
		SampleSchema schema = schemas.get(cls);
		if (schema == null) {
			schemas.putIfAbsent(cls, new SampleSchema(cls));
			schema = schemas.get(cls);
		}
		return schema;
	}


	private class SampleSinkAdapter implements SampleSink {

		private Source source;
		
		public SampleSinkAdapter(Source source) {
			this.source = source;
		}
		
		@Override
		public SampleSink newChildSink(String sourceId, SourceInfo description) {
			Source child = new Source();
			child.id = source.id + sourceId + ".";
			child.sourceDescription = readSample(description);
			source.children.add(child);
			return new SampleSinkAdapter(child);
		}

		@Override
		public <S extends Sample> void annotate(S sample) {
			SSample s = readSample(sample);
			s.source = source;
			source.staticSamples.add(s);
		}

		@Override
		public <S extends Sample> void send(S sample) {
			SSample s = readSample(sample);
			s.source = source;
			source.dynamicSamples.add(s);
		}

		@Override
		public <S extends Sample> void send(S sample, double timestamp) {
			SSample s = readSample(sample);
			s.source = source;
			s.captureTimestamp = timestamp;
			source.dynamicSamples.add(s);
		}
	}
}
