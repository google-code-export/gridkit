package org.gridkit.lab.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SampleListTest {

	public SampleList series1() {
		List<Sample> samples = new ArrayList<Sample>();
		Sample s;
		s = new Sample();
		s.setCoord("N", 1);
		s.setCoord("UID", "3333");
		s.setCoord("System", "X1");
		s.setResult("Count", 1);
		s.setResult("Value", -1);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 2);
		s.setCoord("UID", "2222");
		s.setCoord("System", "X2");
		s.setResult("Count", 10);
		s.setResult("Value", -10);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 3);
		s.setCoord("UID", "1111");
		s.setCoord("System", "X3");
		s.setResult("Count", 20);
		s.setResult("Value", -20);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 4);
		s.setCoord("UID", "4444");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -2);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 5);
		s.setCoord("UID", "5555");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -3);
		samples.add(s);
		s = new Sample();
		s.setCoord("N", 6);
		s.setCoord("UID", "6666");
		s.setCoord("System", "X3*");
		s.setResult("Count", 100);
		s.setResult("Value", -4);
		samples.add(s);
		
		Collections.reverse(samples);
		
		return new SampleList(samples);
	}
	
	@Test
	public void verify_string_sort() {
		
		double[] expected = {3, 2, 1, 4, 5, 6};
		double[] result = series1().sort("UID").numericSeries("N");
		
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(result));
	}

//	@Test
//	public void verify_string_sort() {
//		
//		double[] expected = {3, 2, 1, 4, 5, 6};
//		double[] result = series1().sort("UID").numericSeries("N");
//		
//		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(result));
//	}
//	
}
