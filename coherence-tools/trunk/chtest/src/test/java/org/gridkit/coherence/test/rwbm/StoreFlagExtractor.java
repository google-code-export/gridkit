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

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.extractor.AbstractExtractor;

public class StoreFlagExtractor extends AbstractExtractor implements PortableObject {

    private static final long serialVersionUID = 20010915L;

    public StoreFlagExtractor() {
    }
   
    @Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && getClass() == obj.getClass();
	}

	@Override
    public int compare(Object object1, Object object2) {
        // make no sense
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareEntries(com.tangosol.util.QueryMap.Entry entry1, com.tangosol.util.QueryMap.Entry entry2) {
        // make no sense
        throw new UnsupportedOperationException();
    }

    @Override
    public Object extract(Object object) {
        // decorator can be extracted only from binary entry
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object extractFromEntry(java.util.Map.Entry entry) {
        BinaryEntry binEntry = (BinaryEntry) entry;
        Binary binValue = binEntry.getBinaryValue();
        return extractInternal(binValue, binEntry);
    }

    @Override
    public Object extractOriginalFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
        BinaryEntry binEntry = (BinaryEntry) entry;
        Binary binValue = binEntry.getOriginalBinaryValue();
        return extractInternal(binValue, binEntry);
    }
   
    private Object extractInternal(Binary binValue, BinaryEntry entry) {
    	System.out.println("extractInternal: " + entry);
        if (ExternalizableHelper.isDecorated(binValue)) {
            Binary store = ExternalizableHelper.getDecoration(binValue, ExternalizableHelper.DECO_STORE);
            if (store != null) {
                Object st = ExternalizableHelper.fromBinary(store, entry.getSerializer());
                return st;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public void readExternal(PofReader paramPofReader) throws IOException {
        // do nothing
    }

    @Override
    public void writeExternal(PofWriter paramPofWriter) throws IOException {
        // do nothing
    }
}