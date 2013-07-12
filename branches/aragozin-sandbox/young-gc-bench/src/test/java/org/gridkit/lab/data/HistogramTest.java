package org.gridkit.lab.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.xeiam.xchart.SeriesMarker;

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
		
		byte[] data = new byte[256 << 20];
		new Random().nextBytes(data);

		MemAccessBenchmarker b = new MemAccessBenchmarker();
		b.init("warm-up", data, 32 << 20, 1000);
		
		for(int i = 0; i != 2; ++i) {
			b.measure(10);
		}
		System.gc();
		System.gc();
		System.gc();
		Thread.sleep(1000);
		
		List<MemAccessBenchmarker> lines = new ArrayList<HistogramTest.MemAccessBenchmarker>();
		lines.add(b = new MemAccessBenchmarker());
		b.init("256 MiB, 100", data, 256 << 20, 100);
		lines.add(b = new MemAccessBenchmarker());
		b.init("256 MiB, 400", data, 256 << 20, 400);
		lines.add(b = new MemAccessBenchmarker());
		b.init("256 MiB, 1000", data, 256 << 20, 1000);
		lines.add(b = new MemAccessBenchmarker());
		b.init("256 MiB, 4000", data, 256 << 20, 4000);
		lines.add(b = new MemAccessBenchmarker());
		b.init("256 MiB, 10000", data, 256 << 20, 10000);

		for(int i = 0; i != 20; ++i) {
			System.out.println("Iteration " + i);
			for(MemAccessBenchmarker mb: lines) {
				mb.measure(10);
			}
		}
		
		for(MemAccessBenchmarker mb: lines) {
			mb.addToChart(bg);
		}
		
		bg.setMarker("main", SeriesMarker.NONE);
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
			acc += bench.measure(spree, block);
			qfinder.addAllOf(new DoubleArrayList(block));			
		}
		
		DoubleArrayList x = DoubleQuantileFinderFactory.newEquiDepthPhis(100);
		DoubleArrayList y = qfinder.quantileElements(x);
		gb.addSeries("main", name, x, y);
		
		return acc;
	}
	
	public static class MemAccessBenchmarker {
		
		String name;
		public int acc;
		byte[] data;		
		int limit;
		int population;
		DoubleQuantileFinder qfinder = DoubleQuantileFinderFactory.newDoubleQuantileFinder(false, Long.MAX_VALUE, 0.01, 0.0001, 100, new DRand());
		double[] dataSink = new double[1000000];
		
		public void init(String name, byte[] data, int limit, int population) {
			this.name = name;
			this.data = data;
			this.limit = limit;
			this.population = population;
		}
		
		public void measure(int spree) {
			fillSink(spree);
			fillSink(spree);
			qfinder.addAllOf(new DoubleArrayList(dataSink));
		}
		
		public void addToChart(GraphBuilder bg) {
			DoubleArrayList x = DoubleQuantileFinderFactory.newEquiDepthPhis(100);
			DoubleArrayList y = qfinder.quantileElements(x);
			bg.addSeries("main", name, x, y);
		}
		
		private void fillSink(int spree) {
			int acc = 0;
			Random rnd = new Random();
			long n = 0;
			for(int i = 0; i != dataSink.length; ++i) {
				long s = System.nanoTime();
				n = (n + 1) % population;
				for(int j = 0; j != spree; ++j) {
					rnd.setSeed(n);
					acc += data[Math.abs((rnd.nextInt() * 4)) % limit];
				}
				dataSink[i] = System.nanoTime() - s;
			}
			this.acc += acc;
		}
	}
	
}
