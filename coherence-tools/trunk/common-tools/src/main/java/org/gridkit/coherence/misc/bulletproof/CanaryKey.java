package org.gridkit.coherence.misc.bulletproof;

import java.io.Serializable;

import com.tangosol.net.partition.KeyPartitioningStrategy.PartitionAwareKey;

public class CanaryKey implements PartitionAwareKey, Serializable {

	private static final long serialVersionUID = 20120424L;

	private int partitionId;

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
	public String toString() {
		return "partId:" + partitionId;
	}
}
