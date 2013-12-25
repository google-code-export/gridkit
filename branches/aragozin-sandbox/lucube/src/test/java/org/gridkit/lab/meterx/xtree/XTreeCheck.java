package org.gridkit.lab.meterx.xtree;

import java.util.NoSuchElementException;

import org.gridkit.lab.meterx.EntityAppender;
import org.gridkit.lab.meterx.EntityNode;
import org.gridkit.lab.meterx.ObserverAppender;
import org.gridkit.lab.meterx.ObserverNode;
import org.gridkit.lab.meterx.SampleReader;
import org.junit.Test;

public class XTreeCheck {

	@Test
	public void buildXTree() {
		
		XTree tree = new XTree();
		EntityAppender ea = tree.getAppender().addChildEntity("h0");
		ea.addAttribute("hostname", "host0");
		ea.addTrait("host");
		ea.metaDone();
		ObserverAppender oa = ea.addChildObserver("host0.0");
		oa.addField("timestamp", double.class);
		oa.addField("value", double.class);
		oa.addAttribute("class", "Test");
		oa.addTrait("Test");
		oa.metaDone();
		
		SimpleSample ss = new SimpleSample();
		for(int i = 0; i != 10; ++i) {
			ss.arm(i, 10 * i);
			oa.importSamples(ss);
		}
		
		new String();
	}
	
	public static class SimpleSample implements SampleReader {
		
		double timestamp;
		double value;
		boolean armed;

		public void arm(double timestamp, double value) {
			this.timestamp = timestamp;
			this.value = value;
			this.armed = true;
		}
		
		@Override
		public ObserverNode getObserverNode() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean isReady() {
			return armed;
		}
		
		@Override
		public boolean next() {
			armed = false;
			return false;
		}
		
		@Override
		public Object get(String field) {
			return getDouble(field);
		}
		
		@Override
		public boolean getBoolean(String field) {
			throw new IllegalArgumentException();
		}
		
		@Override
		public long getLong(String field) {
			throw new IllegalArgumentException();
		}
		
		@Override
		public double getDouble(String field) {
			if ("timestamp".equals(field)) {
				return timestamp;
			}
			if ("value".equals(field)) {
				return value;
			}
			throw new NoSuchElementException("No such field [" + field + "]");
		}

		@Override
		public String getString(String field) {
			throw new IllegalArgumentException();
		}
	}
}
