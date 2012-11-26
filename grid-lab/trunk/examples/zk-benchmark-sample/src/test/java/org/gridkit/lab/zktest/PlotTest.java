package org.gridkit.lab.zktest;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import org.gridkit.nimble.metering.Measure;
import org.gridkit.nimble.metering.SamplerBuilder;
import org.junit.Test;

public class PlotTest {

	@Test
	public void read_csv() throws IOException {
		CsvSampleReader reader = new CsvSampleReader(new FileReader("raw-local.csv"));
		
		DataSliceExtractor extractor = new DataSliceExtractor();
		
		extractor.filter(Measure.PRODUCER, "USER");
		extractor.filter(SamplerBuilder.OPERATION, "Read (%s)");
		
		extractor.extract(Measure.TIMESTAMP);
		extractor.extract(Measure.DURATION);

		extractor.feed(reader);
		extractor.sort(0);
		
		Writer writer;
		writer = new FileWriter("read.dat");
		
		for(double[] row: extractor.getData()) {
			for(double v: row) {
				writer.append(String.valueOf(v)).append(' ');
			}
			writer.append('\n');
		}
		
		writer.close();
		System.out.println("Done");
	}

}
