package org.gridkit.lab.data;

import java.util.Random;

import org.junit.Test;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.jet.random.tdouble.engine.DRand;
import cern.jet.stat.tdouble.quantile.DoubleQuantileFinder;
import cern.jet.stat.tdouble.quantile.DoubleQuantileFinderFactory;

public class HistogramTest {

	@Test
	public void calculatePerc() {
		DoubleQuantileFinder qfinder = DoubleQuantileFinderFactory.newDoubleQuantileFinder(false, Long.MAX_VALUE, 0.01, 1e-5, 20, new DRand());

		Random rnd = new Random();
		for(int i = 0; i != 1e4; ++i) {
			qfinder.add(rnd.nextGaussian());
		}
		
		GraphBuilder bg = new GraphBuilder();
		DoubleArrayList x = DoubleQuantileFinderFactory.newEquiDepthPhis(20);
		DoubleArrayList y = qfinder.quantileElements(x);
		bg.addSeries("main", "1e4", x, y);
		
		for(int i = 0; i != 1e6; ++i) {
			qfinder.add(rnd.nextGaussian());
		}

		x = DoubleQuantileFinderFactory.newEquiDepthPhis(20);
		y = qfinder.quantileElements(x);
		bg.addSeries("main", "1e6", x, y);

		for(int i = 0; i != 1e8; ++i) {
			qfinder.add(rnd.nextGaussian());
		}
		
		x = DoubleQuantileFinderFactory.newEquiDepthPhis(20);
		y = qfinder.quantileElements(x);
		bg.addSeries("main", "1e8", x, y);
		
		bg.show();
	}

	
	@Test
	public void calculateMemAccess() throws InterruptedException {
		
		GraphBuilder bg = new GraphBuilder();
		MemAccessBench bench64 = new MemAccessBench();
		bench64.initUnallignedByteMode(new Random(0), 128 << 20, 100);

		MemAccessBench bench256 = new MemAccessBench();
		bench256.initUnallignedByteMode(new Random(0), 256 << 20, 100);

		for(int i = 0; i != 1000; ++i) {
			bench64.measure(100, new double[10000]);
		}
		System.gc();
		System.gc();
		System.gc();
		Thread.sleep(1000);
		
		addMemAccessSerie(bg, "128 MiB, bytes, 1000, 10", bench64, 10);
		addMemAccessSerie(bg, "256 MiB, bytes, 1000, 10", bench256, 10);
		addMemAccessSerie(bg, "128 MiB, bytes, 1000, 10 [2]", bench64, 10);
		addMemAccessSerie(bg, "256 MiB, bytes, 1000, 10 [2]", bench256, 10);
		
		bg.show();
	}
	
	private int addMemAccessSerie(GraphBuilder gb, String name, MemAccessBench bench, int spree) {
		int acc = 0;
		int rs = 10000;
		int nr = 20;
		
		DoubleQuantileFinder qfinder = DoubleQuantileFinderFactory.newDoubleQuantileFinder(true, rs * nr, 0.01, 1e-5, 100, new DRand());
		double[] block = new double[rs];
		
		for(int i = 0; i != nr; ++i) {
			acc += bench.measure(spree, block);
			qfinder.addAllOf(new DoubleArrayList(block));			
		}
		
		DoubleArrayList x = DoubleQuantileFinderFactory.newEquiDepthPhis(100);
		DoubleArrayList y = qfinder.quantileElements(x);
		gb.addSeries("main", name, x, y);
		
		return acc;
	}
	
	private double sqr(double v) {
		return v * v;
	}
	
}
