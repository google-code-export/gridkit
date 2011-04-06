package org.gridkit.coherence.txlite;

import org.gridkit.coherence.txlite.DirtyReadCacheAccessAdapter;
import org.gridkit.coherence.txlite.TxCacheWrapper;
import org.gridkit.coherence.txlite.TxManager;
import org.gridkit.coherence.txlite.TxSession;
import org.gridkit.coherence.txlite.TxSuperviser;
import org.gridkit.coherence.utils.classloader.IsolateTestRunner;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

@RunWith(IsolateTestRunner.class)
public class BasicTxLiteTest {

	static {
	    System.setProperty("tangosol.pof.enabled", "false");
//	    System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
	    System.setProperty("tangosol.coherence.cacheconfig", "tx-lite-test-cache-config.xml");
	}

	private static NamedCache txlog;
	private static TxSuperviser superviser = null;
	private static NamedCache cacheA;
	private static NamedCache cacheB;
	private static NamedCache cacheC;
	
	@Before
	public void init() {
		if (txlog == null) {
			txlog = CacheFactory.getCache("tx-lite-system-cache");
			superviser = new TxSuperviser(txlog);
			cacheA = CacheFactory.getCache("d-A");
			cacheB = CacheFactory.getCache("d-B");
			cacheC = CacheFactory.getCache("d-C");
		}
		txlog.clear();
		cacheA.clear();
		cacheB.clear();
		cacheC.clear();
	}
	
	@Test
	public void testReadThenWrite() {
		
		TxManager txman = new TxManager(superviser);
		
		TxSession readSession = txman.openReadOnlySession();
		NamedCache readA = readSession.connect(cacheA); 
		TxCacheWrapper dirtyA = new TxCacheWrapper(cacheA, new DirtyReadCacheAccessAdapter()); 
		
		TxSession writeSession = txman.openReadWriteSession();
		NamedCache writeA = writeSession.connect(cacheA);
		
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		Assert.assertThat(dirtyA.get("A"), IsNull.nullValue());
		Assert.assertThat(writeA.get("A"), IsNull.nullValue());
		
		writeA.put("A", "A");
		
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		Assert.assertThat((String)dirtyA.get("A"), Is.is("A"));
		Assert.assertThat((String)writeA.get("A"), Is.is("A"));
		
		writeSession.commit();
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		readSession.commit();
		Assert.assertThat((String)readA.get("A"), Is.is("A"));
	}

	@Test
	public void testReadThenWriteThenRollback() {
		
		TxManager txman = new TxManager(superviser);
		
		TxSession readSession = txman.openReadOnlySession();
		NamedCache readA = readSession.connect(cacheA); 
		TxCacheWrapper dirtyA = new TxCacheWrapper(cacheA, new DirtyReadCacheAccessAdapter()); 
		
		TxSession writeSession = txman.openReadWriteSession();
		NamedCache writeA = writeSession.connect(cacheA);
		
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		Assert.assertThat(dirtyA.get("A"), IsNull.nullValue());
		Assert.assertThat(writeA.get("A"), IsNull.nullValue());
		
		writeA.put("A", "A");
		
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		Assert.assertThat((String)dirtyA.get("A"), Is.is("A"));
		Assert.assertThat((String)writeA.get("A"), Is.is("A"));
		
		writeSession.rollback();
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		readSession.commit();
		Assert.assertThat(readA.get("A"), IsNull.nullValue());
		Assert.assertThat(dirtyA.get("A"), IsNull.nullValue());
		Assert.assertThat(writeA.get("A"), IsNull.nullValue());
		writeSession.rollback();
	}
	
}
