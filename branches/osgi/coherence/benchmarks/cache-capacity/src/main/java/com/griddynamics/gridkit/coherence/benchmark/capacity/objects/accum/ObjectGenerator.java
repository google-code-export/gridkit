/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package com.griddynamics.gridkit.coherence.benchmark.capacity.objects.accum;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
public class ObjectGenerator {

	private Random rnd = new Random(0);
	
	private String[] attributeSet = {"OBJ_TYPE", "ATTR1", "ATTR2"};
	private Map<String, String[]> attribValueSet = new HashMap<String, String[]>();
	{
		String[] objTypes = new String['z'-'a'];
		for(int i = 0; i != objTypes.length; ++i) {
			objTypes[i] = String.valueOf((char)('a' + i));
		}
		attribValueSet.put("OBJ_TYPE", objTypes);
		
		String[] attrs = new String[100];
		for(int i = 0; i != attrs.length; ++i) {
			attrs[i] = String.valueOf(rnd.nextInt(100000));
		}
		
		attribValueSet.put("ATTR1", attrs);
		attribValueSet.put("ATTR2", attrs);		
	};
	
	private double attrRatioAvg = 0.3d;
	private double attrRatioDev = 3d;
	private double ruleRatioAvg = 3d;
	private double ruleRatioDev = 3d;
	
	private double accumsPerSideAvg = 22;
	private double accumsPerSideDev = 7;
	
	private long firstId = System.currentTimeMillis(); 
	private long accumCount;
	private long attribCount;
	private long thresholdCount;
	private long ruleCount;
	
	
	public long getFirstAccumId() {
		return firstId;
	}

	public long getAccumCount() {
		return accumCount;
	}

	public long getAttribCount() {
		return attribCount;
	}

	public long getThresholdCount() {
		return thresholdCount;
	}

	public long getRuleCount() {
		return ruleCount;
	}

	public Map<Long, Accumulator> generate(long fromSide, long toSide) {
		Map<Long, Accumulator> result = new HashMap<Long, Accumulator>();
		
		long startId = firstId + accumCount;
		long nextId = startId;
		for(long side = fromSide; side != toSide; ++side) {
			nextId = generateForSide(result, side, startId, nextId);
		}
		
		return result;
	}

	private long generateForSide(Map<Long, Accumulator> result, long side, long startId, long nextId) {
		
		int count = randomGausian(accumsPerSideAvg, accumsPerSideDev);
		
		for(; count > 0; --count) {
			Accumulator accum = new Accumulator();
			++accumCount;
			accum.setId(nextId++);
			accum.setName("Name-" + rnd.nextInt(100));
			accum.setSide(String.valueOf(side));
			accum.setStatus(Accumulator.STATUS_ACTIVE);
			accum.setValue(rnd.nextInt(10000));
			
			int attrCount = randomGausian(attrRatioAvg, attrRatioDev);
			
			for(;attrCount > 0; --attrCount) {
				
				String attrName = attributeSet[rnd.nextInt(attributeSet.length)];
				String[] vset = attribValueSet.get(attrName);
				String attrValue = vset[rnd.nextInt(vset.length)]; 
			
				Attribute attr = new Attribute();
				++attribCount;
				attr.setName(attrName);
				attr.addValue(attrValue);
				
				if (rnd.nextFloat() > 0.5f) {
					attr.addValue(vset[rnd.nextInt(vset.length)]);
				}
				
				accum.addAttribute(attr);
			}
			
			int ruleCount = randomGausian(ruleRatioAvg, ruleRatioDev);
			
			for(;ruleCount > 0; -- ruleCount) {
			
				Threshold thres = new Threshold();
				++thresholdCount;
				thres.setId(-1);
				thres.setDateFrom(System.currentTimeMillis());
				thres.setDirection(rnd.nextBoolean() ? Threshold.DIR_DOWN : Threshold.DIR_UP);
				thres.setValue(rnd.nextInt(10000));
				
				Rule rule = new Rule();
				++this.ruleCount;
				rule.setId(-1);
				rule.setDateFrom(System.currentTimeMillis());
				rule.setOperation(rnd.nextBoolean() ? Rule.OP_STATUS : Rule.OP_VALUE);
				if (rule.getOperation() == Rule.OP_VALUE) {
					int range = (int) (accum.getId() - startId);
					if (range <= 0) {
						rule.setAccumulatorId(startId + 1);
					}
					else {
						rule.setAccumulatorId(startId + rnd.nextInt(range));
					}
					
					rule.setValue(rnd.nextInt(10000));
					
				}
				else {
					rule.setAccumulatorId(accum.getId());
					rule.setValue(Accumulator.STATUS_BLOCKED);
				}
				thres.setRule(rule);
				
				accum.addThreshold(thres);
			}
			
			result.put(accum.getId(), accum);
		}
		
		return nextId;
	}
	
	private int randomGausian(double avg, double dev) {
		int count = (int) (rnd.nextGaussian() * dev + avg);
		if (count < 0) {
			count = 0;
		}
		
		return count;
	}
}
