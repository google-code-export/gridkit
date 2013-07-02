package org.gridkit.lab.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class SampleCSVReader {

	public static List<Sample> read(String path) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (is != null) {
			return new SampleCSVReader(is).readAll();
		}
		else if (new File(path).exists()) {
			return new SampleCSVReader(new FileReader(path)).readAll();
		}
		throw new FileNotFoundException(path);
	}
	
	private CSVHeader header;
	private CSVReader reader;
	
	public SampleCSVReader(Reader reader) throws IOException {
		this.reader = new CSVReader(reader);
		this.header = readHeader(this.reader.readNext());
	}

	public SampleCSVReader(InputStream stream) throws IOException {
		this(new InputStreamReader(stream));
	}

	protected CSVHeader readHeader(String[] h) throws IOException {
		if (h == null) {
			throw new IOException("Header is missing");
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
	
	public Sample readNext() throws IOException {
		String[] row = reader.readNext();
		if (row == null) {
			close();
			return null;
		}
		return header.read(row);
	}

	public List<Sample> readAll() throws IOException {
		List<Sample> list = new ArrayList<Sample>();
		Sample dp;
		while((dp = readNext()) != null) {
			list.add(dp);
		}
		return list;
	}
	
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}
}
