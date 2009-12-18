/*
 * File: SparseRangeTest.java
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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

/**
 * <p>Unit tests for the {@link SparseRange} implementation.</p>
 * 
 * @author Christer Fahlgren
 */
public class SparseRangeTest {

	@Test
	public void testSparseRange() 
    	{
	    //Test default constructor
	    Range range = new SparseRange(); 
	    assert(range!=null);
	    assert(range.isEmpty());
	    assert(!range.isSingleton());
	    assert(range!=null);
	    }
	
	@Test
	public void testSparseRangeLong() 
	    {
	    //Test constructor which takes a value.
	    Range range = new SparseRange(5);
	    assert(range!=null);
	    assert(range.isEmpty());
	    assert(!range.isSingleton());
	    assert(range.getFrom()==5);
	    assert(range.getTo()==4);
	    assert(range.size()==0);
	    }
	
	@Test
	public void testSparseRangeContiguousRangeContiguousRange() 
	    {
	    //Test of constructor which takes two ContiguousRanges
	    Range range = new SparseRange(new ContiguousRange(-1, -1), new ContiguousRange(2, 2));
	
	    assert(range.size() == 2);
	    assert(range.getFrom() == -1);
	    assert(range.getTo() == 2);    
	    assert(!range.isEmpty());
	    assert(!range.isSingleton());
	
	    //Test the case when the first range is empty
	    range = new SparseRange(new ContiguousRange(-1, -2), new ContiguousRange(2, 2));
	
	    assert(range.size() == 1);
	    assert(range.getFrom() == 2);
	    assert(range.getTo() == 2);    
	    assert(!range.isEmpty());
	    assert(range.isSingleton());
	
	    //Test the case when the second range is empty
	
	    range = new SparseRange(new ContiguousRange(-1, 1), new ContiguousRange(2,1));
	
	    assert(range.size() == 3);
	    assert(range.getFrom() == -1);
	    assert(range.getTo() == 1);    
	    assert(!range.isEmpty());
	    assert(!range.isSingleton());
	
	    //Test the case when the both ranges are empty
	
	    range = new SparseRange(new ContiguousRange(-1, -2), new ContiguousRange(2,1));
	
	    assert(range.size() == 0);
	    assert(range.getFrom() == -1);
	    assert(range.isEmpty());
	    assert(!range.isSingleton());
	    }
	
	
	@Test
	public void testContains() 
	    {
	    //Test of contains method
	    Range range = new SparseRange(new ContiguousRange(-1, 1), new ContiguousRange(3, 4));
	
	    assert (range.contains(-1));
	    assert (range.contains(0));
	    assert (range.contains(1));
	    assert (!range.contains(2));
	    assert (range.contains(3));
	    assert (range.contains(4));
	    assert (!range.contains(5));
	    }
	
	@Test
	public void testIsAdjacentAndIntersects() 
	    {
	    //Testing a sparse range with a single digit gap
	    //TODO: Verify assumption that a contiguous range is not adjacent to a sparse range even if it would fit a gap
	    Range range = new SparseRange(new ContiguousRange(-1, 1), new ContiguousRange(3, 4));
	    Range inthemiddle = new ContiguousRange(2,2);
	    assert(range.isAdjacent(inthemiddle)); //Verify with Brian
	    assert(!range.intersects(inthemiddle));
	
	    //Testing the case where a range is overlapping a contiguous range in the middle
	    Range overlapmiddle = new ContiguousRange(2,3);
	    assert(range.contains(3));
	    assert(!range.contains(2));
	    assert(range.isAdjacent(overlapmiddle));
	    assert(range.intersects(overlapmiddle));
	
	    //Testing the case where a range is overlapping the first contiguous range partially
	    Range firstoverlap = new ContiguousRange(-2,0);
	    assert(!range.isAdjacent(firstoverlap));
	    assert(range.intersects(firstoverlap));
	
	    //Testing the case where a range is fully overlapping the sparserange 
	    Range fulloverlap = new ContiguousRange(-5,15);
	    assert(!range.isAdjacent(fulloverlap));
	    assert(range.intersects(fulloverlap));
	
	    //Testing the case where a before range is not overlapping and is not adjacent 
	    Range waybefore = new ContiguousRange(-5,-3);
	    assert(!range.isAdjacent(waybefore));
	    assert(!range.intersects(waybefore));
	
	    //Testing the case where a before range is not overlapping and is adjacent 
	    Range before = new ContiguousRange(-5,-2);
	    assert(range.isAdjacent(before));
	    assert(!range.intersects(before));
	
	    //Testing the case where an after range is not overlapping and is adjacent 
	    Range after = new ContiguousRange(5,6);
	    assert(range.isAdjacent(after));
	    assert(!range.intersects(after));
	
	    //Testing the case where an after range is not overlapping and is not adjacent 
	    Range wayafter = new ContiguousRange(6,7);
	    assert(!range.isAdjacent(wayafter));
	    assert(!range.intersects(wayafter));
	
	    Range sparseintersecting =  new SparseRange(new ContiguousRange(0, 2), new ContiguousRange(5, 6));
	    assert(range.isAdjacent(sparseintersecting));
	    assert(range.intersects(sparseintersecting));
	    }
	
	
	@Test
	public void testAdd() 
	    {
	    //Testing the addition of a value to the range
	    Range range = new SparseRange(new ContiguousRange(1, 2), new ContiguousRange(4, 5));
	    assert(range.size()==4);
	    Range newrange = range.add(3);
	    assert(newrange.size()==5);
	    }
	
	@Test
	public void testRemove() 
	    {
	    Range range = new SparseRange(new ContiguousRange(1, 2), new ContiguousRange(4, 5));
	    assert(range.size()==4);
	    range = range.remove(2);
	    assert(range.size()==3);
	
	    //testing whether removing a non-existing element changes the size of the range, which it shouldn't
	    range = range.remove(0);
	    assert(range.size()==3);
	    }
	
	@Test
	public void testUnion() 
	    {
	    Range range = new SparseRange(new ContiguousRange(4, 4), new ContiguousRange(2, 2));
	    assert(range.size() == 2);
	    assert(range.getFrom() == 2);
	    assert(range.getTo() == 4);    
	
	    range = range.union(range.union(new ContiguousRange(1, 1)));
	
	    assert (range.size() == 3);
	    assert (range.getFrom() == 1);
	    assert (range.getTo() == 4);    
	
	    //Test making a union of two sparse ranges
	    Range sparserange = new SparseRange(new ContiguousRange(1, 1), new ContiguousRange(3, 3));
	    Range othersparse = new SparseRange(new ContiguousRange(2, 2), new ContiguousRange(4, 4));
	    Range unionrange = sparserange.union(othersparse);
	    assert(unionrange.size() == 4);
	    assert(unionrange.getFrom() == 1);
	    assert(unionrange.getTo() == 4);    
	    }
	
	@Test
	public void testIterator() 
	    {
	    Range range = new SparseRange(new ContiguousRange(4, 4), new ContiguousRange(2, 2));
	
	    assert range.size() == 2;
	    assert range.getFrom() == 2;
	    assert range.getTo() == 4;    
	
	    Iterator<Long> iterator = range.iterator();
	    assert iterator.hasNext() && iterator.next() == 2;
	    assert iterator.hasNext() && iterator.next() == 4;
	    assert !iterator.hasNext();
	
	    range = range.union(new ContiguousRange(3, 3));
	
	    assert range.size() == 3;
	    assert range.getFrom() == 2;
	    assert range.getTo() == 4;    
	
	    iterator = range.iterator();
	    assert iterator.hasNext() && iterator.next() == 2;
	    assert iterator.hasNext() && iterator.next() == 3;
	    assert iterator.hasNext() && iterator.next() == 4;
	    assert !iterator.hasNext();
	
	    //test what happens if we make a union with ourselves
	    range.union(range);
	
	    assert range.size() == 3;
	    assert range.getFrom() == 2;
	    assert range.getTo() == 4;    
	
	    iterator = range.iterator();
	    assert iterator.hasNext() && iterator.next() == 2;
	    assert iterator.hasNext() && iterator.next() == 3;
	    assert iterator.hasNext() && iterator.next() == 4;
	    assert !iterator.hasNext();
	    }
	
	@Test
	public void testEqualsObject() 
	    {
	    //Two sparse ranges with same contiguous ranges are equal
	    Range range = new SparseRange(new ContiguousRange(4, 4), new ContiguousRange(2, 2));
	    Range otherrange = new SparseRange(new ContiguousRange(2, 2), new ContiguousRange(4, 4));
	    assert(range.equals(otherrange));
	
	    //Now test two sparse ranges but with different gaps
	    //They should not compare equal
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(4, 5)); //no 3
	    otherrange = new SparseRange(new ContiguousRange(1, 3), new ContiguousRange(5, 5));//no 4
	    assert(!range.equals(otherrange));
	
	    //Test whether a Sparserange and a contiguous range with the same range are equal
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(3,4)); 
	    // These should merge
	    //to become one ContiguousRange of (1,4)
	    otherrange = new ContiguousRange(1,4);
	    assert(range.equals(otherrange));
	
	    //Test whether a Sparserange and a contiguous range wiht the same range are equal
	    range      = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(4,5)); 
	    otherrange = new SparseRange(new ContiguousRange(1,2),new ContiguousRange(3,4));
	    otherrange = otherrange.add(5);
	    otherrange = otherrange.remove(3);
	    //Now these sparse ranges should consist of equal contiguous ranges
	    assert(range.equals(otherrange));
	
	    //Now we test what happens if we are equal with a contiguous range and then add a non-adjacent value  
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(3,5)); 
	    otherrange = new ContiguousRange(1,5);
	    assert(range.equals(otherrange));
	    range = range.add(7); //Now we really have a sparse range
	    assert(!range.equals(otherrange));
	
	
	    //Now lets test a sparse range that we unionize with a partially overlapping adjacent contiguous range
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(4,5)); 
	    range= range.union(new ContiguousRange(2,4)); //this range is both intersecting the second and adjacent to the first
	    otherrange = new ContiguousRange(1,5);
	    assert(range.equals(otherrange));
	
	
	    //Now lets test a sparse range that we unionize with a partially overlapping adjacent sparse range
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(4,5)); 
	    range = range.union(new ContiguousRange(7,9));
	
	    otherrange = new SparseRange(new ContiguousRange(0,1),new ContiguousRange(6,8));
	    otherrange = otherrange.union(new ContiguousRange(3,3));
	    otherrange= otherrange.union(range); //this range is both intersecting the second and adjacent to the first
	    range = new ContiguousRange(0,9);
	    assert(range.equals(otherrange));
	
	    // Now lets test a sparse range that we unionize with an overlapping adjacent sparse range with different 
	    // number of contiguous ranges
	    range = new SparseRange(new ContiguousRange(1,2), new ContiguousRange(4,5)); 
	    range = range.union(new ContiguousRange(7,9));
	
	    otherrange = new SparseRange(new ContiguousRange(0,3),new ContiguousRange(6,6));
	    otherrange= otherrange.union(range); //this range is both intersecting the second and adjacent to the first
	    range = new ContiguousRange(0,9);
	    assert(range.equals(otherrange));
	    }
	
	
	@Test
	public void testToString() 
	    {
	    //Test that the toString output format stays consistent
	    Range range = new SparseRange(new ContiguousRange(1, 1), new ContiguousRange(4, 4));
	    String str = range.toString();
	    assert(str.equals("SparseRange[[ContiguousRange[1], ContiguousRange[4]]]"));
	
	    //Test the empty range
	    range = new SparseRange();
	    str = range.toString();
	    assert(str.equals("SparseRange[]"));
	    }
	
	
	@Test
	public void testReadExternalPofReader() 
	    throws IOException 
	    {
	    PofReader reader = EasyMock.createMock(PofReader.class);
	    SparseRange range = new SparseRange();
	    
	    EasyMock.expect(reader.readLong(0)).andReturn(1L);
	    EasyMock.expect(reader.readCollection(EasyMock.anyInt(), EasyMock.isA(Collection.class))).andReturn(null);
	    EasyMock.replay(reader);
	    range.readExternal(reader);
	    
	    EasyMock.verify(reader);
	    assert(range.getFrom()==1);
	}
	
	
	@Test
	public void testWriteExternalPofWriter() 
	    throws IOException 
	    {
	    
	    //
	    // Create the mock objects
	    //
	    PofWriter writer = EasyMock.createMock(PofWriter.class);
	    ContiguousRange firstrange = org.easymock.classextension.EasyMock.createMock(ContiguousRange.class);
	    ContiguousRange secondrange = org.easymock.classextension.EasyMock.createMock(ContiguousRange.class);
	    
	    //
	    // Set the expected behavior by collaborator mock objects
	    //
	    
	    org.easymock.classextension.EasyMock.expect(firstrange.isEmpty()).andReturn(false);
	    org.easymock.classextension.EasyMock.expect(secondrange.isEmpty()).andReturn(false);
	    org.easymock.classextension.EasyMock.expect(firstrange.isEmpty()).andReturn(false);
	    org.easymock.classextension.EasyMock.expect(firstrange.isAdjacent(secondrange)).andReturn(false);
	    org.easymock.classextension.EasyMock.expect(firstrange.intersects(secondrange)).andReturn(false);
	    org.easymock.classextension.EasyMock.expect(secondrange.compareTo(firstrange)).andReturn(1);
	    org.easymock.classextension.EasyMock.expect(firstrange.getFrom()).andReturn(1L);
	    
	    writer.writeLong(0,1L); //Now write firstrange.getFrom()
	    writer.writeCollection(EasyMock.anyInt(), EasyMock.isA(Collection.class));
	    
	    //
	    // Set the expected behavior
	    //
	    
	    EasyMock.replay(writer);
	    org.easymock.classextension.EasyMock.replay(firstrange);
	    org.easymock.classextension.EasyMock.replay(secondrange);
	    
	    //
	    // Test execution
	    //
	    
	    SparseRange range = new SparseRange(firstrange, secondrange);
	    range.writeExternal(writer);
	    
	    //
	    // Verification of results
	    //
	    
	    EasyMock.verify(writer);
	    org.easymock.classextension.EasyMock.verify(firstrange);
	    org.easymock.classextension.EasyMock.verify(secondrange);
	    }
	}
