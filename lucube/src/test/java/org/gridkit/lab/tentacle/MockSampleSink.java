package org.gridkit.lab.tentacle;

public class MockSampleSink implements SampleSink {

	private String id;
	private String name;
	
	public MockSampleSink(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public SampleSink newChildSink(String sourceId, SourceInfo description) {
		String nid = id + sourceId;
		String nname = name + toString(description);
		System.out.println(nname + " <NEW>");
		return new MockSampleSink(nid, nname);
	}

	@Override
	public <S extends Sample> void send(S sample) {
		System.out.println(name + " " + toString(sample));		
	}

	@Override
	public <S extends Sample> void send(S sample, double timestamp) {
		send(sample);
	}
	
	private String toString(Sample c) {
		Class<?> ri = getRootDeclaration(c.getClass());
		SampleSchema ss = new SampleSchema(c.getClass());
		StringBuilder sb = new StringBuilder();
		sb.append(ri.getSimpleName());
		sb.append("[");
		Object[] vals = ss.extract(c);
		for(int i = 0; i != vals.length; ++i) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(ss.getMethods().get(i).getName());
			sb.append("=").append(vals[i]);
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	private Class<?> getRootDeclaration(Class<?> s) {
		if (!Sample.class.isAssignableFrom(s)) {
			throw new IllegalArgumentException(s.getName() + " is not a sample class");			
		}
		else if (s.isInterface()) {
			return s;
		}
		else {
			for(Class<?> c: s.getInterfaces()) {
				if (Sample.class.isAssignableFrom(s)) {
					return c;
				}
			}
		}
		throw new Error("Impossible");
	}
}
