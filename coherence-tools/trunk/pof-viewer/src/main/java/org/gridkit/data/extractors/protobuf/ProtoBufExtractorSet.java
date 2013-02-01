package org.gridkit.data.extractors.protobuf;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gridkit.data.extractors.common.BinaryExtractor;
import org.gridkit.data.extractors.common.BinaryExtractorSet;
import org.gridkit.data.extractors.common.CompositeExtractorSet;
import org.gridkit.data.extractors.common.ResultVectorReceiver;

public class ProtoBufExtractorSet implements BinaryExtractorSet, Serializable {

	private int numExtractors;
	private Entry root = new Entry(null, 0);
	
	@Override
	public int addExtractor(BinaryExtractor<?> extractor) {
		return root.addExtractor((ProtoBufExtractor<?>) extractor);
	}
	
	@Override
	public void compile() {
		root.prepare();		
	}

	@Override
	public int getSize() {
		return numExtractors;
	}

	@Override
	public void extractAll(ByteBuffer buffer, ResultVectorReceiver resultReceiver) {
		
	}
	
	private static int[] set(int[] array, int index, int value) {
		if (array == null) {
			array = new int[index + 1];
			Arrays.fill(array, -1);
		}
		else if (array.length <= index) {
			int n = array.length;
			array = Arrays.copyOf(array, index + 1);
			Arrays.fill(array, n, array.length, -1);
		}
		array[index] = value;
		
		return array;
	}


	private class Entry implements Serializable, Comparable<Entry> {
		
		private static final long serialVersionUID = 20130127L;
		
		private final Entry parent;
		private final int pbIndex;
		
		private SortedMap<Integer, Entry> childEntries = new TreeMap<Integer, ProtoBufExtractorSet.Entry>();

		private int[] outIndexes;
		private CompositeExtractorSet composite = new CompositeExtractorSet();
		private Map<ProtoBufExtractor<?>, Integer> leafs = new HashMap<ProtoBufExtractor<?>, Integer>();
		
		public Entry(Entry parent, int pbIndex) {
			this.parent = parent;
			this.pbIndex = pbIndex;
		}
		
		@Override
		public int compareTo(Entry o) {
			return pbIndex - o.pbIndex;
		} 
		
		public int addExtractor(ProtoBufExtractor<?> extractor) {
			if (extractor.isLeaf()) {
				if (extractor.getNestedExtractor() == null) {
					if (leafs.containsKey(extractor)) {
						return leafs.get(extractor);
					}
					else {
						int id = numExtractors++;
						leafs.put(extractor, id);
						return id;
					}
				}
				else {
					int cid = composite.addExtractor(extractor.getNestedExtractor());
					if (outIndexes != null && outIndexes.length > cid && outIndexes[cid] != -1) {
						return outIndexes[cid];
					}
					else {
						int id = numExtractors++;
						outIndexes = set(outIndexes, cid, id);
						return id;
					}
				}
			}
			else {
				int pbi = extractor.getPrefix();
				Entry child = childEntries.get(pbi);
				if (child == null) {
					child = new Entry(this, pbi);
					childEntries.put(pbi, child);
				}
				return child.addExtractor(extractor.trim());
			}
		}
		
		public void prepare() {
			if (composite != null) {
				composite.compile();
			}
			if (childEntries != null) {
				for(Entry child: childEntries.values()) {
					child.prepare();
				}
			}
		}
	}	
}
