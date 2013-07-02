package org.gridkit.lab.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CSVDataTest {

	@Test
	public void test_read_write() throws IOException {
		List<Sample> list = dataSet1();
		String path = "target/test.dat";
		SampleCSVWriter.overrride(path, list);
		List<Sample> list2 = SampleCSVReader.read(path);
		Assert.assertEquals(list, list2);
	}

	@Test
	public void test_overwrite() throws IOException {
		String path = "target/test.dat";
		SampleCSVWriter.overrride(path, dataSet1());
		SampleCSVWriter.overrride(path, dataSet2());
		List<Sample> list2 = SampleCSVReader.read(path);
		Assert.assertEquals(dataSet2(), list2);
	}

	@Test
	public void test_append() throws IOException {
		String path = "target/test.dat";
		SampleCSVWriter.overrride(path, dataSet1());
		SampleCSVWriter.append(path, dataSet2());
		List<Sample> list2 = SampleCSVReader.read(path);
		List<Sample> expected = new ArrayList<Sample>();
		expected.addAll(dataSet1());
		expected.addAll(dataSet2());
		Assert.assertEquals(expected, list2);
	}

	@Test
	public void test_merge() throws IOException {
		String path = "target/test.dat";
		SampleCSVWriter.overrride(path, dataSet1());
		SampleCSVWriter.append(path, dataSet3());
		List<Sample> list2 = SampleCSVReader.read(path);
		List<Sample> expected = new ArrayList<Sample>();
		expected.addAll(dataSet1());
		expected.addAll(dataSet3());
		Assert.assertEquals(expected, list2);
	}

	private List<Sample> dataSet1() {
		List<Sample> list = new ArrayList<Sample>();
		Sample dp = new Sample();
		dp.setCoord("A", "aaa");
		dp.setResult("V", "100");
		list.add(dp);
		dp = new Sample();
		dp.setCoord("A", "aaa");
		dp.setResult("V", "101");
		list.add(dp);
		return list;
	}

	private List<Sample> dataSet2() {
		List<Sample> list = new ArrayList<Sample>();
		Sample dp = new Sample();
		dp.setCoord("A", "aaa");
		dp.setResult("V", "200");
		list.add(dp);
		dp.setCoord("A", "aaa");
		dp.setResult("V", "201");
		list.add(dp);
		return list;
	}

	private List<Sample> dataSet3() {
		List<Sample> list = new ArrayList<Sample>();
		Sample dp = new Sample();
		dp.setCoord("AA", "aaa");
		dp.setResult("V", "200");
		list.add(dp);
		dp.setCoord("AA", "aaa");
		dp.setResult("V", "201");
		list.add(dp);
		return list;
	}
	
}
