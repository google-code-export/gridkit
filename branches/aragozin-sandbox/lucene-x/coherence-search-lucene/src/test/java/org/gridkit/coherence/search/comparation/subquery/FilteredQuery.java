package org.gridkit.coherence.search.comparation.subquery;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.OpenBitSet;

@SuppressWarnings("deprecation")
public class FilteredQuery extends Query {

	private static final long serialVersionUID = 1L;

	private Term[] maskTerms;
	private Query query;
	
	private MaskedIndexReader maskedReader;
	
	public FilteredQuery(Query q, Term... terms) {
		this.query = q;
		this.maskTerms = terms;
	}

	public FilteredQuery(Query q, Term[] terms, MaskedIndexReader reader) {
		this.query = q;
		this.maskTerms = terms;
		this.maskedReader = reader;
	}

	@Override
	public String toString(String field) {
		return query.toString(field) + "|" + Arrays.toString(maskTerms);
	}

	@Override
	public Weight createWeight(final Searcher searcher) throws IOException {
		final Weight w = query.createWeight(new IndexSearcher(maskedReader) {
			@Override
			public Similarity getSimilarity() {
				return searcher.getSimilarity();
			}					
		});
		
		return new Weight() {
			
			@Override
			public float sumOfSquaredWeights() throws IOException {
				return w.sumOfSquaredWeights();
			}
			
			@Override
			public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,	boolean topScorer) throws IOException {
				return w.scorer(maskedReader, scoreDocsInOrder, topScorer);
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
				return w.explain(maskedReader, doc);
			}
		};
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		if (this.maskedReader != null) {
			return this;
		}
		else {
			
			OpenBitSet base = null;
			for(Term term: maskTerms) {
				OpenBitSet result = new OpenBitSet(reader.maxDoc());
				TermDocs td = reader.termDocs(term);
				while(td.next()) {
					System.out.println("mask: " + td.doc());
					result.fastSet(td.doc());
				}
				if (base == null) {
					base = result;
				}
				else {
					base.and(result);
				}
			}
			
			MaskedIndexReader mr = new MaskedIndexReader(reader, base);
			
			Query newQuery = query;
			while(true) {
				Query q = newQuery.rewrite(mr);
				if (q == newQuery) {
					break;
				}
				newQuery = q;
			}
			
			return new FilteredQuery(newQuery, maskTerms, mr);
		}
	}
	
	private static class MaskedIndexReader extends FilterIndexReader {
		
		private DocIdSet docMask;
		
		public MaskedIndexReader(IndexReader reader, DocIdSet docMask) {
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
				super.seek(term);
				docIt = docMask.iterator();
			}

			@Override
			public void seek(TermEnum termEnum) throws IOException {
				super.seek(termEnum);
				docIt = docMask.iterator();
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
				while(true) {
					int di = docIt.docID();
					if (di == DocIdSetIterator.NO_MORE_DOCS) {
						return false;
					}
					if (!in.skipTo(di)) {
						return false;
					}
					if (di == doc()) {
						System.out.println("#" + this.hashCode() + " find match: " + doc());
						return true;
					}
					else {
						docIt.advance(doc());
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
				return ((TermPositions)in).getPayloadLength();
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
