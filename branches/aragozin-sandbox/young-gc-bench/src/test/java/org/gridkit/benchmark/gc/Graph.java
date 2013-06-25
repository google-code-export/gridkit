package org.gridkit.benchmark.gc;

import java.awt.Color;
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
import com.xeiam.xchart.SeriesLineStyle;
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
	public void showTreadsGraph_hs6u43() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");

		samples = samples.filter("algo", "Serial", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 6u43, MSC]", samples, "hs6u43", null, range(4, 50));
	}

	@Test
	public void showTreadsGraph_hs7u15() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");
		
		samples = samples.filter("algo", "Serial", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 7u15, MSC]", samples, "hs7u15", null, range(4, 50));
	}

	@Test
	public void showTreadsGraph_MSC_compare_hs7_hs6_low() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");
		
		samples = samples.filter("algo", "Serial", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 6u43 Vs. 7u15, MSC]", samples, "hs6u43", "hs7u15", range(0, 8));
	}

	@Test	
	public void showTreadsGraph_MSC_compare_hs7_hs6_high() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");
		
		samples = samples.filter("algo", "Serial", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 6u43 Vs. 7u15, MSC]", samples, "hs6u43", "hs7u15", range(9, 50));
	}
	
	@Test
	public void showTreadsGraph_CMS_compare_hs7_hs6_low() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");
		
		samples = samples.filter("algo", "CMS_DefNew", "CMS_ParNew");
		samples = samples.replace("algo", "CMS_DefNew", "Serial");
		samples = samples.replace("algo", "CMS_ParNew", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 6u43 Vs. 7u15, CMS]", samples, "hs6u43", "hs7u15", range(0, 8));
	}
	
	@Test	
	public void showTreadsGraph_CMS_compare_hs7_hs6_high() throws IOException {
		SampleList samples = readSamples("gcrep.cms-full.txt");
		
		samples = samples.filter("algo", "CMS_DefNew", "CMS_ParNew");
		samples = samples.replace("algo", "CMS_DefNew", "Serial");
		samples = samples.replace("algo", "CMS_ParNew", "ParNew");
		
		showThreadSeries("Young GC pause times [Java 6u43 Vs. 7u15, MSC]", samples, "hs6u43", "hs7u15", range(9, 50));
	}

	@Test	
	public void showTreadsGraph_G1() throws IOException {
		SampleList samples = readSamples("g1rep.txt");
		
		samples = samples.filter("algo", "G1");
		samples = samples.replace("algo", "G1", "ParNew");
		
		showThreadSeries("Young GC pause times [7u15, G1]", samples, "hs7u15", null, range(8, 50));
	}
	
	@Test
	public void showSizeGraph() throws IOException {
		SampleList data = readSamples("gcrep.1488.txt");
		data = data.filter("threads", 8, 100);
//		showSizeSeries(data, "hs6u43");
		showSizeSeries(data, "hs7u15");
	}

	@Test
	public void showNormalizedSizeGraph_MSC_j6() throws IOException {
//		SampleList data = readSamples("gcrep.2462.txt");
		SampleList data = readSamples("gcrep.cms-full.txt");
		showNormalizedSizeSeries(data, "hs6u43", "Java 6u43, MSC", 8);
	}

	@Test
	public void showNormalizedSizeGraph_MSC_j7() throws IOException {
//		SampleList data = readSamples("gcrep.2462.txt");
		SampleList data = readSamples("gcrep.cms-full.txt");
		showNormalizedSizeSeries(data, "hs7u15", "Java 7u15, MSC", 8);
	}

	@Test
	public void showNormalizedSizeGraph_CMS_j6() throws IOException {
//		SampleList data = readSamples("gcrep.txt");
		SampleList data = readSamples("gcrep.cms-full.txt");
		showNormalizedSizeSeries(data, "hs6u43", "Java 6u43, CMS", 4);
	}

	@Test
	public void showNormalizedSizeGraph_CMS_j7() throws IOException {
//		SampleList data = readSamples("gcrep.2462.txt");
		SampleList data = readSamples("gcrep.cms-full.txt");
		showNormalizedSizeSeries(data, "hs7u15", "Java 7u15, CMS", 4);
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
	
	private String jvm1Tag(String jvm1, String jvm2) {
		return jvm2 == null ? "" : " [" + jvm1 + "]"; 
	}

	private String jvm2Tag(String jvm1, String jvm2) {
		return jvm2 == null ? "" : " [" + jvm2 + "]"; 
	}
	
	public void showThreadSeries(String title, SampleList samples, String jvm1, String jvm2, int... threads) throws IOException {
		// Create Chart
		Chart chart = new ChartBuilder()
			.width(800).height(600)
			.theme(ChartTheme.Matlab)
			.title(title)
			.xAxisTitle("Old space [GiB]").yAxisTitle("Pause mean [ms]")
		.build();
		
		ColorPallete pal1 = new ColorPallete(0.9f, 0.8f, -0.01f, 0.30f);
		ColorPallete pal2 = new ColorPallete(0.9f, 0.7f, -0.01f, 0.30f);
		
		chart.getStyleManager().setPlotGridLinesVisible(true);
		chart.getStyleManager().setChartType(ChartType.Line);
		 
		SampleList series;

		SampleList samples1 = samples.filter("jvm", jvm1);
		SampleList samples2 = jvm2 == null ? null : samples.filter("jvm", jvm2);
		
		if (has(threads, 0)) {
			series = newThreadSeries(samples1, jvm1, "Serial", 1);
			Series ser1 = chart.addSeries("Serial" + jvm1Tag(jvm1, jvm2), series.numericSeries("size"), series.numericSeries("mean"));
			Color c = pal1.nextColor();
			ser1.setMarker(SeriesMarker.CIRCLE);
			ser1.setLineColor(c);
			ser1.setMarkerColor(c);
			ser1.setLineStyle(SeriesLineStyle.SOLID);
			if (jvm2 != null) {
				series = newThreadSeries(samples2, jvm2, "Serial", 1);
				Series ser2 = chart.addSeries("Serial"  + jvm2Tag(jvm1, jvm2), series.numericSeries("size"), series.numericSeries("mean"));
				c = pal2.nextColor();
				ser2.setMarker(SeriesMarker.CIRCLE);
				ser2.setMarkerColor(c);
				ser2.setLineColor(c);
				ser2.setLineStyle(SeriesLineStyle.DOT_DOT);
			}
		}

		samples = samples.filter("algo", "ParNew");
		
		for(Object t: new TreeSet<Object>(samples.groupBy("threads").keySet())) {
			int tc = ((Number)t).intValue();
			if (has(threads, tc)) {
				series = newThreadSeries(samples, jvm1, "ParNew", tc);
				Series ser1 = chart.addSeries("pt=" + tc + jvm1Tag(jvm1, jvm2), series.numericSeries("size"), series.numericSeries("mean"));
				Color c = pal1.nextColor();
				ser1.setMarker(SeriesMarker.CIRCLE);
				ser1.setLineColor(c);
				ser1.setMarkerColor(c);
				ser1.setLineStyle(SeriesLineStyle.SOLID);
				if (jvm2 != null) {
					series = newThreadSeries(samples2, jvm2, "ParNew", tc);
					Series ser2 = chart.addSeries("pt=" + tc + jvm2Tag(jvm1, jvm2), series.numericSeries("size"), series.numericSeries("mean"));
					c = pal2.nextColor();
					ser2.setMarker(SeriesMarker.CIRCLE);
					ser2.setMarkerColor(c);
					ser2.setLineColor(c);
					ser2.setLineStyle(SeriesLineStyle.DOT_DOT);
				}
			}
		}

		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
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
		dialog.setVisible(true);
	}
	
	public void showNormalizedSizeSeries(SampleList samples, String jvm, String caption, int etalon) throws IOException {
		// Create Chart
		Chart chart = new ChartBuilder()
			.width(800).height(600)
			.theme(ChartTheme.Matlab)
			.title("Young GC parallerism normalized by " + etalon + " threads case [" + caption + "]")
			.xAxisTitle("Parallel threads").yAxisTitle("Parallel factor")
		.build();
		
		chart.getStyleManager().setPlotGridLinesVisible(true);
		chart.getStyleManager().setChartType(ChartType.Line);
		 
		SampleList series;
		
		samples = samples.filter("jvm", jvm);
		
		samples = samples.filter("algo", "ParNew");
		
		int maxThreads = etalon;
		
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
			for(double tc: factor) {
				if (maxThreads < tc) {
					maxThreads = (int) Math.ceil(tc);
				}
			}
		}
	
		double[] diag = {1, maxThreads};
		Series diagSer = chart.addSeries("x=y", diag, diag);
		diagSer.setMarker(SeriesMarker.NONE);
		diagSer.setLineStyle(SeriesLineStyle.DASH_DASH);
		diagSer.setLineColor(Color.GRAY.brighter());
		
		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
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
