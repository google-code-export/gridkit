/*
 * File: RangeTests.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.common.ranges;

import java.util.Iterator;

import org.testng.annotations.Test;

/**
 * <p>Unit tests for the {@link Range} implementations.</p>
 * 
 * @author Brian Oliver
 */
public class RangeTests {

	@Test
	public void testUnion() {
		
		Range range = new SparseRange(new ContiguousRange(4, 4), new ContiguousRange(2, 2));
		
		assert range.size() == 2;
		assert range.getFrom() == 2;
		assert range.getTo() == 4;		
		
		Iterator<Long> iterator = range.iterator();
		assert iterator.hasNext() && iterator.next() == 2;
		assert iterator.hasNext() && iterator.next() == 4;
		assert !iterator.hasNext();
		
		range = range.union(range.union(new ContiguousRange(1, 1)));
		
		assert range.size() == 3;
		assert range.getFrom() == 1;
		assert range.getTo() == 4;		

		iterator = range.iterator();
		assert iterator.hasNext() && iterator.next() == 1;
		assert iterator.hasNext() && iterator.next() == 2;
		assert iterator.hasNext() && iterator.next() == 4;
		assert !iterator.hasNext();
	}
	
	
	@Test
	public void testRemove() {
		Range range = new SparseRange(new ContiguousRange(4, 4), new ContiguousRange(2, 2));
		
		range = range.remove(2);

		assert range.size() == 1;
		assert range.getFrom() == 4;
		assert range.getTo() == 4;		
		
		Iterator<Long> iterator = range.iterator();
		assert iterator.hasNext() && iterator.next() == 4;
		assert !iterator.hasNext();
		
		range = range.remove(4);
		assert range.size() == 0;
		assert range.isEmpty();
	}
}
