package org.gridkit.coherence.search.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.tangosol.util.Binary;
import com.tangosol.util.ValueExtractor;

public class LuceneDocumentExtractor implements ValueExtractor {

	public static final String DOCUMENT_ID = "doc-id";
	
	private List<FieldFactory> fieldMap = new ArrayList<FieldFactory>();

	public LuceneDocumentExtractor() {
	}
	
	public LuceneDocumentExtractor(String field, ValueExtractor extractor) {
		addText(field, extractor);
	}
	
	public void addText(String name, ValueExtractor extractor) {
		addText(name, extractor, Field.Store.NO, Field.Index.ANALYZED, TermVector.NO);
	}

	public void addText(String name, ValueExtractor extractor, Field.Store store, Field.Index index) {
		addText(name, extractor, store, index, TermVector.NO);
	}

	public void addText(String name, ValueExtractor extractor, Field.Store store, Field.Index index, Field.TermVector termVector) {
		fieldMap.add(new GenericFieldFactory(name, extractor, false, store, index, termVector));
	}
	
	public void addBinaryField(String name, ValueExtractor extractor, Field.Store store) {
		fieldMap.add(new GenericFieldFactory(name, extractor, true, store, null, null));
	}
	
	public void addCustomField(FieldFactory ff) {
		fieldMap.add(ff);
	}
	
	@Override
	public Object extract(Object object) {
		Field[] fields = new Field[fieldMap.size()];
		for(int i = 0; i != fields.length; ++i) {
			FieldFactory ff = fieldMap.get(i);
			// TODO binary entry extraction optimizations
			Object attrib = ff.getExtractor().extract(object);
			fields[i] = ff.toField(attrib);
		}
		return fields;
	}
	
	public static interface FieldFactory {		

		public Field toField(Object attribute);
		public ValueExtractor getExtractor();
		
	}
	
	public static class GenericFieldFactory implements FieldFactory {
		private String name;
		private ValueExtractor extractor;
		private boolean binary;
		private Field.Store store = Store.NO;
		private Field.Index index = Index.ANALYZED;
		private Field.TermVector termVector = TermVector.NO;
		
		public GenericFieldFactory() {
			// for remotting
		}
		
		public GenericFieldFactory(String name, ValueExtractor extractor, boolean binary, Store store, Index index, TermVector termVector) {
			this.name = name;
			this.extractor = extractor;
			this.binary = binary;
			this.store = store;
			this.index = index;
			this.termVector = termVector;
		}

		@Override
		public ValueExtractor getExtractor() {
			return extractor;
		}

		@Override
		public Field toField(Object attribute) {
			if (binary) {
				byte[] data;
				if (attribute instanceof byte[]) {
					data = (byte[]) attribute;
				}
				else if (attribute instanceof Binary) {
					data = ((Binary)attribute).toByteArray();
				}
				else {
					throw new IllegalArgumentException("Cannot conver to binary - " + attribute);
				}
				return new Field(name, data, store);
			}
			else {
				String text = String.valueOf(attribute);
				return new Field(name, text, store, index, termVector);
			}
		}
	}
}
