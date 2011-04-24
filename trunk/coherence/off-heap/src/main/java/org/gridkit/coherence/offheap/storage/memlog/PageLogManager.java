package org.gridkit.coherence.offheap.storage.memlog;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import com.tangosol.dev.assembler.Aload;

class PageLogManager {

	private final static int PAGE_HEADER = 32; // leaves 0 and 1 pointers as special values
	
	private final int pageSize;
	private final int pageUsageLimit;
	private final int offsetMask;
	private final int pageShift;
	
	private AtomicLong memUsed = new AtomicLong();
	private long memUsageLimit;
	
	private ReentrantLock[] allocationLock;
	private AtomicInteger pagesInUse = new AtomicInteger();
	private AtomicInteger[] top;
	private AtomicReferenceArray<ByteChunk> pages;
	private AtomicIntegerArray pageUtilizations;
	private AtomicInteger evacuationQueueLength = new AtomicInteger(); 
	private int evacuationQueueLimit; 
	private long[] pageTimestamps;
	private volatile boolean[] pagesForCleanUp;
	
	private AtomicInteger pageReleaseCounter = new AtomicInteger();
	
	private float scavengeGcThreshold = 0.8f;
	private float minGcThreshold = 0.4f;
	private float gcThreshold = minGcThreshold;
	
	private volatile int fence;
	
	public PageLogManager(int pageSize, int pageUsageLimit, int allocNumber) {
		this.pageSize = pageSize;
		if (pageSize != Integer.highestOneBit(pageSize) || pageSize > 1 << 30) {			
			throw new IllegalArgumentException("Invalid page size " + pageSize + ", valid page size should be power of 2 and no more than 1Gb");
		}
		this.offsetMask = pageSize - 1;
		this.pageShift = Integer.bitCount(offsetMask);
		if (1l * pageSize * pageUsageLimit > 32l << 30) {
			throw new IllegalArgumentException("Single manager cannot handle more than 32Gb of memory");
		}
		
		this.pageUsageLimit = pageUsageLimit;
		this.memUsageLimit = ((long)pageSize) * pageUsageLimit;
		
		this.memUsed.set(0);
		this.pagesInUse.set(0);
		
		this.pages = new AtomicReferenceArray<ByteChunk>(pageUsageLimit);
		this.pageUtilizations = new AtomicIntegerArray(pageUsageLimit);
		this.pageTimestamps = new long[pageUsageLimit];
		this.pagesForCleanUp = new boolean[pageUsageLimit];
		
//		evacuationQueueLimit = pageUsageLimit / 16;
//		evacuationQueueLimit = evacuationQueueLimit < 2 ? 2 : evacuationQueueLimit;
		evacuationQueueLimit = pageUsageLimit;
		
		// allocate first page
		allocationLock = new ReentrantLock[allocNumber];
		top = new AtomicInteger[allocNumber];
		for(int i = 0; i!= allocNumber; ++i) {
			allocationLock[i] = new ReentrantLock(); 
			top[i] = new AtomicInteger();
			top[i].set(pointer(newPage(i), PAGE_HEADER));
		}
	}
	
	int page(int pointer) {
		int page = (0x7FFFFFFF & pointer) >> pageShift;
		return page;
	}

	int offset(int pointer) {
		int offs = (offsetMask & pointer) << 4;
		return offs;
	}
	
	int size(int sv) {
		return 0x7FFFFFFF & sv;
	}
	
	boolean erased(int sv) {
		return (0x80000000 & sv) != 0;
	}
	
	int pointer(int page, int offset) {
		int pointer = offsetMask & (offset >> 4);
		pointer |= (0x7FFFFFFF & (page << pageShift));
		return pointer;
	}
	
	public ByteChunk get(int pointer) {
		validate(pointer);
		int page = page(pointer);
		int offs = offset(pointer);
		if (offs < PAGE_HEADER) {
			throw new IllegalArgumentException("Invalid pointer " + Long.toHexString(pointer));
		}
		
		ByteChunk chunk = pages.get(page);
		if (chunk == null) {
			throw new IllegalArgumentException("Broken pointer " + Long.toHexString(pointer) + " page " + Integer.toHexString(page) + " is not allocated");
		}
		int len = size(chunk.intAt(offs));
		return chunk.subChunk(offs + 4, len - 4);
	}

	public int allocate(int size, int allocNo) {
		if (size > pageSize >> 2) {
			// TODO allocate large objects in heap
			throw new IllegalArgumentException("Size is too large");
		}
		while(true) {
			int len = size;
			len += 4;
			int alen = align(len);
			
			int pp;
			while(true) {
				pp = top[allocNo].get();
				int offs;
				offs = offset(pp);
				if (offs + alen > pageSize) {
					allocationLock[allocNo].lock();
					try {
						pp = top[allocNo].get();
						offs = offset(pp);
						if (offs + alen > pageSize) {
							int page = page(pp);
							top[allocNo].set(pointer(newPage(page + 1),PAGE_HEADER));
							if (pageUtilizations.get(page) == 0) {
								ByteChunk oldPage = pages.getAndSet(page, null);
								if (oldPage != null) {
									pagesInUse.decrementAndGet();
									pageReleaseCounter.incrementAndGet();
								}
							}
						}
						else {
							continue;
						}						
					}
					finally {
						allocationLock[allocNo].unlock();
					}
				}
				else {
					int npp = pointer(page(pp), offs + alen);
					if (top[allocNo].compareAndSet(pp, npp)) {
						break;
					}
				}
			}
			
			int page = page(pp);
			int offs = offset(pp);
			ByteChunk pageBuf = pages.get(page);
			// TODO debug
			if (pageBuf == null) {
				new String();
			}
			pageBuf.putInt(offs, len);
			pageUtilizations.addAndGet(page, len);
			memUsed.addAndGet(len);			
			fence += 2;
			validate(pp);
			return pp;
		}
	}

	private int align(int len) {
		return (len + 0xF) & (0xFFFFFFF0);
	}
	
	private int newPage(int start) {
		while(true) {
			for(int i = 0; i != pageUsageLimit; ++i) {
				int page = (start + i) % pageUsageLimit; 
				if (pages.get(page) == null) {
					ByteChunk chunk = new ByteChunk(new byte[pageSize]);
					pagesForCleanUp[page] = false;
					pages.set(page, chunk);
					pageTimestamps[page] = System.nanoTime();
					pagesInUse.incrementAndGet();
					return page;
				}
			}
			// hit memory limit, should give scavenger some time to
			// recover pages
			System.out.println("Out of pages");
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
		}
	}

	public void release(int pointer) {
		int page = page(pointer);
		int offs = offset(pointer);
		if (offs < PAGE_HEADER) {
			throw new IllegalArgumentException("Invalid pointer " + Long.toHexString(pointer));
		}
		if (page >= pages.length()) {
			// TODO allocate large objects in heap
			throw new IllegalArgumentException("Invalid pointer " + Integer.toHexString(pointer));
		}

		
		ByteChunk pageBuf = pages.get(page);
		int len = pageBuf.intAt(offs);
		pageUtilizations.addAndGet(page, -len);
		memUsed.addAndGet(-len);
		
		if (!pagesForCleanUp[page]) {
			checkPageUsage(page);
		}
		
		int allocNo = -1;
		for(int j = 0; j != top.length; ++j) {
			if (page(top[j].get()) == page) {
				allocNo = j;
				break;
			}
		}
		// TODO is race condition possible ?
		if (pageUtilizations.get(page) == 0 && allocNo == -1) {
			if (pages.compareAndSet(page, pageBuf, null)) {
				pagesInUse.decrementAndGet();
				pageReleaseCounter.incrementAndGet();
				if (pagesForCleanUp[page]) {
					evacuationQueueLength.decrementAndGet();
				}
			}
		}

		// mark chunk as deleted
		pageBuf.putInt(offs, 0x80000000 | len);
	}

	private void checkPageUsage(int page) {
		int allocNo = -1;
		for(int j = 0; j != top.length; ++j) {
			if (page(top[j].get()) == page) {
				allocNo = j;
				break;
			}
		}
		if (allocNo == -1) {
			int usage = pageUtilizations.get(page);
			if (usage < (gcThreshold * pageSize)) {
				int ql = evacuationQueueLength.get(); 
				if (pages.get(page) != null && ql < evacuationQueueLimit) {
					if (evacuationQueueLength.compareAndSet(ql, ql + 1)) {
						pagesForCleanUp[page] = true;
					}
				}
			}
		}
	}
	
	public int nextEvacuation(int pointer) {
		int next;
		if (pointer == 0 || (next = nextChunk(pointer)) == 0) {
			int page = choosePageToEvacuate();
			if (page == -1) {
				return 0;
			}
			else {
				int pp = pointer(page, PAGE_HEADER);
				next = nextEvacuation(pp);
			}
		}
		return next;
	}

	private int choosePageToEvacuate() {
		int page = -1;
		int minUsed = 0;
		for (int i = 0; i != pages.length(); ++i) {
			if (pageUtilizations.get(i) == 0) {
				continue;
			}
			boolean tp = false;
			for (int j = 0; j != top.length; ++j) {
				if (page(top[j].get()) == page) {
					tp = true;
					break;
				}
			}
			if (!tp) {
				int usage = pageUtilizations.get(i);
				if (minUsed > usage) {
					minUsed = usage;
					page = i;
				}
			}
		}
		
		if (minUsed < scavengeGcThreshold * pageSize) {
			return page;
		}
		
		return -1;
	}

	private int nextChunk(int pointer) {
		validate(pointer);
		
		int page = page(pointer);
		int offs = offset(pointer);
		
		ByteChunk pageBuf = pages.get(page);
		
		int len = align(size(pageBuf.intAt(offs)));
		offs += len;
		
		while(offs < pageSize) {
			int sv = pageBuf.intAt(offs);
			if (sv == 0) {
				break;
			}
			else {
				if (erased(sv)) {
					offs += align(size(sv));
					continue;
				}
				else {
					return offs;
				}
			}
		}		
		return 0;
	}
	
	public boolean isMarkedForRecycle(int pp) {
		int page = page(pp);
		if ((page < 0) || (page > pagesForCleanUp.length)) {
			// for debug
			new String();
		}
		return pagesForCleanUp[page];
	}
	
	public long getMemUsage() {
		return memUsed.get();
	}
	
	// for debug only
	void validate(int pp) {
		if (true) {
			return;
		}
		else {
			int page = page(pp);
			int offs = offset(pp);
			 
//			if (page == 0) {
//				return;
//			}
			 
			if (page <0 || page > pages.length()) {
				throw new AssertionError();
			}
			 
			ByteChunk buf = pages.get(page);
			int roll = PAGE_HEADER;
			int oldRoll = 0;
			while(true) {
				// forcing memory fence
				synchronized(buf) {
					if (roll == offs) {
						int size = buf.intAt(roll);
						if (align(size) + offs > pageSize) {
							throw new AssertionError();
						}
						return;
					}
					if (roll >offs) {
						throw new AssertionError();
					}
					int size = size(buf.intAt(roll));
					if (size == 0) {
						throw new AssertionError();
					}
					oldRoll = roll;
					roll += align(size);
					if (roll >offs) {
						throw new AssertionError();
					}
				}
			}
		}
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
