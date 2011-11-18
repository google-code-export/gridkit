package org.gridkit.coherence.search.comparation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.gridkit.coherence.search.lucene.LuceneSearchFactory;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

public class LuceneIndexTestSequence extends BaseTestSequence {

	LuceneSearchFactory factory = new LuceneSearchFactory(new TestDocExtractor());
	
	public void createIndexes(NamedCache cache, List<String> fields) {
		factory.getEngineConfig().setIndexUpdateQueueSizeLimit(16);
		factory.createIndex(cache);
	}
	
	public Filter createFilter(QueryCondition[] conditions) {
		Query q; 
		if (conditions.length == 1) {
			q = createQuery(conditions[0]);
		}
		else {
			BooleanQuery bq = new BooleanQuery();
			for(int i = 0; i != conditions.length; ++i) {
				bq.add(createQuery(conditions[i]), Occur.MUST);
			}
			q = bq;
		}
		return factory.createFilter(q);
	}

	private Query createQuery(QueryCondition qc) {
		if (qc.rangeQuery) {
			TermRangeQuery query = new TermRangeQuery(qc.field, qc.terms[0], qc.terms[1], true, false);
			return query;
		}
		else if (qc.terms.length == 1) {
			return new TermQuery(new Term(qc.field, qc.terms[0]));
		}
		else {
			return new TermSetQuery(qc.field, qc.terms);
		}
	}

	public static class TermSetQuery extends MultiTermQuery {
		
		private static final long serialVersionUID = 20110827L;
		
		private String field;
		private String[] terms;
		
		public TermSetQuery(String field, String[] terms) {
			this.field = field;
			this.terms = terms;
		}

		@Override
		protected FilteredTermEnum getEnum(IndexReader reader)	throws IOException {
			
			return new FilteredTermEnum() {
				
				int pos = 0;
				
				@Override
				public boolean next() throws IOException {
					++pos;
					return pos < terms.length;
				}

				@Override
				public Term term() {
					return new Term(field, terms[pos]);
				}

				@Override
				protected boolean termCompare(Term term) {
					return true;
				}
				
				@Override
				protected boolean endEnum() {
					return pos >= terms.length;
				}
				
				@Override
				public float difference() {
					return 0;
				}
			};
		}

		@Override
		public String toString(String field) {
			return "TermSetQuery(" + field + ", " + Arrays.toString(terms) + ")";
		}
	}
	
	public static void main(String[] args) {
		new LuceneIndexTestSequence().start();
	}	
}
