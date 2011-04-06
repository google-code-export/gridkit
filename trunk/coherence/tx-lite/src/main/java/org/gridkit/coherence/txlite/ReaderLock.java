package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.UID;

public class ReaderLock implements PortableObject, Serializable {

	private UID clientIndentity;
	private int txVersion;
	
	public ReaderLock() {
		// deserialization constructor
	}
	
	public ReaderLock(UID id, int txVersion) {
		this.clientIndentity = id;
		this.txVersion = txVersion;
	}

	public UID getClientIndentity() {
		return clientIndentity;
	}

	public int getTxVersion() {
		return txVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientIndentity == null) ? 0 : clientIndentity.hashCode());
		result = prime * result + txVersion;
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
		ReaderLock other = (ReaderLock) obj;
		if (clientIndentity == null) {
			if (other.clientIndentity != null)
				return false;
		} else if (!clientIndentity.equals(other.clientIndentity))
			return false;
		if (txVersion != other.txVersion)
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		clientIndentity = (UID) in.readObject(1);
		txVersion = in.readInt(2);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeObject(1, clientIndentity);
		out.writeInt(2, txVersion);
	}
	
	@Override
	public String toString() {
		return clientIndentity.toString() + "@" + txVersion;
	}
}
