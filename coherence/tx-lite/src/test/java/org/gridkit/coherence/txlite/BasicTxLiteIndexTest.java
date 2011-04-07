package org.gridkit.coherence.txlite;

import java.io.Serializable;

import org.gridkit.coherence.utils.classloader.IsolateTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.EqualsFilter;

@RunWith(IsolateTestRunner.class)
public class BasicTxLiteIndexTest {

	static {
	    System.setProperty("tangosol.pof.enabled", "false");
//	    System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
	    System.setProperty("tangosol.coherence.cacheconfig", "tx-lite-test-cache-config.xml");
	}
	private static NamedCache cacheA;
	private static NamedCache cacheB;
	private static NamedCache cacheC;
	
	@Before
	public void init() {
		if (cacheA == null) {
			cacheA = CacheFactory.getCache("t-A");
			cacheB = CacheFactory.getCache("t-B");
			cacheC = CacheFactory.getCache("t-C");
		}
		((TxWrappedCache)cacheA).getVersionedCache().clear();
		((TxWrappedCache)cacheB).getVersionedCache().clear();
		((TxWrappedCache)cacheC).getVersionedCache().clear();
	}
	
	@AfterClass
	public static void tearDown() {
		if (cacheA != null) {
			cacheA.destroy();
			cacheB.destroy();
			cacheC.destroy();
		}
	}
	
	public void createIndex() {
		cacheA.addIndex(new FirstCharExtractor(), false, null);
	}
	
	@Test
	public void testFilterSearch() {
		TxManager txman = TxLite.getManager();
		TxSession writer = txman.openReadWriteSession();
		
		TxSession reader = txman.openReadOnlySession();
		NamedCache rrA = reader.connect(cacheA);
		NamedCache rcA = txman.toReadCommited(cacheA);
		NamedCache dirtyA = txman.toDirtyRead(cacheA);
		
		NamedCache writerA = writer.connect(cacheA);
		writerA.put("A", "A");
		writerA.put("B", "B");
		writerA.put("C", "C");
		writerA.put("D", "D");

		Filter f = new EqualsFilter(new FirstCharExtractor(), "A");
		
		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[]", rrA.keySet(f).toString());
		Assert.assertEquals("[]", rcA.keySet(f).toString());
		
		writer.commit();
		
		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[]", rrA.keySet(f).toString());
		Assert.assertEquals("[A]", rcA.keySet(f).toString());
		
		reader.commit();

		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[A]", rrA.keySet(f).toString());
		Assert.assertEquals("[A]", rcA.keySet(f).toString());
		
		writer.commit();
	}

	@Test
	public void testIndexFilterSearch() {
		
		cacheA.addIndex(new FirstCharExtractor(), false, null);
		
		TxManager txman = TxLite.getManager();
		TxSession writer = txman.openReadWriteSession();
		
		TxSession reader = txman.openReadOnlySession();
		NamedCache rrA = reader.connect(cacheA);
		NamedCache rcA = txman.toReadCommited(cacheA);
		NamedCache dirtyA = txman.toDirtyRead(cacheA);
		
		NamedCache writerA = writer.connect(cacheA);
		writerA.put("A", "A");
		writerA.put("B", "B");
		writerA.put("C", "C");
		writerA.put("D", "D");
		
		FirstCharExtractor extractor = new FirstCharExtractor();
		Filter f = new EqualsFilter(extractor, "A");
		
		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[]", rrA.keySet(f).toString());
		Assert.assertEquals("[]", rcA.keySet(f).toString());
		
		writer.commit();
		
		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[]", rrA.keySet(f).toString());
		Assert.assertEquals("[A]", rcA.keySet(f).toString());
		
		reader.commit();
		
		Assert.assertEquals("[A]", writerA.keySet(f).toString());
		Assert.assertEquals("[A]", dirtyA.keySet(f).toString());
		Assert.assertEquals("[A]", rrA.keySet(f).toString());
		Assert.assertEquals("[A]", rcA.keySet(f).toString());
		
		writer.commit();
	}
	
	public static class FirstCharExtractor implements ValueExtractor, Serializable {
		
		boolean dirty = false;
		
		@Override
		public Object extract(Object obj) {
			dirty = true;
			return new String(new char[]{((String)obj).charAt(0)});
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}
	}
}
