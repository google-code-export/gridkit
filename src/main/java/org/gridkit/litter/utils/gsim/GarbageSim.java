package org.gridkit.litter.utils.gsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class GarbageSim {

	
//	public static 
	
	
	public static interface Demography {
		
		public float nextObjectLifeSpan();
		
	}
	
	public static class WeightedDemography implements Demography {
		
		private Random end = new Random();
		private List<Demography> parts = new ArrayList<GarbageSim.Demography>();
		private List<Double> weights = new ArrayList<Double>();
		private double totalWeight = 0d;
		
		public 
		
	}
	
}
