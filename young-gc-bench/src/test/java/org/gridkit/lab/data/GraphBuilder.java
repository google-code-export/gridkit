package org.gridkit.lab.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.gridkit.benchmark.gc.ColorPallete;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager;
import com.xeiam.xchart.StyleManager.ChartTheme;
import com.xeiam.xchart.XChartPanel;
import com.xeiam.xchart.internal.chartpart.Axis.AxisType;
import com.xeiam.xchart.internal.style.SeriesColorMarkerLineStyle;

public class GraphBuilder {

	private static Series STUB = new Series("stub", Collections.singleton(0d), AxisType.Number, Collections.<Number>singleton(0d), AxisType.Number, null, new SeriesColorMarkerLineStyle(Color.black, null, new BasicStroke()));
	
	Chart chart = new ChartBuilder().width(1000).height(600).theme(ChartTheme.Matlab).build();

	Map<String, List<Series>> displayGroups = new HashMap<String, List<Series>>();
	
	public GraphBuilder setTitle(String title) {
		chart.setChartTitle(title);
		return this;
	}

	public GraphBuilder setXAxisTitle(String title) {
		chart.setXAxisTitle(title);
		return this;
	}

	public GraphBuilder setYAxisTitle(String title) {
		chart.setYAxisTitle(title);
		return this;
	}

	public void addSeries(String group, String name, double[] x, double[] y) {
//		System.out.println("Series " + name + " " + Arrays.toString(y));
		Series s = chart.addSeries(name, x, y);
		group(group).add(s);
	}

	public void addStub(String group) {
		group(group).add(STUB);
	}

	public void setMarker(String group, SeriesMarker marker) {
		for(Series s: group(group)) {
			s.setMarker(marker);
		}
	}

	public void setLineColor(String group, Color color) {
		for(Series s: group(group)) {
			s.setLineColor(color);
			s.setMarkerColor(color);
		}
	}

	public void setLineColor(String group, ColorPallete pallete) {
		for(Series s: group(group)) {
			Color c = pallete.nextColor();
			s.setLineColor(c);
			s.setMarkerColor(c);
		}
	}

	public void setLineStyle(String group, BasicStroke stroke) {
		for(Series s: group(group)) {
			s.setLineStyle(stroke);
		}
	}

	public void setLineStyle(String group, SeriesLineStyle stroke) {
		for(Series s: group(group)) {
			s.setLineStyle(stroke);
		}
	}

	public void setMarkerColor(String group, Color color) {
		for(Series s: group(group)) {
			s.setMarkerColor(color);
		}
	}

	public void setMarkerColor(String group, ColorPallete pallete) {
		for(Series s: group(group)) {
			s.setMarkerColor(pallete.nextColor());
		}
	}
	
	public void setConnerSE(double x, double y) {
		chart.getStyleManager().setXAxisMin(x);
		chart.getStyleManager().setYAxisMin(y);
	}
	
	public StyleManager getStyleManager() {
		return chart.getStyleManager();
	}
	
	private List<Series> group(String group) {
		List<Series> list = displayGroups.get(group);
		if (list == null) {
			list = new ArrayList<Series>();
			displayGroups.put(group, list);
		}
		return list;
	}

	public GraphBuilder writeTo(String fileName) throws IOException {
		File file = new File(fileName);
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		file.delete();
		
		BitmapEncoder.savePNGWithDPI(chart, fileName, 360);
		return this;
	}
	
	public void show() {
		XChartPanel panel = new XChartPanel(chart);
		
		JDialog dialog = new JDialog();
		dialog.add(panel);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
	}
}
