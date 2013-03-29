package org.gridkit.coherence.utils.pof;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.reflect.ComplexPofValue;
import com.tangosol.io.pof.reflect.PofNavigator;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ImmutableArrayList;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.extractor.AbstractExtractor;

public class PofMultiExtractor extends AbstractExtractor implements PortableObject {

	private static final long serialVersionUID = 20121220L;
	
	private PofNavigator rootNavigator;
	private int[] indexArray;
	private int[] positionArray;
	
	public PofMultiExtractor() {
		// for serialization
	}
	
	public PofMultiExtractor(int... indexes) {
		this(null, indexes);
	}

	public PofMultiExtractor(PofNavigator extractRoot, int... indexes) {
		this.rootNavigator = extractRoot;
		indexArray = Arrays.copyOf(indexes, indexes.length);
		Arrays.sort(indexArray);
		positionArray = new int[indexArray.length];
		for(int i = 0; i != indexes.length; ++i) {
			positionArray[Arrays.binarySearch(indexArray, indexes[i])] = i;
		}		
	}
	
	@SuppressWarnings("rawtypes")
	public Object extractFromEntry(Map.Entry entry) {
		return extractInternal(entry, VALUE);
	}

	public Object extractOriginalFromEntry(MapTrigger.Entry entry) {
		return extractInternal(entry, -1);
	}

	@SuppressWarnings("rawtypes")
	private Object extractInternal(Map.Entry entry, int nTarget) {
		BinaryEntry binEntry;
		PofContext ctx;
		try {
			binEntry = (BinaryEntry) entry;
			ctx = (PofContext) binEntry.getSerializer();
		} catch (ClassCastException cce) {
			String sReason = (entry instanceof BinaryEntry) ? "the configured Serializer is not a PofContext"
					: "the Map Entry is not a BinaryEntry";

			throw new UnsupportedOperationException(
					new StringBuilder()
							.append("PofExtractor must be used with POF-encoded Binary entries; ")
							.append(sReason).toString());
		}

		Binary binTarget;
		switch (nTarget) {
			case VALUE:
			default:
				binTarget = binEntry.getBinaryValue();
				break;
			case KEY:
				binTarget = binEntry.getBinaryKey();
				break;
			case -1: // original value
				binTarget = binEntry.getOriginalBinaryValue();
		}

		if (binTarget == null) {
			// TODO or should I return something else
			return null;
		}

		PofValue valueRoot = PofValueParser.parse(binTarget, ctx);
		if (rootNavigator != null) {
			valueRoot = rootNavigator.navigate(valueRoot);
		}
		
		if (valueRoot instanceof ComplexPofValue) {
			ComplexPofValue pofArray = (ComplexPofValue) valueRoot;
			Object[] result = new Object[indexArray.length];

			for(int i = 0; i != result.length; ++i) {
				result[positionArray[i]] = pofArray.getChild(indexArray[i]).getValue();
			}

			return new ImmutableArrayList(result);
		}
		else {
			return null;
		}
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indexArray);
		result = prime * result + Arrays.hashCode(positionArray);
		result = prime * result
				+ ((rootNavigator == null) ? 0 : rootNavigator.hashCode());
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
		PofMultiExtractor other = (PofMultiExtractor) obj;
		if (!Arrays.equals(indexArray, other.indexArray))
			return false;
		if (!Arrays.equals(positionArray, other.positionArray))
			return false;
		if (rootNavigator == null) {
			if (other.rootNavigator != null)
				return false;
		} else if (!rootNavigator.equals(other.rootNavigator))
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		rootNavigator = (PofNavigator) in.readObject(1);
		indexArray = in.readIntArray(2);
		positionArray = in.readIntArray(3);
		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeObject(1, rootNavigator);
		out.writeIntArray(2, indexArray);
		out.writeIntArray(3, positionArray);
	}
}
