package org.gridkit.lab.zktest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class Resampler {

	public static final Assessment MEAN = new Univariate(new Mean());
	public static final Assessment STD_DEV = new Univariate(new StandardDeviation());
	public static final Assessment MAX = new Univariate(new Max());
	public static final Assessment MIN = new Univariate(new Min());
	public static final Assessment PERC75 = new Univariate(new Percentile(75));
	public static final Assessment CONFIDENCE_99_HIGHER = new Conf(0.99, true);
	public static final Assessment CONFIDENCE_99_LOWER = new Conf(0.99, false);
	
	private final double step;
	
	private List<double[]> results = new ArrayList<double[]>();
	private List<Assessment> assessments = new ArrayList<Resampler.Assessment>();
	
	public Resampler(double step) {
		this.step = step;
	}
	
	public void add(Assessment assessment) {
		assessments.add(assessment);
	}
	
	public List<double[]> getData() {
		return results;
	}
	
	public void feed(List<double[]> data) {
		double start = data.get(0)[0];
		double end = start + step;
		int s = 0;
		while(true) {
			if (s >= data.size()) {
				break;
			}
			int e;
			for(e = s; e < data.size(); ++e) {
				if (data.get(e)[0] >= end) {
					break;
				}
			}
			if (e + 2 <= s) {
				// at least 2 samples should be present
				break;
			}
			else {
				double[] slice = new double[e - s];
				for(int i = 0; i != slice.length; ++i) {
					slice[i] = data.get(s + i)[1];
				}
				double t1 = start;
				double t2 = end;
				s = e;
				start = end;
				end = start + step;
				double[] as = new double[1 + assessments.size()];
				as[0] = t1;
				for(int i = 0; i != assessments.size(); ++i) {
					as[1 + i] = assessments.get(i).evaluate(slice, t1, t2);
				}
				results.add(as);
			}
		}
	}	
	
	public interface Assessment {
		
		public double evaluate(double[] samples, double t1, double t2);
		
	}

	public static class Univariate implements Assessment {

		private final UnivariateStatistic stat;
		
		private Univariate(UnivariateStatistic stat) {
			this.stat = stat;
		}

		@Override
		public double evaluate(double[] samples, double t1, double t2) {
			return stat.evaluate(samples);
		}
	}
	
	public static class Conf implements Assessment {

		private final double significance;
		private final boolean upper;
		
		private Conf(double significance, boolean upper) {
			this.significance = significance;
			this.upper = upper;
		}

		@Override
		public double evaluate(double[] samples, double t1, double t2) {
			SummaryStatistics ss = new SummaryStatistics();
			for(double s: samples) {
				ss.addValue(s);
			}
			double ci = getConfidenceIntervalWidth(ss, significance);
			return upper ? ss.getMean() + ci / 2 : ss.getMean() - ci / 2;
		}
		
		private double getConfidenceIntervalWidth(SummaryStatistics summaryStatistics, double significance) {
			TDistribution tDist = new TDistribution(summaryStatistics.getN() - 1);
			double a = tDist.inverseCumulativeProbability(1.0 - significance / 2);
			return a * summaryStatistics.getStandardDeviation()
					/ Math.sqrt(summaryStatistics.getN());
		}		
	}
}
