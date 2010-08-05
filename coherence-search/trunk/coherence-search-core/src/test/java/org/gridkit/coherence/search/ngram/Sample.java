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

package org.gridkit.coherence.search.ngram;

import org.gridkit.coherence.search.SearchFactory;
import org.gridkit.coherence.search.ngram.NGramIndex;
import org.gridkit.coherence.search.ngram.NGramIndexPlugin;
import org.junit.Ignore;

import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;

/**
 * Sample of API usage
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class Sample {

	NamedCache cache;
	
	public void init() {
		// plugin for n-gram index
		NGramIndexPlugin plugin = new NGramIndexPlugin();

		// create index factory for "toString" attribute using n-gram index plugin with n-gram size 3
		SearchFactory<NGramIndex, Integer, String> nGramSearchFactory = new SearchFactory<NGramIndex, Integer, String>(plugin, 3, new ReflectionExtractor("toString"));

		// initialize index
		nGramSearchFactory.createIndex(cache);
		
		// query by n-gram index, looking to every object containing substring "text"
		cache.keySet(nGramSearchFactory.createFilter("text"));
		
	}
}
