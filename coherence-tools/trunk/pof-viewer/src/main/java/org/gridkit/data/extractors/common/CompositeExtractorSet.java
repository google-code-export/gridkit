package org.gridkit.data.extractors.common;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeExtractorSet implements BinaryExtractorSet, Serializable {

	private static final long serialVersionUID = 20130127L;
	
	private List<Batch> batches = new ArrayList<Batch>();
	private List<Composition> compositions = new ArrayList<Composition>();
	private int nOut = 0;
	private int nInternal = 0;

	@Override
	public int addExtractor(BinaryExtractor<?> extractor) {
		return addExtractor(extractor, false);
	}

	private int addExtractor(BinaryExtractor<?> extractor, boolean intermediate) {
		int id = intermediate ? - (++nInternal) : nOut++;
		if (extractor instanceof CompositeExtractor) {
			CompositeExtractor<?> ce = (CompositeExtractor<?>) extractor;
			List<BinaryExtractor<?>> el = ce.getSubExtractors();
			int[] in = new int[el.size()];
			int n = 0;
			for(BinaryExtractor<?> e: el) {
				in[n++] = addExtractor(e, intermediate);
			}
			Composition c = new Composition();
			c.outIndex = n;
			c.inIndexes = in;
			c.extractor = ce;
			compositions.add(c);
			return id;
		}
		else {
			for(Batch batch: batches) {
				if (extractor.isCompatible(batch.extractorSet)) {
					int x = batch.extractorSet.addExtractor(extractor);
					batch.extractors = add(batch.extractors, extractor);
					batch.outIndexes = set(batch.outIndexes, x, id);
					return id;
				}
			}
			// create new batch
			Batch batch = new Batch();
			batches.add(batch);
			batch.extractors = add(batch.extractors, extractor);
			batch.extractorSet = extractor.newExtractorSet();
			int x = batch.extractorSet.addExtractor(extractor);
			batch.outIndexes = set(batch.outIndexes, x, id);
			return id;
		}
	}

	@SuppressWarnings("rawtypes")
	private BinaryExtractor<?>[] add(BinaryExtractor<?>[] extractors, BinaryExtractor<?> extractor) {
		if (extractors == null) {
			BinaryExtractor<?>[] r = new BinaryExtractor[1];
			r[0] = (BinaryExtractor) extractor;
			return r;
		}
		else {
			extractors = Arrays.copyOf(extractors, extractors.length + 1);
			extractors[extractors.length - 1] = extractor;
			return extractors;
		}
	}

	private int[] set(int[] array, int x, int id) {
		if (array == null) {
			array = new int[x + 1];
		}
		else if (array.length <= x) {
			array = Arrays.copyOf(array, x + 1);
		}
		array[x] = id;
		
		return array;
	}

	@Override
	public int getSize() {
		return nOut;
	}

	@Override
	public void compile() {
		for(Batch batch: batches) {
			batch.extractorSet.compile();
		}
	}
	
	@Override
	public void extractAll(ByteBuffer buffer, ResultVectorReceiver resultReceiver) {
		final Object[] result = new Object[nOut];
		final Object[] internal = new Object[nInternal];
		
		for(Batch batch: batches) {
			batch.exec(buffer, result, internal);
		}
		
		for(Composition c: compositions) {
			c.exec(result, internal);
		}
		
		for(int i = 0; i != result.length; ++i) {
			resultReceiver.push(i, result[i]);
		}
	}
	
	private static class Batch {
		
		BinaryExtractor<?>[] extractors;
		int[] outIndexes;
		BinaryExtractorSet extractorSet;

		
		public void exec(ByteBuffer buffer, final Object[] result, final Object[] internal) {
			extractorSet.extractAll(buffer, new ResultVectorReceiver() {
				@Override
				public void push(int id, Object part) {
					int n = outIndexes[id];
					if (n >= 0) {
						result[n] = part;
					}
					else {
						internal[- (n + 1)] = part;
					}
				}
			});
		}
	}
	
	private static class Composition {
		
		int outIndex;
		int[] inIndexes;
		CompositeExtractor<?> extractor;
		
		public void exec(Object[] result, Object[] internal) {
			Object[] args = new Object[inIndexes.length];
			for(int i = 0; i != inIndexes.length; ++i) {
				int n = inIndexes[i];
				if (n >= 0) {
					args[i] = result[n];
				}
				else {
					args[i] = internal[- (n + 1)];
				}
			}
			Object v = extractor.extract(args);
			if (outIndex >= 0) {
				result[outIndex] = v;
			}
			else {
				internal[- (outIndex + 1)] = v;
			}
		}
	}
}
