package org.gridkit.coherence.offheap.storage.memlog;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

class PageLogManager {

	private final int pageSize;
	private final int pageUsageLimit;
	
	private AtomicLong memUsed = new AtomicLong();
	private long memUsageLimit;
	
	private ReentrantLock allocationLock = new ReentrantLock();
	private AtomicInteger pagesInUse = new AtomicInteger();
	private AtomicLong top = new AtomicLong();
	private AtomicReferenceArray<ByteChunk> pages;
	private AtomicIntegerArray pageUtilizations;
	private long[] pageTimestamps;
	private boolean[] pagesForCleanUp;
	
	private AtomicInteger pageReleaseCounter = new AtomicInteger();
	
	private float minGcThreshold = 0.2f;
	private float gcThreshold = minGcThreshold;
	
	public PageLogManager(int pageSize, int pageUsageLimit) {
		this.pageSize = pageSize;
		this.pageUsageLimit = pageUsageLimit;
		this.memUsageLimit = ((long)pageSize) * pageUsageLimit;
		
		this.memUsed.set(0);
		this.pagesInUse.set(0);
		
		this.pages = new AtomicReferenceArray<ByteChunk>(pageUsageLimit);
		this.pageUtilizations = new AtomicIntegerArray(pageUsageLimit);
		this.pageTimestamps = new long[pageUsageLimit];
		this.pagesForCleanUp = new boolean[pageUsageLimit];
		
		// allocate first page
		pages.set(0, new ByteChunk(new byte[pageSize]));
		pagesInUse.set(1);
		top.set(16); // to avoid zero pointers
	}
		
	public ByteChunk get(long pointer) {
		validate(pointer);
		int page = (int)(pointer >> 32);
		int offs = (int)pointer;
		if (offs < 16) {
			throw new IllegalArgumentException("Invalid pointer " + Long.toHexString(pointer));
		}
		
		ByteChunk chunk = pages.get(page);
		if (chunk == null) {
			throw new IllegalArgumentException("Broken pointer " + Long.toHexString(pointer) + " page " + Integer.toHexString(page) + " is not allocated");
		}
		int len = chunk.intAt(offs);
		return chunk.subChunk(offs + 4, (len > 0 ? len : -len) - 4);
	}

	public long allocate(int size) {
		if (size + 16 > pageSize) {
			// TODO allocate large objects in heap
			throw new IllegalArgumentException("Size is too large");
		}
		while(true) {
			int len = size;
			len += 4;
			int alen = align(len);
			
			long pp;
			while(true) {
				pp = top.get();
				int offs;
				offs = (int)pp;
				if (offs + alen > pageSize) {
					allocationLock.lock();
					try {
						pp = top.get();
						offs = (int)pp;
						if (offs + alen > pageSize) {
							int page = (int)(pp >> 32);
							top.set(newPage(page + 1) + 16);
							
							if (pageUtilizations.get(page) == 0) {
								pages.set(page, null);
							}							
						}
						else {
							continue;
						}						
					}
					finally {
						allocationLock.unlock();
					}
				}
				else {
					long npp = pp + alen;
					if (top.compareAndSet(pp, npp)) {
						break;
					}
				}
			}
			
			int page = (int)(pp >> 32);
			int offs = (int)pp;
			ByteChunk pageBuf = pages.get(page);
			if (pageBuf == null) {
				new String();
			}
			pageBuf.putInt(offs, len);
			pageUtilizations.addAndGet(page, len);
			memUsed.addAndGet(len);
			return pp;
		}
	}

	private int align(int len) {
		return (len + 0xF) & (0xFFFFFFF0);
	}
	
//	public long store(ByteChunk bytes) {	
//	}
	
	private long newPage(int start) {
		while(true) {
			for(int i = 0; i != pageUsageLimit; ++i) {
				int page = (start + i) % pageUsageLimit; 
				if (pages.get(page) == null) {
					ByteChunk chunk = new ByteChunk(new byte[pageSize]);
					pages.set(page, chunk);
					pagesForCleanUp[page] = false;
					pageTimestamps[page] = System.nanoTime();
					pagesInUse.incrementAndGet();
					return ((long)page) << 32;
				}
			}
			// hit memory limit, should give scavenger some time to
			// recover pages
			System.out.println("Out of pages");
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
		}
	}

	public void release(long pointer) {
		int page = (int)(pointer >> 32);
		int offs = (int)pointer;
		if (offs < 16) {
			throw new IllegalArgumentException("Invalid pointer " + Long.toHexString(pointer));
		}
		
		ByteChunk pageBuf = pages.get(page);
		int len = pageBuf.intAt(offs);
		pageBuf.putInt(offs, -len);
		pageUtilizations.addAndGet(page, -len);
		memUsed.addAndGet(-len);
		
		if (!pagesForCleanUp[page]) {
			checkPageUsage(page);
		}
		
		int tp = (int) (top.get() >> 32);
		if (pageUtilizations.get(page) == 0 && tp != page) {
			pages.compareAndSet(page, pageBuf, null);
			pagesInUse.decrementAndGet();
			pageReleaseCounter.incrementAndGet();
		}
	}

	private void checkPageUsage(int page) {
		int tp = (int) (top.get() >> 32);
		if (page != tp) {
			int usage = pageUtilizations.get(page);
			if (usage < (gcThreshold * pageSize)) {
				pagesForCleanUp[page] = true;
			}
		}
	}

	public boolean isMarkedForRecycle(long pp) {
		int page = (int) (pp >> 32);
		return pagesForCleanUp[page];
	}
	
	public long getMemUseg() {
		return memUsed.get();
	}
	
	// for debug only
	void validate(long pp) {
		return;
//		 int page = (int)(pp >> 32);
//		 int offs = (int)pp;
//		 
//		 if (page == 0) {
//			 return;
//		 }
//		 
//		 if (page <0 || page > pages.length()) {
//			 throw new AssertionError();
//		 }
//		 
//		 ByteChunk buf = pages.get(page);
//		 int roll = 16;
//		 while(true) {
//			 if (roll == offs) {
//				 return;
//			 }
//			 if (roll >offs) {
//				 throw new AssertionError();
//			 }
//			 int size = buf.intAt(roll);
//			 if (size == 0) {
//				 throw new AssertionError();
//			 }
//			 if (size < 0) {
//				 size = -size;
//			 }			 
//			 roll += align(size);
//			 if (roll >offs) {
//				 throw new AssertionError();
//			 }
//		 }
	}

	public void dumpStatistics() {
		StringBuilder buf = new StringBuilder();
		buf.append("Pages allocated: ").append(pagesInUse.get()).append('/').append(pageUsageLimit).append('\n');
		buf.append("Pages freed since last report: ").append(pageReleaseCounter.get()).append('\n');
		buf.append("Memory used: ").append(memUsed.get()).append('/').append(((long)pageUsageLimit) * pageSize).append('\n');
		buf.append("Page utilization: ").append(String.format("%f", ((double)memUsed.get()) / (((double)pagesInUse.get()) * pageSize))).append('\n');
		
		pageReleaseCounter.set(0);
		System.out.println(buf.toString());
	}
}
