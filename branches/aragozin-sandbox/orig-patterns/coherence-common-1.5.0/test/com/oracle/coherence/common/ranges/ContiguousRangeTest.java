/*
 * File: ContiguousRangeTest.java
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

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

/**
 * <p>Unit tests for the {@link ContiguousRange} implementation.</p>
 * 
 * @author Christer Fahlgren
 */
public class ContiguousRangeTest
    {
        @Test
        public void testContiguousRangeLongLong()
            {
            Range range = new ContiguousRange(5);
            assert(range.getFrom()==5);
            assert (range.isEmpty());
            assert(!range.isSingleton());
            }

        @Test
        public void testContiguousRange()
            {
            Range range = new ContiguousRange();
            assert(range.getFrom()==0);
            assert (range.isEmpty());
            assert(!range.isSingleton());
            }

        @Test
        public void testAdd()
            {
            Range range = new ContiguousRange(1,5);
          
            //Just add one number that is adjacent
            range = range.add(6);
            assert (range.size()==6);
            
            //Just add one number that is adjacent
            range = range.add(0);
            assert (range.size()==7);
            
            //Just add one number that already exists
            range = range.add(0);
            assert (range.size()==7);
            
            //Add another one that is not adjacent
            range = range.add(8);
            assert (range.size()==8);
            assert (range instanceof SparseRange);
            
            }

  
        @Test
        public void testUnion()
            {
            Range range = new ContiguousRange(1,5);
            
            assert(range.size()==5);

            //make union with empty range
            range = range.union(new ContiguousRange());
            assert(range.size()==5);
            
            //make union with non adjacent range
            Range other = new ContiguousRange(7,8);
            range = range.union(other);
            assert (range.size()==7);
            assert (range instanceof SparseRange);
            assert (!range.contains(6));
            
            //Make union with other sparserange
            range = new ContiguousRange(1,5);
            other = new SparseRange(new ContiguousRange(7,8),new ContiguousRange(9,10));
            range = range.union(other);
            assert(range.size()==9);
            assert (range instanceof SparseRange);
            
            
            }
        @Test
        public void testCompareTo() 
            {
            ContiguousRange range = new ContiguousRange(1,5);
            ContiguousRange other = new ContiguousRange(6,10);
            assert(range.compareTo(other) == -1);
            other = new ContiguousRange(-5,0);
            assert(range.compareTo(other) == 1);
            /*other = new ContiguousRange(1,5);
            assert(range.compareTo(other) == 0);
            */
            other = new ContiguousRange(1,3);
            try
                {
                range.compareTo(other);
                assert(false);
                }
            catch(NotComparableRuntimeException e)
                {
                assert(true);
                }
          
            }
            

        @Test
        public void testWriteExternalPofWriter() 
            throws IOException 
            {
            
            //
            // Create the mock objects
            //
            PofWriter writer = EasyMock.createMock(PofWriter.class);
             
            //
            // Set the expected behavior by collaborator mock objects
            //
            
            
            writer.writeLong(0,0L); //Now write range.getFrom()
            writer.writeLong(1,1L); //Now write range.getTo()
             
            //
            // Set the expected behavior
            //
            
            EasyMock.replay(writer);
            
            //
            // Test execution
            //
            
            ContiguousRange range = new ContiguousRange (0,1);
            range.writeExternal(writer);
            
            //
            // Verification of results
            //
            
            EasyMock.verify(writer);
            }

        @Test
        public void testReadExternalPofWriter() 
            throws IOException 
            {
            
            //
            // Create the mock objects
            //
            PofReader reader = EasyMock.createMock(PofReader.class);
             
            //
            // Set the expected behavior by collaborator mock objects
            //
            
            
            EasyMock.expect(reader.readLong(0)).andReturn(0L); //Now write range.getFrom()
            EasyMock.expect(reader.readLong(1)).andReturn(5L); //Now write range.getFrom()
             
            //
            // Set the expected behavior
            //
            
            EasyMock.replay(reader);
            
            //
            // Test execution
            //
            
            ContiguousRange range = new ContiguousRange ();
            range.readExternal(reader);
            assert(range.getFrom()==0);
            assert(range.getTo()==5);
            
            
            //
            // Verification of results
            //
            
            EasyMock.verify(reader);
            }
    }
