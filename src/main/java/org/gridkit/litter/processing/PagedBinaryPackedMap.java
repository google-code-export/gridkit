package org.gridkit.litter.processing;

import org.gridkit.coherence.offheap.storage.BinaryPackedBytesHashStore;
import org.gridkit.coherence.offheap.storage.ObjectStoreScheme;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PagedBinaryPackedMap extends ObjectStoreScheme {

	public PagedBinaryPackedMap() {
		super(new BinaryPackedBytesHashStore(100000, 64, true) {

			@Override
			protected Object keyFromBytes(byte[] data, int offs, int size) {
				// TODO Auto-generated method stub
				return new String(data, offs, size);
			}

			@Override
			protected byte[] keyToBytes(Object key) {
				return ((String)key).getBytes();
			}

			@Override
			protected Object valueFromBytes(byte[] data, int offs, int size) {
				// TODO Auto-generated method stub
				return keyFromBytes(data, offs, size);
			}

			@Override
			protected byte[] valueToBytes(Object value) {
				return keyToBytes(value);
			}
		}, false);
	}	
}
