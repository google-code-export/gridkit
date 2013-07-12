package org.gridkit.benchmark.gc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.gridkit.lab.data.Sample;
import org.gridkit.lab.data.SampleCSVReader;
import org.gridkit.lab.data.SampleCSVWriter;

public class FilePersitedStrategy implements SweepStrategy {

	private String path;
	private SweepStrategy strategy;
	
	public FilePersitedStrategy(SweepStrategy strategy, String path) {
		this.strategy = strategy;
		this.path = path;
		try {
			List<Sample> existing = SampleCSVReader.read(path);
			for(Sample s: existing) {
				strategy.notifyResult(s);
			}
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Sample nextDataPoint() {
		return strategy.nextDataPoint();
	}

	@Override
	public synchronized void notifyResult(Sample sample) {
		try {
			SampleCSVWriter.append(path, Collections.singleton(sample));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		strategy.notifyResult(sample);
	}
	
	
	
}
