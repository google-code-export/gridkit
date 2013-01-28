package org.gridkit.data.extractors.protobuf;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.gridkit.data.extractors.common.BinaryExtractor;
import org.gridkit.data.extractors.common.BinaryExtractorSet;
import org.gridkit.data.extractors.common.CompositeExtractorSet;
import org.gridkit.data.extractors.common.ResultVectorReceiver;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class ProtoBufExtractorSet implements BinaryExtractorSet, Serializable {

	private int numExtractors;
	private Entry root;
	
	@Override
	public int addExtractor(BinaryExtractor<?> extractor) {
		ProtoBufExtractor<?> pbx = (ProtoBufExtractor<?>) extractor;
		int[] path = pbx.getPath();
		return 0;
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

	private static class Entry implements Serializable, Comparable<Entry> {
		
		private static final long serialVersionUID = 20130127L;
		
		private final Entry parent;
		private final int attrIndex;
		
		private List<Entry> childEntries;
		private 
		
		private CompositeExtractorSet extractor;
		
		public Entry(Entry parent, int attrIndex) {
			this.parent = parent;
			this.attrIndex = attrIndex;
		}
		
		@Override
		public int compareTo(Entry o) {
			return attrIndex - o.attrIndex;
		} 
		
		public void prepare() {
			if (childEntries != null) {
				Collections.sort(childEntries);
				for(Entry child: childEntries) {
					child.prepare();
				}
			}
			if (processors != null) {
				for(Processor processor: processors) {
					processor.prepare();
				}
			}
		}
	}
	
	private static abstract class Processor implements Serializable {
		
		public abstract void init(ResultWrapper receiver);

		public abstract void prepare();
		
		public abstract void process(ResultWrapper receiver, int wireType, ByteBuffer buffer) throws IOException;
		
	}
	
	private static class ScalarProcessor extends Processor {

		private final int outIndex;
		private final ProtoBufExtractor<?> extractor;
		
		public ScalarProcessor(int outIndex, ProtoBufExtractor<?> extractor) {
			this.outIndex = outIndex;
			this.extractor = extractor;
		}

		@Override
		public void prepare() {
			// do nothing
		}

		@Override
		public void init(ResultWrapper receiver) {
		}

		@Override
		public void process(ResultWrapper receiver, int wireType, ByteBuffer buffer) throws IOException {
			receiver.push(outIndex, extractor.decode(wireType, buffer));			
		}
	}

	private static class ListProcessor extends Processor {
		
		private final int outIndex;
		private final ProtoBufExtractor<?> extractor;
		
		public ListProcessor(int outIndex, ProtoBufExtractor<?> extractor) {
			this.outIndex = outIndex;
			this.extractor = extractor;
		}
		
		@Override
		public void prepare() {
		}

		@Override
		public void init(ResultWrapper receiver) {
			receiver.createList(outIndex);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void process(ResultWrapper receiver, int wireType, ByteBuffer buffer) throws IOException {
			receiver.push(outIndex, extractor.decode(wireType, buffer));			
		}
	}
	
	private static class AlienProcessor extends Processor {
		
		private int[] outIndexes;
		private int[] groupIndexes;
		private BinaryExtractorSet extractorSet;

		@Override
		public void prepare() {
			extractorSet.compile();			
		}
		
		@Override
		public void init(ResultWrapper receiver) {
			for(int i: groupIndexes) {
				receiver.createList(i);
			}
		}
		
		@Override
		public void process(final ResultWrapper receiver, int wireType, ByteBuffer buffer) throws IOException {
			int wireFormat = wireType & 0x7;
			if (wireFormat == WireFormat.WIRETYPE_LENGTH_DELIMITED) {
				CodedInputStream cis = PBHelper.inputStream(buffer);
				int len = cis.readInt32();
				int offs = cis.getTotalBytesRead();
				ByteBuffer bb = buffer.slice();
				bb.position(offs);
				bb.limit(offs + len);
				extractorSet.extractAll(bb, new ResultVectorReceiver() {
					
					@Override
					public void push(int id, Object part) {
						receiver.push(outIndexes[id], part);
					}
				});
			}
		}
	}
	
	private static class ResultWrapper implements ResultVectorReceiver {
		
		private final Object[] buffer;
		private final boolean[] collections;
		
		public ResultWrapper(int size) {
			buffer = new Object[size];
			collections = new boolean[size];
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void push(int id, Object part) {
			if (collections[id]) {
				((Collection)buffer[id]).add(part);
			}
			else {
				if (buffer[id] == null) {
					buffer[id] = part;
				}
			}
		}
		
		public void createList(int id) {
			collections[id] = true;
			buffer[id] = new ArrayList<Object>();
		}
	}
}
