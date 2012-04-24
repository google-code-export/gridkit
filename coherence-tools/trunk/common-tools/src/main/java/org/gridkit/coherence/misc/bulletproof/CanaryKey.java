package org.gridkit.coherence.misc.bulletproof;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.partition.KeyPartitioningStrategy.PartitionAwareKey;

public class CanaryKey implements PortableObject, PartitionAwareKey {

	private int partitionId;

	public CanaryKey() {
		// for POF serialization
	} 
	
	public CanaryKey(int partitionId) {
		this.partitionId = partitionId;
	}

	public int getPartitionId() {
		return partitionId;
	}
	

	@Override
	public int hashCode() {
		return getClass().getName().hashCode() ^ partitionId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CanaryKey other = (CanaryKey) obj;
		if (partitionId != other.partitionId)
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		partitionId = reader.readInt(1);
	}
	
	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeInt(1, partitionId);
	}
	
	@Override
	public String toString() {
		return "partId:" + partitionId;
	}
}
