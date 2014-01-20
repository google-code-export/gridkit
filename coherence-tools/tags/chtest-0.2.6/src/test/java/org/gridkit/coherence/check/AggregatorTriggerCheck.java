/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CacheConfig;
import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.CompositeKey;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.processor.AbstractProcessor;

public class AggregatorTriggerCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}
	
	@Test
	public void test_aggregating_put() throws InterruptedException, ExecutionException {
		
		cloud.all().presetFastLocalCluster();
		
		DistributedScheme dscheme = CacheConfig.distributedSheme();
		dscheme.backingMapScheme(CacheConfig.localScheme());
		
		
		cloud.all().mapCache("data", dscheme);
		cloud.all().mapCache("data-total", dscheme);
		
		CohNode storage = cloud.node("storage");
		CohNode client = cloud.node("client");
		storage.localStorage(true);
		client.localStorage(true);

		cloud.all().getCache("data");
		cloud.all().getCache("data-total");
		
		client.exec(new Runnable() {
			
			@Override
			public void run() {

				PutAggregateProcessor putp = new PutAggregateProcessor(1d);

				NamedCache summary = CacheFactory.getCache("data-total");

				Assert.assertEquals(null, summary.get(new CompositeKey("A", "total")));
				Assert.assertEquals(null, summary.get(new CompositeKey("B", "total")));

				NamedCache cache = CacheFactory.getCache("data");
				cache.invoke(new CompositeKey("A", 1), putp);
				cache.invoke(new CompositeKey("A", 2), putp);
				cache.invoke(new CompositeKey("A", 3), putp);
				cache.invoke(new CompositeKey("B", 1), putp);
				cache.invoke(new CompositeKey("B", 2), putp);
				
				Assert.assertEquals(3d, summary.get(new CompositeKey("A", "total")));
				Assert.assertEquals(2d, summary.get(new CompositeKey("B", "total")));
			}
		});
		
	}

	
	@SuppressWarnings("serial")
	public static class CacheTrigger implements MapTrigger, Serializable {

		@SuppressWarnings("unchecked")
		@Override
		public void process(Entry e) {
			BinaryEntry be = (BinaryEntry)e;
			BackingMapContext map = be.getContext().getBackingMapContext("a-B");
			map.getBackingMap().put(be.getBinaryKey(), be.getBinaryValue());
		}
	}
	
	@SuppressWarnings("serial")
	public static class PutAggregateProcessor extends AbstractProcessor implements Serializable {
		
		private Double value;

		public PutAggregateProcessor(Double value) {
			this.value = value;
		}

		@Override
		public Object process(com.tangosol.util.InvocableMap.Entry e) {
			CompositeKey key = (CompositeKey) e.getKey();
			CompositeKey total = new CompositeKey(key.getPrimaryKey(), "total");
			Double oval = (e.isPresent()) ? (Double)(e.getValue()) : 0; 
			Double nval = value;
			e.setValue(value);
			
			BinaryEntry be = (BinaryEntry) e;
			Binary btk = (Binary) be.getContext().getKeyToInternalConverter().convert(total);
			InvocableMap.Entry sum = be.getContext().getBackingMapContext("data-total").getBackingMapEntry(btk);
			if (!sum.isPresent()) {
				sum.setValue(nval.doubleValue());
			}
			else {
				double s = (Double) sum.getValue();
				s += nval.doubleValue() - oval.doubleValue();
				sum.setValue(s);
			}
			return null;
		}
	}
	
	// Not working
	@SuppressWarnings("serial")
	public static class AggregatorMapTrigger implements MapTrigger, Serializable {

		private String summaryCacheName;
		private String subKey;
		private ValueExtractor extractor;
		

		public AggregatorMapTrigger(String summaryCacheName, String subKey, ValueExtractor extractor) {
			this.summaryCacheName = summaryCacheName;
			this.subKey = subKey;
			this.extractor = extractor;
		}

		@Override
		public void process(Entry e) {
			CompositeKey key = (CompositeKey) e.getKey();
			CompositeKey total = new CompositeKey(key.getPrimaryKey(), "total");
			Number oval = (e.isOriginalPresent()) ? (Number)(extractor.extract(e.getOriginalValue())) : 0; 
			Number nval = (e.isPresent()) ? (Number)(extractor.extract(e.getValue())) : 0;
			BinaryEntry be = (BinaryEntry) e;
			Binary btk = (Binary) be.getContext().getKeyToInternalConverter().convert(total);
			InvocableMap.Entry sum = be.getContext().getBackingMapContext(summaryCacheName).getBackingMapEntry(btk);
			if (!sum.isPresent()) {
				sum.setValue(nval.doubleValue());
			}
			else {
				double s = (Double) sum.getValue();
				s += nval.doubleValue() - oval.doubleValue();
				sum.setValue(s);
			}
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((extractor == null) ? 0 : extractor.hashCode());
			result = prime * result
					+ ((subKey == null) ? 0 : subKey.hashCode());
			result = prime
					* result
					+ ((summaryCacheName == null) ? 0 : summaryCacheName
							.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AggregatorMapTrigger other = (AggregatorMapTrigger) obj;
			if (extractor == null) {
				if (other.extractor != null)
					return false;
			} else if (!extractor.equals(other.extractor))
				return false;
			if (subKey == null) {
				if (other.subKey != null)
					return false;
			} else if (!subKey.equals(other.subKey))
				return false;
			if (summaryCacheName == null) {
				if (other.summaryCacheName != null)
					return false;
			} else if (!summaryCacheName.equals(other.summaryCacheName))
				return false;
			return true;
		}			
	}
}
