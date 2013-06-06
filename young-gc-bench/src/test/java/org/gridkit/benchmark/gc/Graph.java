package org.gridkit.benchmark.gc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JDialog;

import org.junit.Test;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.ChartTheme;
import com.xeiam.xchart.StyleManager.ChartType;
import com.xeiam.xchart.XChartPanel;

public class Graph {

	public SampleList readSamples(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		List<Map<String, Object>> samples = new ArrayList<Map<String,Object>>();
		while(true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			String[] fields = line.split("[,]");
			Map<String, Object> sample = new LinkedHashMap<String, Object>();
			sample.put("tag", fields[0]);
			sample.put("jvm", fields[1]);
			sample.put("algo", fields[2]);
			sample.put("size", Double.parseDouble(fields[3]) / 1024);
			sample.put("threads", Double.parseDouble(fields[4]));
			sample.put("mean", Double.parseDouble(fields[5]));
			sample.put("stdDev", Double.parseDouble(fields[6]));
			sample.put("count", Integer.parseInt(fields[7]));
			samples.add(sample);
		}
		return new SampleList(samples);
	}
	
	@Test
	public void test_sample_reader() throws IOException {
		SampleList readSamples = readSamples("gcrep.1076.txt");
		System.out.println(readSamples.sort("size", "threads", "algo", "jvm"));
		System.out.println("Medians");
		System.out.println(readSamples.sort("size", "threads", "algo", "jvm").filterMedian("size", "threads", "algo", "jvm"));
		System.out.println("Sieved");
		System.out.println(readSamples.filter("jvm", "hs6u43").filter("threads", 1d));
	}
	
	@Test
	public void showThreadsGraph() throws IOException {
		showThreadSeries(readSamples("gcrep.1076.txt"), "hs6u43", range(4, 50));
	}

	@Test
	public void showSizeGraph() throws IOException {
		SampleList data = readSamples("gcrep.1076.txt");
		data = data.filter("threads", 8, 100);
		showSizeSeries(data, "hs6u43");
	}

	@Test
	public void showNormalizedSizeGraph() throws IOException {
		SampleList data = readSamples("gcrep.1076.txt");
//		data = data.filter("threads", 1, 16);
//		showNormalizedSizeSeries(data, "hs6u43", 8);
		showNormalizedSizeSeries(data, "hs7u15", 8);
	}
	
	private int[] range(int l, int h) {
		int[] array = new int[h - l + 1];
		for(int i = 0; i != array.length; ++i) {
			array[i] = l + i;
		}
		return array;
	}

	private boolean has(int[] vals, int val) {
		for(int n: vals) {
			if (n == val) {
				return true;
			}
		}
		return false;
	}
	
	public void showThreadSeries(SampleList samples, String jvm, int... threads) throws IOException {
		// Create Chart
		Chart chart = new ChartBuilder()
			.width(800).height(600)
			.theme(ChartTheme.Matlab)
			.title("Young GC pause mean [" + jvm + ", CMS]")
			.xAxisTitle("Old space [GiB]").yAxisTitle("Pause mean [ms]")
		.build();
		
		chart.getStyleManager().setPlotGridLinesVisible(true);
		chart.getStyleManager().setChartType(ChartType.Line);
		 
		SampleList series;
		
		samples = samples.filter("jvm", jvm);
		
		if (has(threads, 0)) {
			series = newThreadSeries(samples, jvm, "Serial", 1);
			chart.addSeries("Serial", series.numericSeries("size"), series.numericSeries("mean"));
		}

		samples = samples.filter("algo", "ParNew");
		
		for(Object t: new TreeSet<Object>(samples.groupBy("threads").keySet())) {
			int tc = ((Number)t).intValue();
			if (has(threads, tc)) {
				series = newThreadSeries(samples, jvm, "ParNew", tc);
				Series ser = chart.addSeries("pt=" + tc, series.numericSeries("size"), series.numericSeries("mean"));
				ser.setMarker(SeriesMarker.CIRCLE);
			}
		}

		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.show();
	}

	public void showSizeSeries(SampleList samples, String jvm) throws IOException {
		// Create Chart
		Chart chart = new ChartBuilder()
			.width(800).height(600)
			.theme(ChartTheme.Matlab)
			.title("Young GC pause mean [" + jvm + ", CMS]")
			.xAxisTitle("Parallel threads").yAxisTitle("Pause mean [ms]")
		.build();
		
		chart.getStyleManager().setPlotGridLinesVisible(true);
		chart.getStyleManager().setChartType(ChartType.Line);
		 
		SampleList series;
		
		samples = samples.filter("jvm", jvm);
		
		samples = samples.filter("algo", "ParNew");
		
		for(Object t: new TreeSet<Object>(samples.groupBy("size").keySet())) {
			double size = ((Number)t).intValue();
			series = newSizeSeries(samples, jvm, "ParNew", size);
			Series ser = chart.addSeries("" + size + " [GiB]", series.numericSeries("threads"), series.numericSeries("mean"));
			ser.setMarker(SeriesMarker.CIRCLE);
		}

		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.show();
	}
	
	public void showNormalizedSizeSeries(SampleList samples, String jvm, int etalon) throws IOException {
		// Create Chart
		Chart chart = new ChartBuilder()
			.width(800).height(600)
			.theme(ChartTheme.Matlab)
			.title("Young GC parallerism normalized by " + etalon + " threads case [" + jvm + "]")
			.xAxisTitle("Parallel threads").yAxisTitle("Parallel factor")
		.build();
		
		chart.getStyleManager().setPlotGridLinesVisible(true);
		chart.getStyleManager().setChartType(ChartType.Line);
		 
		SampleList series;
		
		samples = samples.filter("jvm", jvm);
		
		samples = samples.filter("algo", "ParNew");
		
		for(Object t: new TreeSet<Object>(samples.groupBy("size").keySet())) {
			double size = ((Number)t).intValue();
			series = newSizeSeries(samples, jvm, "ParNew", size);
			double norm = series.filter("threads", new Double(etalon)).numericSeries("mean")[0];
			double[] factor = series.numericSeries("mean");
			for(int i = 0; i != factor.length; ++i) {
				factor[i] = etalon * norm / factor[i];
			}
			Series ser = chart.addSeries("" + size + " [GiB]", series.numericSeries("threads"), factor);
			ser.setMarker(SeriesMarker.CIRCLE);
		}
	
		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.show();
	}

	private SampleList newThreadSeries(SampleList list, String jvm, String algo, int threads) {
		list = list.filter("jvm", jvm).filter("algo", algo).filter("threads", new Double(threads));
//		list = list.sort("stdDev").reverseSort("count").filterFirst("jvm", "algo", "size", "threads");
		list = list.sort("mean").filterMedian("jvm", "algo", "size", "threads");
		return list.sort("size");
	}

	private SampleList newSizeSeries(SampleList list, String jvm, String algo, double size) {
		list = list.filter("jvm", jvm).filter("algo", algo).filter("size", size);
//		list = list.sort("stdDev").reverseSort("count").filterFirst("jvm", "algo", "size", "threads");
		list = list.sort("mean").filterMedian("jvm", "algo", "size", "threads");
		return list.sort("threads");
	}

	
}
