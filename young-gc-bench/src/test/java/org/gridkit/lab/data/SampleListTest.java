package org.gridkit.lab.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class SampleListTest {

	public SampleList series1() {
		List<Sample> samples = new ArrayList<Sample>();
		Sample s;
		s = new Sample();
		s.setCoord("N", 1);
		s.setCoord("UID", "u3333");
		s.setCoord("System", "X1");
		s.setResult("Count", 1);
		s.setResult("Value", -1);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 2);
		s.setCoord("UID", "u2222");
		s.setCoord("System", "X2");
		s.setResult("Count", 10);
		s.setResult("Value", -10);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 3);
		s.setCoord("UID", "u1111");
		s.setCoord("System", "X3");
		s.setResult("Count", 20);
		s.setResult("Value", -20);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 4);
		s.setCoord("UID", "u4444");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -2);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 5);
		s.setCoord("UID", "u5555");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -3);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 6);
		s.setCoord("UID", "u6666");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -4);
		samples.add(s);
		
		Collections.reverse(samples);
		
		return new SampleList(samples);
	}

	public SampleList newList(int n) {
		List<Sample> list = new ArrayList<Sample>();
		for(int i = 0; i != n; ++i) {
			Sample s = new Sample();
			s.setCoord("N", i);
			list.add(s);
		}
		return new SampleList(list);
	}
	
	public void addGausian(SampleList samples, String field, long seed) {
		Random rnd = new Random(seed);
		for(Sample s: samples.asList()) {
			s.setResult(field, rnd.nextGaussian());
		}
	}

	public void addRandom(SampleList samples, String field, long seed, String... values) {
		Random rnd = new Random(seed);
		for(Sample s: samples.asList()) {
			s.setCoord(field, values[rnd.nextInt(values.length)]);
		}
	}

	public void addRoundRobin(SampleList samples, String field, String... values) {
		int n = 0;
		for(Sample s: samples.asList()) {
			s.setCoord(field, values[(n++) % values.length]);
		}
	}

	public void addColumn(SampleList samples, String field, long... values) {
		int n = 0;
		for(Sample s: samples.asList()) {
			s.setCoord(field, values[n++]);
		}
	}

	public void addColumn(SampleList samples, String field, double... values) {
		int n = 0;
		for(Sample s: samples.asList()) {
			s.setCoord(field, values[n++]);
		}
	}

	public void addColumn(SampleList samples, String field, String... values) {
		int n = 0;
		for(Sample s: samples.asList()) {
			s.setCoord(field, values[n++]);
		}
	}
	
	@Test
	public void verify_string_field_sort() {
		
		long[] expected = {3, 2, 1, 4, 5, 6};
		long[] result = series1().sort("UID").integerSeries("N");
		
		assertEquals(expected, result);
	}

	@Test
	public void verify_integer_field_sort_1() {
		
		long[] expected = {-20, -10, -4, -3, -2, -1};
		long[] result = series1().sort("Value").integerSeries("Value");
		
		assertEquals(expected, result);
	}

	@Test
	public void verify_two_field_sort() {
		
		long[] expected = {1, 2, 3, 4, 5, 6};
		long[] result = series1().sortReverse("N").sort("Count", "UID").integerSeries("N");
		
		assertEquals(expected, result);
	}

	@Test
	public void verify_first() {
	
		SampleList list;
	
		list = gausianSeries1().filterFirst("T");
		long[] first_unsorted = {0, 1, 3};
		assertEquals(first_unsorted, list.integerSeries("N"));
		
		list = gausianSeries1().sort("T", "X").filterFirst("T");
		long[] first_by_X = {8, 1, 5};
		assertEquals(first_by_X, list.integerSeries("N"));

		list = gausianSeries1().sortReverse("X").sort("T").filterFirst("T");
		long[] last_by_X = {0, 2, 4};
		assertEquals(last_by_X, list.integerSeries("N"));
	}

	@Test
	public void verify_median() {
		
		SampleList list;
		
		list = gausianSeries1().filterMedian("T");
		long[] med_unsorted = {7, 2, 5};
		assertEquals(med_unsorted, list.integerSeries("N"));
		
		list = gausianSeries1().sort("T", "X").filterMedian("T");
		long[] med_by_X = {7, 2, 6};
		assertEquals(med_by_X, list.integerSeries("N"));
		
		list = gausianSeries1().sortReverse("X").sort("T").filterMedian("T");
		long[] reverse_med_by_X = {7, 1, 6};
		assertEquals(reverse_med_by_X, list.integerSeries("N"));
	}

	private SampleList gausianSeries1() {
		SampleList list = newList(10);
		addGausian(list, "X", 0);
		addRandom(list, "T", 0, "A", "B", "C");
		return list;
	}

	private void assertEquals(long[] expected, long[] result) {
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(result));
	}
	
}
