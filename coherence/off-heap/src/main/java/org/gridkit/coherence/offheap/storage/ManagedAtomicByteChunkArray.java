package org.gridkit.coherence.offheap.storage;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ManagedAtomicByteChunkArray implements DynamicAtomicArray<byte[]> {
	
	private static int CONCURENCY_FACTOR = 16;
	
	private PageManager manager;
	private ReentrantReadWriteLock resizeLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock memLocks[];

	private int chunkSize;
	private int chuckPerPage;
	private volatile int[] rootPages;
	private volatile int lenght;
	
	public ManagedAtomicByteChunkArray(PageManager manager, int chunkSize) {
		this.manager = manager;
		this.memLocks = new ReentrantReadWriteLock[CONCURENCY_FACTOR];
		for(int i = 0; i != memLocks.length; ++i) {
			memLocks[i] = new ReentrantReadWriteLock();
		}
		this.chunkSize = chunkSize;
		chuckPerPage = manager.getPageSize() / chunkSize;
		rootPages = new int[0];
		lenght = 0;
	}
	
	@Override
	public void setLength(int length) {
		int pages = chuckPerPage;
		if (rootPages.length != pages) {
			int[] newRootPages = Arrays.copyOf(rootPages, pages);
			
			if (pages > rootPages.length) {
				for(int i = rootPages.length; i != newRootPages.length; ++i) {
					newRootPages[i] = manager.allocate();
				}
			}
			int[] oldRootPages = rootPages;
			if (newRootPages.length < rootPages.length) {
				this.lenght = length;
				rootPages = newRootPages;
			}
			else {
				rootPages = newRootPages;
				this.lenght = length;
			}
				
			if (pages < oldRootPages.length) {				
				resizeLock.writeLock().lock();
				try {
					for(int i = pages; i != oldRootPages.length; ++i) {
						manager.release(oldRootPages[i]);
					}
				}
				finally {
					resizeLock.writeLock().unlock();
				}
			}
		}
	}

	@Override
	public boolean compareAndSet(int i, byte[] expect, byte[] update) {
		if (expect.length != chunkSize || update.length != chunkSize) {
			throw new IllegalArgumentException("Byte array size doesn't match chunk size");
		}
		if (i > lenght) {
			throw new ArrayIndexOutOfBoundsException("index: " + i + " size: " + lenght);
		}
		else {
			Lock lock = memLocks[i % memLocks.length].writeLock();
			try {
				resizeLock.readLock().lock();
				lock.lock();
				int page = i / chuckPerPage;
				int offs = (i % chuckPerPage) * chunkSize;
				byte[] chunk = manager.read(page, offs, chunkSize);
				if (Arrays.equals(chunk, expect)) {
					manager.write(page, offs, update);
					return true;
				}
			}
			finally{
				resizeLock.readLock().unlock();
				lock.unlock();
			}
		}
		return false;
	}

	@Override
	public byte[] get(int i) {
		if (i > lenght) {
			throw new ArrayIndexOutOfBoundsException("index: " + i + " size: " + lenght);
		}
		else {
			Lock lock = memLocks[i % memLocks.length].readLock();
			try {
				resizeLock.readLock().lock();
				lock.lock();
				int page = i / chuckPerPage;
				int offs = (i % chuckPerPage) * chunkSize;
				byte[] chunk = manager.read(page, offs, chunkSize);
				return chunk;
			}
			finally{
				resizeLock.readLock().unlock();
				lock.unlock();
			}
		}
	}

	@Override
	public byte[] getAndSet(int i, byte[] newValue) {
		if (newValue.length != chunkSize) {
			throw new IllegalArgumentException("Byte array size doesn't match chunk size");
		}
		if (i > lenght) {
			throw new ArrayIndexOutOfBoundsException("index: " + i + " size: " + lenght);
		}
		else {
			Lock lock = memLocks[i % memLocks.length].writeLock();
			try {
				resizeLock.readLock().lock();
				lock.lock();
				int page = i / chuckPerPage;
				int offs = (i % chuckPerPage) * chunkSize;
				byte[] chunk = manager.read(page, offs, chunkSize);
				manager.write(page, offs, newValue);
				return chunk;
			}
			finally{
				resizeLock.readLock().unlock();
				lock.unlock();
			}
		}
	}

	@Override
	public void lazySet(int i, byte[] newValue) {
		set(i, newValue);
	}

	@Override
	public int length() {
		return lenght;
	}

	@Override
	public void set(int i, byte[] newValue) {
		if (newValue.length != chunkSize) {
			throw new IllegalArgumentException("Byte array size doesn't match chunk size");
		}
		if (i > lenght) {
			throw new ArrayIndexOutOfBoundsException("index: " + i + " size: " + lenght);
		}
		else {
			Lock lock = memLocks[i % memLocks.length].writeLock();
			try {
				resizeLock.readLock().lock();
				lock.lock();
				int page = i / chuckPerPage;
				int offs = (i % chuckPerPage) * chunkSize;
				byte[] chunk = manager.read(page, offs, chunkSize);
				manager.write(page, offs, newValue);
			}
			finally{
				resizeLock.readLock().unlock();
				lock.unlock();
			}
		}
	}

	@Override
	public boolean weakCompareAndSet(int i, byte[] expect, byte[] update) {
		return compareAndSet(i, expect, update);
	}
}
