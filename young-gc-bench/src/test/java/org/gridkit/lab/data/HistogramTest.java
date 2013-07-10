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

	private double sqr(double v) {
		return v * v;
	}
	
}
