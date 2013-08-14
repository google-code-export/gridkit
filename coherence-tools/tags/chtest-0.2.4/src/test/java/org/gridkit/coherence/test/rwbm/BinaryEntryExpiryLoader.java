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
package org.gridkit.coherence.test.rwbm;

import java.util.Set;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;

/** */
public class BinaryEntryExpiryLoader implements BinaryEntryStore {

	
	int counter = 0; 
		
	public BinaryEntryExpiryLoader() {		
	}
	
	public BinaryEntryExpiryLoader(BackingMapContext bmContext) {		
	}
	
	@Override
	public void load(BinaryEntry entry) {
		Object key = entry.getKey();
		if (key instanceof Number) {
			Object value = String.valueOf(key) + "-" + (counter++);
			Binary bv = (Binary) entry.getContext().getValueToInternalConverter().convert(value);
			long now = CacheFactory.getSafeTimeMillis();
			long expiry = ((Number) key).intValue();
			Binary dbv = ExternalizableHelper.decorate(bv, ExternalizableHelper.DECO_EXPIRY, new Binary(longToBinary(now + expiry)));
			entry.updateBinaryValue(dbv);
		}
	}
	
	private byte[] longToBinary(long l) {
		byte[] word = new byte[8];
		word[0] = ((byte)(l >> 56));
		word[1] = ((byte)(l >> 48));
		word[2] = ((byte)(l >> 40));
		word[3] = ((byte)(l >> 32));
		word[4] = ((byte)(l >> 24));
		word[5] = ((byte)(l >> 16));
		word[6] = ((byte)(l >> 8));
		word[7] = ((byte)(l));
		return word;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void loadAll(Set entrySet) {
		for(Object e: entrySet) {
			load((BinaryEntry) e);
		}
	}

	@Override
	public void store(BinaryEntry entry) {
		throw new UnsupportedOperationException("Read only");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void storeAll(Set entrySet) {
		throw new UnsupportedOperationException("Read only");
	}

	@Override
	public void erase(BinaryEntry entry) {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void eraseAll(Set entrySet) {
	}
}
