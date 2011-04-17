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
	
	@Test
	public void testPercentile() {
		Histogram hist = new Histogram(1, 0, 100, 100);
		
		for (int i = 0; i < 99; ++i) {
			hist.addSample(90);
		}
		
		double p95 = hist.getApproximatePercentile95();
		assertEquals(91, (int)p95);
		double p99 = hist.getApproximatePercentile99();
		assertEquals(91, (int)p99);
		double p999 = hist.getApproximatePercentile999();
		assertEquals(91, (int)p999);
		
		for (int i = 0; i < 5; ++i) {
			hist.addSample(99);
		}
		
		p95 = hist.getApproximatePercentile95();
		assertEquals(91, (int)p95);
		p99 = hist.getApproximatePercentile99();
		assertEquals(100, (int)p99);
		p999 = hist.getApproximatePercentile999();
		assertEquals(100, (int)p999);
		
		for (int i = 0; i < 5; ++i) {
			hist.addSample(99);
		}
		
		p95 = hist.getApproximatePercentile95();
		assertEquals(100, (int)p95);
		p99 = hist.getApproximatePercentile99();
		assertEquals(100, (int)p99);
		p999 = hist.getApproximatePercentile999();
		assertEquals(100, (int)p999);
		
		for (int i = 0; i < 100000; ++i) {
			hist.addSample(1);
		}
		
		p95 = hist.getApproximatePercentile95();
		assertEquals(2, (int)p95);
		p99 = hist.getApproximatePercentile99();
		assertEquals(2, (int)p99);
		p999 = hist.getApproximatePercentile999();
		assertEquals(91, (int)p999);
	}
}
