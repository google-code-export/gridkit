package org.gridkit.coherence.profile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HistogramTest {

	@Test
	public void testAddValueSimple() {
		Histogram hist = new Histogram(1, 1, 100, 10);
		hist.addSample(11);
		hist.addSample(9);
		hist.updateStats();
		assertEquals(11, (long)hist.getMax());
		assertEquals(2, (long)hist.getCount());
		assertEquals(20, (long)hist.getTotal());
		assertEquals(10, (long)hist.getAvg());
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

	@Test
	public void testClone() {
		Histogram hist = new Histogram(1, 1, 100, 10);
		hist.addSample(11);
		hist.addSample(9);
		
		Histogram hist2 = hist.clone();
		hist2.updateStats();
		
		assertEquals(11, (long)hist2.getMax());
		assertEquals(2, (long)hist2.getCount());
		assertEquals(20, (long)hist2.getTotal());
		assertEquals(10, (long)hist2.getAvg());
	}
	
	@Test
	public void testAddHistogram() {
		Histogram hist = new Histogram(1, 1, 100, 10);
		hist.addSample(11);
		hist.addSample(9);
		
		Histogram hist2 = hist.clone();
		hist2.addSample(15);
		hist.addHistogram(hist2);
		hist.updateStats();
		
		assertEquals(15, (long)hist.getMax());
		assertEquals(5, (long)hist.getCount());
		assertEquals(55, (long)hist.getTotal());
		assertEquals(11, (long)hist.getAvg());
	}
	
	@Test
	public void testReset() {
		Histogram hist = new Histogram(1, 1, 100, 10);
		hist.addSample(11);
		hist.addSample(9);
		
		assertEquals(11, (long)hist.getMax());
		
		hist.reset();
		assertEquals(0, (long)hist.getMax());
	}
}
