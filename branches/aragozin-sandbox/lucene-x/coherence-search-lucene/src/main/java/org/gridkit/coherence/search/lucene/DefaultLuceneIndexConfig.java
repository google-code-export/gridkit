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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DefaultLuceneIndexConfig implements LuceneIndexConfig, Serializable, PortableObject {

	private static final long serialVersionUID = 20100728L;

	private LuceneAnalyzerProvider analyzerProvider = new WhitespaceAnalyzerProvider();
	private LuceneDirectoryProvider directoryProvider = new RAMDirectoryProvider();
	
	@Override
	public Analyzer getAnalyzer() {
		return analyzerProvider.getAnalyzer();
	}
	
	@Override
	public Directory createDirectoryInstance() {
		return directoryProvider.createDirectory();
	}

	public LuceneAnalyzerProvider getAnalyzerProvider() {
		return analyzerProvider;
	}

	public void setAnalyzerProvider(LuceneAnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}

	public LuceneDirectoryProvider getDirectoryProvider() {
		return directoryProvider;
	}

	public void setDirectoryProvider(LuceneDirectoryProvider directoryProvider) {
		this.directoryProvider = directoryProvider;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		int i = 1;

		analyzerProvider = (LuceneAnalyzerProvider) in.readObject(i++);
		directoryProvider = (LuceneDirectoryProvider) in.readObject(i++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int i = 1;

		out.writeObject(i++, analyzerProvider);
		out.writeObject(i++, directoryProvider);
	}
}
