/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridkit.coherence.search.lucene;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.gridkit.coherence.search.SearchFactory.QueryFilter;
import org.gridkit.coherence.search.SearchFactory.SearchIndexExtractor;
import org.gridkit.coherence.search.lucene.util.JavaSerializationSerializer;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * This class is required to workaround POF serialization problem.
 * Lucene {@link Query} is not compatible with POF, so it will be serializaed using
 * java serialization. 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LuceneQueryFilter extends QueryFilter<LuceneInMemoryIndex, Query> implements PortableObject {

	private static final long serialVersionUID = 20110410L;

	public LuceneQueryFilter() {
		// for POF deserialization
	}

	public LuceneQueryFilter(SearchIndexExtractor<LuceneInMemoryIndex, ?, Query> extractor, Query query) {
		super(extractor, query);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader in) throws IOException {
		int i = 1;
		extractor = (SearchIndexExtractor<LuceneInMemoryIndex, ?, Query>) in.readObject(i++);
		byte[] q = in.readByteArray(i++);
		query = (Query) JavaSerializationSerializer.fromBytes(q);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int i = 1;
		out.writeObject(i++, extractor);
		out.writeByteArray(i++, JavaSerializationSerializer.toBytes(query));
	}
}
