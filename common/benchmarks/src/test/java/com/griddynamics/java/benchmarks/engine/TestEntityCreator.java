package com.griddynamics.java.benchmarks.engine;

import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import com.griddynamics.java.benchmarks.model.Group;
import com.griddynamics.java.benchmarks.model.Storage;
import com.griddynamics.java.benchmarks.model.Entity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: akondratyev
 */
public class TestEntityCreator {

    @Test(timeout=1000)
    public void testCreation() throws Exception {
        Storage storageMock = mock(Storage.class);
        final Group groupMock = mock(Group.class);
        when(groupMock.getMaxObjectsCount()).thenReturn(1);
        when(groupMock.getObjectsCount()).thenReturn(0);
        when(groupMock.getObjectSize()).thenReturn(1);
        when(groupMock.getStorage()).thenReturn(storageMock);
        doAnswer(new Answer() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                when(groupMock.getObjectsCount()).thenReturn(1);
                return groupMock;
            }
        }).when(groupMock).incObjectsCount();

        ConcurrentLinkedQueue<Group> groups = mock(ConcurrentLinkedQueue.class);
        when(groups.poll()).thenReturn(groupMock);
        when(groups.peek()).thenReturn(groupMock);
        when(groups.offer((Group)anyObject())).thenReturn(true);

        TickCounter tickCounterMock = mock(TickCounter.class);
        when(tickCounterMock.getTicks()).thenReturn(0);

        EntityCreator creator = new EntityCreator(groups, tickCounterMock);
        new Thread(creator).start();

        while(groupMock.getObjectsCount() != 1) {
            Thread.yield();
        }
        verify(groupMock).getStorage();
        verify(groupMock).incObjectsCount();
        verify(storageMock).push(anyInt(), (Entity) anyObject());
    }
}
