package org.gridkit.coherence.search.lucene.util;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ScoredTopMerger {

	public static DocComparator SCORE_COMPARATOR = new ScoreComparator();
	
	private PriorityQueue<Pointer> pq;
	
	public ScoredTopMerger(DocComparator dc) {
		pq = new PriorityQueue<ScoredTopMerger.Pointer>(8, new PointerComparator(dc));
	}
	
	public void addBlock(ScoredTop block) {
		Pointer pp = new Pointer(block);
		if (!pp.isEmpty()) {
			pq.add(pp);
		}
	}
	
	public boolean getNext(Object[] key, float[] score) {
		Pointer pp = pq.poll();
		if (pp == null) {
			return false;
		}
		else {
			key[0] = pp.getKey();
			score[0] = pp.getScore();
			pp.next();
			if (!pp.isEmpty()) {
				pq.add(pp);
			}
			return true;
		}
	}
	
	private static class PointerComparator implements Comparator<Pointer> {
		
		private DocComparator dc;
		
		public PointerComparator(DocComparator dc) {
			this.dc = dc;
		}

		@Override
		public int compare(Pointer o1, Pointer o2) {
			Object k1 = o1.getKey();
			float s1 = o1.getScore();
			Object a1 = o1.getSortKey();
			Object k2 = o2.getKey();
			float s2 = o2.getScore();
			Object a2 = o2.getSortKey();
			return dc.compare(k1, s1, a1, k2, s2, a2);
		}
	}
	
	private static class Pointer {
		
		private ScoredTop block;
		private int position;
		
		public Pointer(ScoredTop block) {
			this.block = block;
			this.position = 0;
		}

		public void next() {
			++position;
		}
		
		public Object getKey() {
			return block.keys[position];
		}
		
		public float getScore() {
			return block.scores[position];
		}
		
		public Object getSortKey() {
			return block.sortKey == null ? null : block.sortKey[position];
		}
		
		public boolean isEmpty() {
			return position >= block.keys.length;
		}
	}
	
	public interface DocComparator {
		public int compare(Object k1, float s1, Object a1, Object k2, float s2, Object a2);
	}

	public static class ScoreComparator implements DocComparator {

		@Override
		public int compare(Object k1, float s1, Object a1, Object k2, float s2, Object a2) {
			if (s1 > s2) {
				return 1;
			}
			else if (s1 < s2) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
}
