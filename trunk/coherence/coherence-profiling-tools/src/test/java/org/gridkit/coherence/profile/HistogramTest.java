package org.gridkit.coherence.profile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HistogramTest {

	@Test
	public void testAddValueSimple() {
		Histogram hist = new Histogram(1, 1, 100, 10);
		hist.addSample(11);
		hist.updateStats();
		assertEquals(11, (long)hist.getMax());
		assertEquals(1, (long)hist.getCount());
		assertEquals(11, (long)hist.getTotal());
		assertEquals(11, (long)hist.getAvg());
	}
	
	@Test
	public void testAddValueScaled() {
		Histogram hist = new Histogram(2, 1, 100, 10);
		hist.addSample(11);
		hist.updateStats();
		assertEquals(5, (long)hist.getMax());
		assertEquals(1, (long)hist.getCount());
		assertEquals(5, (long)hist.getTotal());
		assertEquals(5, (long)hist.getAvg());
	}

}
