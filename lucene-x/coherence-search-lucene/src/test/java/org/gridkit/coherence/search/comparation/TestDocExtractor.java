package org.gridkit.coherence.search.comparation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ValueExtractor;

public class TestDocExtractor implements ValueExtractor, Serializable, PortableObject {

	@Override
	@SuppressWarnings("unchecked")
	public Object extract(Object obj) {
		Map<String, String> map = (Map<String, String>) obj;
		Field[] fields = new Field[map.size()];
		int n = 0;
		for (Map.Entry<String,String> entry: map.entrySet()) {
			fields[n++] = new Field(entry.getKey(), entry.getValue(), Store.NO, Index.NOT_ANALYZED_NO_NORMS);
		}
		return fields;
	}
	
	@Override
	public void readExternal(PofReader reader) throws IOException {
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
	}	
}
