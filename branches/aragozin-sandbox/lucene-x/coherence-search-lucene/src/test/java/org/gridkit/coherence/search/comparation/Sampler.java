package org.gridkit.coherence.search.comparation;

public class Sampler {

	private double scale = 1;
	private long count = 0;
	private double sum = 0;
	private double sqSum = 0;
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public void add(long value) {
		++count;
		sum += value;
		double avg = ((double)sum) / count;
		sqSum += (avg - value) * (avg - value);
	}
	
	public String asString() {
		if (count == 0) {
			return "no data";
		}
		double avg = scale * ((double)sum) / count;
		double stdDev = scale * Math.sqrt(sqSum / (count - 1));
		return String.format("%.3f[%.3f]", avg, stdDev);
	}
	
	public void reset() {
		count = 0;
		sum = 0;
		sqSum = 0;
	}	
}
