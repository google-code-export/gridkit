package org.gridkit.lab.zktest;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.gridkit.nimble.metering.Measure;
import org.gridkit.nimble.metering.SampleReader;
import org.gridkit.nimble.metering.SamplerBuilder;
import org.gridkit.nimble.pivot.Filters;
import org.junit.Test;

public class PlotRun {

	@Test
	public void read_csv() throws IOException {
		
		SampleReader read = new SievedReader(new CsvSampleReader(new FileReader("raw-local.csv")) 
				, Filters.equals(Measure.PRODUCER, "USER")
				, Filters.equals(SamplerBuilder.OPERATION, "Read (%s)"));

		SampleReader write = new SievedReader(new CsvSampleReader(new FileReader("raw-local.csv")) 
				, Filters.equals(Measure.PRODUCER, "USER")
				, Filters.equals(SamplerBuilder.OPERATION, "Write (%s)"));
		
		extractTimeline(read, 1, "reads.dat");
		extractTimeline(write, 1, "writes.dat");
		
		System.out.println("Done");
	}

	protected void extractTimeline(SampleReader source, double step, String filename) throws IOException {
		DataSliceExtractor extractor = new DataSliceExtractor();
		
		extractor.extract(Measure.TIMESTAMP);
		extractor.extract(Measure.DURATION);

		extractor.feed(source);
		extractor.sort(0);
		extractor.shift(0, -extractor.getData().get(0)[0]);
		
		Writer writer;
		writer = new FileWriter(filename);
		
		Resampler resampler = new Resampler(step);
		resampler.add(Resampler.MEAN);
		resampler.add(Resampler.STD_DEV);
		resampler.add(Resampler.CONFIDENCE_99_LOWER);
		resampler.add(Resampler.CONFIDENCE_99_HIGHER);
		resampler.add(Resampler.PERC75);
		resampler.add(Resampler.MAX);
		
		resampler.feed(extractor.getData());
		
		for(double[] row: resampler.getData()) {
			for(double v: row) {
				writer.append(String.valueOf(v)).append(' ');
			}
			writer.append('\n');
		}
		
		writer.close();
	}

}
