package org.gridkit.coherence.search.comparation.subquery;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.OpenBitSet;

@SuppressWarnings("deprecation")
public class FilteredQuery extends Query {

	private static final long serialVersionUID = 1L;

	private Term[] maskTerms;
	private Query query;
	
	public FilteredQuery(Query q, Term... terms) {
		this.query = q;
		this.maskTerms = terms;
	}

	@Override
	public String toString(String field) {
		return query.toString(field) + "|" + Arrays.toString(maskTerms);
	}

	@Override
	@SuppressWarnings("serial")
	public Weight createWeight(final Searcher searcher) throws IOException {
		final Weight w = query.createWeight(searcher);
		
		return new Weight() {
			
			@Override
			public float sumOfSquaredWeights() throws IOException {
				return w.sumOfSquaredWeights();
			}
			
			@Override
			public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,	boolean topScorer) throws IOException {
				return w.scorer(maskReader(reader), scoreDocsInOrder, topScorer);
			}
			
			@Override
			public void normalize(float norm) {
				w.normalize(norm);				
			}
			
			@Override
			public float getValue() {
				return w.getValue();
			}
			
			@Override
			public Query getQuery() {
				return w.getQuery();
			}
			
			@Override
			public Explanation explain(IndexReader reader, int doc) throws IOException {
				return w.explain(maskReader(reader), doc);
			}
		};
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		Query newQuery = query;
		while(true) {
			Query q = newQuery.rewrite(reader);
			if (q == newQuery) {
				break;
			}
			newQuery = q;
		}

		if (newQuery != query) {
			return new FilteredQuery(newQuery, maskTerms);
		}
		else {
			return this;
		}
	}
	
	public IndexReader maskReader(IndexReader reader) throws IOException {
		OpenBitSet base = null;
		for(Term term: maskTerms) {
			OpenBitSet result = new OpenBitSet(reader.maxDoc());
			TermDocs td = reader.termDocs(term);
			while(td.next()) {
//					System.out.println("mask: " + td.doc());
				result.fastSet(td.doc());
			}
			if (base == null) {
				base = result;
			}
			else {
				base.and(result);
			}
		}
		
//		System.out.println("Index mask for " + reader + " - " + base.cardinality());
		
		MaskedIndexReader mr = new MaskedIndexReader(reader, base);
		
		return mr;
	}
	
	private static class MaskedIndexReader extends FilterIndexReader {
		
		private OpenBitSet docMask;
		
		public MaskedIndexReader(IndexReader reader, OpenBitSet docMask) {
			super(reader);
			this.docMask = docMask;
		}

		@Override
		public TermDocs termDocs() throws IOException {
			return new MaskedTermPositions(in.termDocs());
		}

		@Override
		public TermDocs termDocs(Term term) throws IOException {
			TermDocs docs = new MaskedTermPositions(in.termDocs(term));
			return docs;
		}

		@Override
		public TermPositions termPositions() throws IOException {
			return new MaskedTermPositions(in.termPositions());
		}
		
		class MaskedTermPositions extends FilterTermDocs implements TermPositions {

			DocIdSetIterator docIt;
			
			public MaskedTermPositions(TermDocs in) {
				super(in);
			}

			@Override
			public void seek(Term term) throws IOException {
				in.seek(term);
				docIt = docMask.iterator();
			}

			@Override
			public void seek(TermEnum termEnum) throws IOException {
				in.seek(termEnum);
				docIt = docMask.iterator();
			}

			@Override
			public int freq() {
				return in.freq();
			}

			@Override
			public int doc() {
				return super.doc();
			}

			@Override
			public boolean next() throws IOException {
				docIt.nextDoc();
				return findMatch();
			}

			@Override
			public boolean skipTo(int i) throws IOException {
				docIt.advance(i);				
				return findMatch();
			}

			private boolean findMatch() throws IOException {
				if (!in.next()) {
					return false;
				}
				while(true) {
					int di = docIt.docID();
					if (di == DocIdSetIterator.NO_MORE_DOCS) {
						return false;
					}
					if (di == in.doc()) {
						return true;
					}
					else if (di < in.doc()) {
						docIt.advance(in.doc());
						continue;
					}
					else {
						if (!in.skipTo(di)) {
							return false;
						}
					}
				}
			}

			@Override
			public int read(int[] docs, int[] freqs) throws IOException {
				int n = 0;
				while(n < docs.length &&next()) {
					docs[n] = doc();
					freqs[n] = freq();
					++n;
				}
				return n;
			}

			@Override
			public int nextPosition() throws IOException {
				return ((TermPositions)in).nextPosition();
			}

			@Override
			public int getPayloadLength() {
				return ((TermPositions)in).getPayloadLength();
			}

			@Override
			public byte[] getPayload(byte[] data, int offset)	throws IOException {
				return ((TermPositions)in).getPayload(data, offset);
			}

			@Override
			public boolean isPayloadAvailable() {
				return ((TermPositions)in).isPayloadAvailable();
			}
		}
	}	
}
