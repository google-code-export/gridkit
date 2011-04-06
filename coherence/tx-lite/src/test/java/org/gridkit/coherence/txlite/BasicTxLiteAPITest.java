package org.gridkit.coherence.txlite;

import org.gridkit.coherence.txlite.TxLite;
import org.gridkit.coherence.txlite.TxManager;
import org.gridkit.coherence.txlite.TxSession;
import org.gridkit.coherence.utils.classloader.IsolateTestRunner;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

@RunWith(IsolateTestRunner.class)
public class BasicTxLiteAPITest {

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
	
	@Test
	public void testReadThenWrite() {
		
		TxManager txman = TxLite.getManager();
		
		TxSession rsession = txman.openReadOnlySession();
		TxSession wsession = txman.openReadWriteSession();
		NamedCache writeA = wsession.connect(cacheA);
		NamedCache rrA = rsession.connect(cacheA);
		NamedCache rcA = txman.toReadCommited(cacheA);

		Assert.assertThat(rrA.get("A"), IsNull.nullValue());
		Assert.assertThat(rcA.get("A"), IsNull.nullValue());
		Assert.assertThat(cacheA.get("A"), IsNull.nullValue());
		Assert.assertThat(writeA.get("A"), IsNull.nullValue());
		
		writeA.put("A", "A");
	
		Assert.assertThat(rrA.get("A"), IsNull.nullValue());
		Assert.assertThat(rcA.get("A"), IsNull.nullValue());
		Assert.assertThat((String)cacheA.get("A"), Is.is("A"));
		Assert.assertThat((String)writeA.get("A"), Is.is("A"));

		wsession.commit();
		
		Assert.assertThat(rrA.get("A"), IsNull.nullValue());
		Assert.assertThat((String)rcA.get("A"), Is.is("A"));

		rsession.commit();

		Assert.assertThat((String)rrA.get("A"), Is.is("A"));
	}
}
