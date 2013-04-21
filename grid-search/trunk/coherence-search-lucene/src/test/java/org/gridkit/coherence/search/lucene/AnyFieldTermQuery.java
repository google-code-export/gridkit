package org.gridkit.coherence.search.lucene;

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.junit.Ignore;

/**
 * Example
 * 
 * This query will match term occurrence ignoring field name.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 */
@Ignore
public class AnyFieldTermQuery extends MultiTermQuery implements Serializable {

	private static final long serialVersionUID = 20110414L;
	
	private String text;
	
	public AnyFieldTermQuery(String text) {
		this.text = text;
	}
	
	@Override
	protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
		return new TermTextFilter(reader);
	}
	
	@Override
	public String toString(String field) {
		return "*:" + text;
	}
	
	private class TermTextFilter extends FilteredTermEnum {

		private IndexReader reader;
		private Term last = new Term("", "");
		
		public TermTextFilter(IndexReader reader) {
			this.reader = reader;
		}

		@Override
		public boolean next() throws IOException {
			if (last == null) {
				return false;
			}
			TermEnum te = reader.terms(last);				
			if (!te.next()) {
				last = null;
				return false;
			}
			while(true) {
				Term term = te.term();
//				System.out.println("Text: " + term);
				if (term == null) {
					last = null;
					return false;					
				}
				else if (term.text().equals(text)) {
					last = term;
					te.close();
					return true;
				}
				else {
					Term t;
					if (text.compareTo(term.text()) < 0) {
						t = new Term(term.field() + ((char)0), text);
					}
					else {
						t = new Term(term.field(), text);						
					}
					te.close();
					te = reader.terms(t);
					//te.next();
					continue;
				}
			}
		}

		@Override
		public Term term() {
			return last;
		}

		@Override
		public float difference() {
			return 0;
		}

		@Override
		protected boolean endEnum() {
			return last != null;
		}

		@Override
		protected boolean termCompare(Term term) {
			return text.equals(term.text());
		}
	}
}
