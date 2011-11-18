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

package org.gridkit.coherence.search.lucene.samples;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.gridkit.coherence.search.lucene.LuceneDocumentExtractor;
import org.gridkit.coherence.search.lucene.LuceneSearchFactory;
import org.junit.Ignore;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.BetweenFilter;

/**
 * Sample of Lucene plugin API usage
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class Sample {

	NamedCache cache;
	
	// sample of searchable object
	public static class Post {
		
		private String title;
		private String author;
		private String content;
		private String[] tags;
		private long postDateTime;
		
		public Post() {
			// 
		}
		
		public long getDateTime() {
			return postDateTime;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getAuthor() {
			return author;
		}
		
		public String getContent() {
			return content;
		}
		
		/**
		 * We want to put all tags in single searchable filed in Lucene.
		 * To do this all tag array is coverted to space separated string
		 * which will be properly tokenized by Lucene.
		 * @return all tags is space separated string
		 */
		public String getSearchableTags() {
			StringBuilder builder = new StringBuilder();
			for(String tag: tags) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(tag);	 
			}
			return builder.toString();
		}		
	}
	
	public void usingGridSearch() {

		// First, we need to define how our object will map
		// to field in Lucene document
		LuceneDocumentExtractor extractor = new LuceneDocumentExtractor();
		extractor.addText("title", new ReflectionExtractor("getTitle"));
		extractor.addText("author", new ReflectionExtractor("getAuthor"));
		extractor.addText("content", new ReflectionExtractor("getContent"));
		extractor.addText("tags", new ReflectionExtractor("getSearchableTags"));
		
		// next create LuceneSearchFactory helper class
		LuceneSearchFactory searchFactory = new LuceneSearchFactory(extractor);
		
		// initialize index for cache
		// this operation actually tells coherence
		// to create index structures on all
		// storage enabled nodes
		searchFactory.createIndex(cache);

		// now index is ready and we can search Coherence cache
		// using Lucene queries

		PhraseQuery pq = new PhraseQuery();
		pq.add(new Term("content", "Coherence"));
		pq.add(new Term("content", "search"));
		
		// Lucene filter is converted to Coherence filter by search factory
		cache.keySet(searchFactory.createFilter(pq));
		
		// You can also combine normal Coherence filters with Lucene queries
		long startDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24; // last day
		long endDate = System.currentTimeMillis();
		BetweenFilter dateFilter = new BetweenFilter("getDateTime", startDate, endDate);
		Filter pqFilter = searchFactory.createFilter(pq);

		// Now we are selecting objects by Lucene query and apply
		// standard Coherence filter over Lucene result set
		cache.keySet(new AndFilter(pqFilter, dateFilter));
	}
}
