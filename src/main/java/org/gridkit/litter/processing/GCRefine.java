package org.gridkit.litter.processing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class GCRefine {

	
	public static void main(String[] args) throws IOException {
		int skipFull = 3;
		int skipYoung = 15;
		nextFile:
		for(String file: args) {
			if (file.startsWith("-skip-full")) {
				skipFull = Integer.parseInt(file.substring("-skip-full=".length()));
				System.out.println("Skip full collections = " + skipFull);
				continue;
			}
			if (file.startsWith("-skip-young:")) {
				skipYoung = Integer.parseInt(file.substring("-skip-young=".length()));
				System.out.println("Skip young collections = " + skipYoung);
				continue;
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Writer writer = new FileWriter(file + ".points");
			
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					System.out.println("Incomplete file: " + file);
					continue nextFile;
				}
				if (isProcessingStartedLine(line)) {
					break;
				}
			}
			
			int fullSkip = skipFull;
			int lineSkip = skipYoung;
			boolean process = false;
			List<Double> points = new ArrayList<Double>();
			List<Double> fullPoints = new ArrayList<Double>();
			
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (isGCLine(line)) {
					if (isFullGCLine(line)) {
						--fullSkip;
						if (!points.isEmpty()) {
							double avg = 0;
							for(double point: points) {
								avg += point;
							}
							avg /= points.size();
							System.out.println("Avg: " + avg + " over " + points.size() + " points");
							for(double point: points) {
								writer.append(String.valueOf(point)).append('\n');
							}
							fullPoints.addAll(points);
							points.clear();
						}

						if (fullSkip <= 0 && lineSkip <= 0) {
							process = true;
						}
					}
					else if (isMinorGC(line)){
						--lineSkip;
						if (process) {
							points.add(getPauseTime(line));
						}
					}
					
					if (skipFull == 0 && lineSkip <= 0) {
						process = true;
					}
				}			
			}

			if (!points.isEmpty() && fullPoints.isEmpty()) {
				fullPoints.addAll(points);
			}
			
			double avg = 0;
			for(double point: fullPoints) {
				avg += point;
			}
			avg /= fullPoints.size();
			System.out.println("[" + file + "] Total avg: " + avg + " over " + fullPoints.size() + " points");
			writer.append(String.valueOf(avg)).append('\n');
			
			writer.close();
			reader.close();
		}
	}

	private static boolean isProcessingStartedLine(String line) {
		return line != null && (line.contains("Processing") || line.contains("Initial loading complete"));
	}
	
	private static boolean isGCLine(String line) {
		return (line.startsWith("[GC ") || line.startsWith("[Full GC ")) && !(line.contains("CMS-"));
	}
	
	private static boolean isFullGCLine(String line) {
		return line.contains("Full GC") || line.contains("Tenured");
	}
	
	private static boolean isMinorGC(String line) {
		if (line.contains("CMS") || line.contains("YG occupancy")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private static Pattern PAUSE_TIME = Pattern.compile(" ([\\d][.][\\d]+) secs"); 
	
	private static double getPauseTime(String line) {
		Matcher matcher = PAUSE_TIME.matcher(line);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Cannot pasre: " + line);
		}
		return Double.parseDouble(matcher.group(1));
	}
}
