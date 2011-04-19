package org.gridkit.coherence.offheap.storage.memlog;

import org.junit.Test;

import junit.framework.Assert;

public class SplitHashTest {

	@Test
	public void test1() {
		Assert.assertEquals(511, PagedMemoryBinaryStoreManager.splitHash(-1, 512));
	}

	@Test
	public void test2() {
		Assert.assertEquals(510, PagedMemoryBinaryStoreManager.splitHash(-2, 512));
	}

	@Test
	public void test3() {
		Assert.assertEquals(0, PagedMemoryBinaryStoreManager.splitHash(0, 512));
	}

	@Test
	public void test4() {
		Assert.assertEquals(1, PagedMemoryBinaryStoreManager.splitHash(1, 512));
	}

	@Test
	public void test5() {
		Assert.assertEquals(511, PagedMemoryBinaryStoreManager.splitHash(511, 512));
	}

	@Test
	public void test6() {
		Assert.assertEquals(255, PagedMemoryBinaryStoreManager.splitHash(511, 511));
	}

	@Test
	public void test7() {
		Assert.assertEquals(256, PagedMemoryBinaryStoreManager.splitHash(256, 511));
	}

	@Test
	public void test8() {
		Assert.assertEquals(256, PagedMemoryBinaryStoreManager.splitHash(-256, 511));
	}
}
