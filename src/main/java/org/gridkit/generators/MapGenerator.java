package org.gridkit.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class MapGenerator<K, V> implements DeterministicObjectGenerator<Map<K, V>> {

	private int sizeAvg;
	private int sizeStdDev;
	
	private int valueSpaceLimit = 0;
	private int keyHashValueSpaceShift = 0;
	
	private DeterministicObjectGenerator<K> keyGenerator;
	private DeterministicObjectGenerator<V> valueGenerator;
	
	private Random rnd;
	
	
	public MapGenerator(
			int sizeAvg, 
			int sizeStdDev, 
			DeterministicObjectGenerator<K> keyGenerator,
			DeterministicObjectGenerator<V> valueGenerator) {
		this.sizeAvg = sizeAvg;
		this.sizeStdDev = sizeStdDev;
		this.keyGenerator = keyGenerator;
		this.valueGenerator = valueGenerator;
	}
	
	public MapGenerator(
			int sizeAvg, 
			int sizeStdDev, 
			DeterministicObjectGenerator<K> keyGenerator,
			DeterministicObjectGenerator<V> valueGenerator,
			int valueSpaceLimit,
			int keyHashValueSpaceShift) {
		this.sizeAvg = sizeAvg;
		this.sizeStdDev = sizeStdDev;
		this.valueSpaceLimit = valueSpaceLimit;
		this.keyHashValueSpaceShift = keyHashValueSpaceShift;
		this.keyGenerator = keyGenerator;
		this.valueGenerator = valueGenerator;
	}

	@Override
	public Map<K, V> object(long id) {
		int size = (int) (sizeAvg + sizeStdDev * rnd.nextGaussian());
		if (size < 1) {
			size = 1;
		}
		Map<K, V> map = new HashMap<K, V>();
		while(map.size() < size) {
			int n = map.size();
			K key = keyGenerator.object(rnd.nextLong());
			long vid = rnd.nextLong();
			if (valueSpaceLimit != 0) {
				vid %= valueSpaceLimit;
				vid += key.hashCode() * keyHashValueSpaceShift;
			}
			V value = valueGenerator.object(vid);
			map.put(key, value);
		}
		return map;
	}
	
	public MapGenerator<K, V> clone() {
		return new MapGenerator<K, V>(sizeAvg, sizeStdDev, keyGenerator, valueGenerator, valueSpaceLimit, keyHashValueSpaceShift);
	}	
}
