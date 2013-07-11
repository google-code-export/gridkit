package org.gridkit.lab.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class SampleCSVWriter {

	public static void append(String path, Iterable<Sample> points) throws IOException {
		new File(path).getParentFile().mkdirs();
		SampleCSVWriter writer = new SampleCSVWriter(path, true);
		writer.writeAll(points);
		writer.close();
	}

	public static void overrride(String path, Iterable<Sample> points) throws IOException {
		File dir = new File(path).getParentFile();
		if (dir != null) {
			dir.mkdirs();
		}
		SampleCSVWriter writer = new SampleCSVWriter(path, false);
		writer.writeAll(points);
		writer.close();
	}
	
	private CSVHeader header = new CSVHeader(Collections.<String>emptyList(), Collections.<String>emptyList());
	
	private File file;
	private CSVWriter writer;
	
	public SampleCSVWriter(String file, boolean append) throws IOException {
		this(new File(file), append);
	}

	public SampleCSVWriter(File file, boolean append) throws IOException {
		this.file = file;
		if (!append && file.exists()) {
			if (!file.delete()) {
				throw new IOException("Cannot delete file [" + file.getAbsolutePath() + "]");
			}
		}
		if (file.exists()) {
			reinitWriter();
		}
		else {
			// do not do anything, file will be initialezed on first write 
		}
	}

	
	protected void reinitWriter() throws IOException {
		if (!file.exists()) {
			FileOutputStream fos = new FileOutputStream(file, false);
			writer = new CSVWriter(new OutputStreamWriter(fos));
			writeHeader(writer);			
		}
		else {
			CSVHeader fileH = readHeader(file);
			if (fileH == null) {
				FileOutputStream fos = new FileOutputStream(file, false);
				writer = new CSVWriter(new OutputStreamWriter(fos));
				writeHeader(writer);
			}
			else {
				if (header.equals(fileH)) {
					FileOutputStream fos = new FileOutputStream(file, true);
					writer = new CSVWriter(new OutputStreamWriter(fos));
				}
				else {
					header = header.merge(fileH);
					String nn = file.getName() + ".old";
					File oldFile = new File(file.getParentFile(), nn);
					oldFile.delete();
					if (!file.renameTo(oldFile)) {
						throw new IOException("Cannot rename [" + file.getAbsolutePath() + "]");
					}
	
					// creating new file
					FileOutputStream fos = new FileOutputStream(file, false);
					writer = new CSVWriter(new OutputStreamWriter(fos));
					writeHeader(writer);
	
					CSVReader reader = new CSVReader(new FileReader(oldFile));
					reader.readNext();
					while(true) {
						String[] row = reader.readNext();
						if (row == null) {
							break;
						}
						writer.writeNext(header.write(fileH.read(row)));
					}
					writer.flush();
					reader.close();
					oldFile.delete();
				}
			}
		}
	}

	private void writeHeader(CSVWriter writer) {
		List<String> h = new ArrayList<String>();
		for(String hh: header.header1) {
			h.add(hh);
		}
		h.add("-");
		for(String hh: header.header2) {
			h.add(hh);
		}
		writer.writeNext(h.toArray(new String[0]));		
	}


	protected CSVHeader readHeader(File file) throws IOException {
		FileReader fr = new FileReader(file);
		CSVReader reader = new CSVReader(fr);
		String[] h = reader.readNext();
		reader.close();
		if (h == null) {
			return null;
		}
		else {
			List<String> t = Arrays.asList(h);
			int n = t.indexOf("-");
			if (n < 0) {
				throw new IllegalArgumentException("Invalid header format");
			}
			return new CSVHeader(t.subList(0, n), t.subList(n + 1, t.size()));
		}
	}
	
	public void writeNext(Sample dp) throws IOException {
		String[] row = header.write(dp);
		if (row == null) {
			updateHeader(dp);
			writer.writeNext(header.write(dp));			
		}
		else {
			if (writer == null) {
				reinitWriter();
			}
			writer.writeNext(row);			
		}
	}
	
	private void updateHeader(Sample dp) throws IOException {
		CSVHeader nh = new CSVHeader(new ArrayList<String>(dp.coordinateKeys()), new ArrayList<String>(dp.resultsKeys()));
		if (writer != null) {
			writer.close();
			writer = null;
		}
		header = header.merge(nh);
		reinitWriter();
	}


	public void writeAll(Iterable<Sample> points) throws IOException {
		for(Sample dp: points) {
			writeNext(dp);
		}
	}
	
	public void close() throws IOException {
		if (writer != null) {
			writer.flush();
			writer.close();
		}
	}
}
