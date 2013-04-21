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
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.util.Version;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class WhitespaceAnalyzerProvider implements LuceneAnalyzerProvider, Serializable, PortableObject {

	private static final long serialVersionUID = 20100720L;
	
	public static final WhitespaceAnalyzerProvider INSTANCE = new WhitespaceAnalyzerProvider();
	
	public WhitespaceAnalyzerProvider() {		
	}
	
	public Analyzer getAnalyzer() {
		return new WhitespaceAnalyzer(Version.LUCENE_33);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
	}	
}
