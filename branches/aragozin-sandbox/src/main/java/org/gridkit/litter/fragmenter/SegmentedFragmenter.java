package org.gridkit.litter.fragmenter;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SegmentedFragmenter {
	
	private static TieredChunkSizer BI_LEVEL_SIZER = new TieredChunkSizer();
	static {
		BI_LEVEL_SIZER.setSizers(
				new RandomChunkSizer(16 << 10),
				new RandomChunkSizer(4096),
				new RandomChunkSizer(128));
		BI_LEVEL_SIZER.setWeights(1, 10, 1000);

//		BI_LEVEL_SIZER.setSizers(
//				new RandomChunkSizer(4096),
//				new RandomChunkSizer(128));
//		BI_LEVEL_SIZER.setWeights(1, 256);
	}
	
	int segmentCount;
	GarbageSegment[] segments;
	int[] segmentPositions;
	
	long memoryLimit;
	int segmentLimit;
	Random rnd = new Random(0);
	
	ChunkSizer sizer;
	GarbageSegmentFactory segFactory;
	
	int tickCountLimit = Integer.MAX_VALUE;
	int tickCount;
	
	public static void main(String[] args) {
		
		boolean bit64 = !"x86".equals(ManagementFactory.getOperatingSystemMXBean().getArch());
		System.out.println("Mode: " + (bit64 ? "64 bit" : "32 bit"));
		
		int busyCores = Integer.getInteger("busy-cores", 0);
		if (busyCores > 0) {
			System.out.println("Swapping " + busyCores + " CPU eaters");
			
			while(busyCores > 0) {
				--busyCores;
				
				Thread t = new Thread() {
					public void run() {
						double x = 1;
						double y = 2;
						while(true) {
							x = Math.cos(x + y);
							y = Math.sin(x + y);
						}
					};
				};
				t.setDaemon(true);
				t.start();
			}
		}
		
		SegmentedFragmenter frag = new SegmentedFragmenter();
		frag.segmentCount = 64;
		frag.memoryLimit = 300 << 20;
//		frag.sizer = new RandomChunkSizer(128);
		frag.sizer = BI_LEVEL_SIZER;
				
		if (args.length > 0) {
			frag.tickCountLimit = Integer.parseInt(args[0]);
		}
		
		frag.segFactory = new GarbageSegmentFactory() {
			@Override
			public GarbageSegment newSegment() {
				return new ByteArrayGarbageSegment(256 << 10, 1, 1);
			}
		};
//		frag.segFactory = new GarbageSegmentFactory() {
//			@Override
//			public GarbageSegment newSegment() {
////				return new ByteBufferFragmenter(256 << 10, 1, 1);
//				return new UnsafeFragmenter(256 << 10, 1, 1);
//			}
//		};
		frag.segmentLimit = 256 << 10;
		
		if (bit64) {
			frag.memoryLimit *= 2;
			final ChunkSizer sizer = frag.sizer;
			frag.sizer = new ChunkSizer() {
				@Override
				public int nextChunkSize() {
					return 2 * sizer.nextChunkSize();
				}
			};
		}
		
		frag.run();
		
	}

	private void run() {
		
		segments = new GarbageSegment[segmentCount];
		segmentPositions = new int[segmentCount];
		
		for(int i = 0; i != segments.length; ++i) {
			segments[i] = segFactory.newSegment();
		}
		
		List<byte[]> filler = new ArrayList<byte[]>();
		
		long totalSize = 0;
		while(tickCount < tickCountLimit) {
			
			int chunkSize = sizer.nextChunkSize();
			++tickCount;
			
			if (totalSize + chunkSize > memoryLimit) {
				
				int segNo;
				do {
					segNo = (int) Math.abs(segmentCount / 4 + rnd.nextGaussian() * segmentCount / 4);
				}
				while(segNo >= segmentCount);
			
				System.out.println("Sweep " + segNo + " size " + segments[segNo].totalSize());
				totalSize -= segments[segNo].totalSize();
				segments[segNo].sweep();
				segmentPositions[segNo] = 0;				
			}

			// add
			{
				int segNo;
				do {
					segNo = (int) Math.abs(rnd.nextGaussian() * segmentCount / 2);
				}
				while(segNo >= segmentCount || segmentPositions[segNo] >= segmentLimit);

				segments[segNo].allocate(segmentPositions[segNo]++, chunkSize);
				totalSize += chunkSize;				
			}
			
			if (tickCount % 100000 == 0) {
				System.out.println("Tick (" + tickCount + ") total size " + totalSize);
				for(int i = 0; i != segments.length; ++i) {
					if ((i + 1) % (segmentCount / 4) == 0) {
						if (segmentPositions[i] > 0) {
							int ts = segments[i].totalSize();
							System.out.println(" Seg[" + i + "] -> " + ts + " (" + segmentPositions[i] + ", " + (ts/segmentPositions[i]) + " avg. per chunk)");
						}
					}
				}
			}	
			
			for(int i = 1; i != 31; ++i) {
				filler.add(new byte[chunkSize]);
			}
			if (filler.size() > 200) {
				filler.clear();
			}
		}		
	}

	interface ChunkSizer {
		public int nextChunkSize();
	}
	
	static class TieredChunkSizer implements ChunkSizer {
	
		private Random rnd = new Random(0);

		private int[] weights;
		private RandomChunkSizer[] sizers;
		
		public TieredChunkSizer() {			
		}
		
		public void setWeights(double... w) {
			weights = new int[w.length];
			double sum = 0;
			for(double ws: w) {
				sum += ws;
			}
			for(int i = 0; i != weights.length; ++i) {
				weights[i] = (int)(w[i] * Integer.MAX_VALUE / sum);
			}
		}
		
		public void setSizers(RandomChunkSizer... s) {
			sizers = s.clone();
		}

		@Override
		public int nextChunkSize() {
			int x = rnd.nextInt(Integer.MAX_VALUE);
			for(int i = 0; i != weights.length; ++i) {
				x -= weights[i];
				if (x <= 0) {
					return sizers[i].nextChunkSize();
				}
			}
			return sizers[sizers.length - 1].nextChunkSize();
		}
	}
	
	static class RandomChunkSizer implements ChunkSizer {
		
		private Random rnd = new Random(0);
		private int chunkAvg;
		
		public RandomChunkSizer(int size) {
			this.chunkAvg = size;
		}

		@Override
		public int nextChunkSize() {
			int size = (int) (chunkAvg + rnd.nextGaussian() * chunkAvg / 2);
			if (size < ((chunkAvg + 3) / 4)) {
				size = (chunkAvg + 3) / 4;				
			}
			return size;
		}
	}
	
	interface GarbageSegmentFactory {
		public GarbageSegment newSegment();
	}	
}
