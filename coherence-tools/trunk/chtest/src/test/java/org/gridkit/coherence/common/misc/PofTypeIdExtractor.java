package org.gridkit.coherence.common.misc;

import java.io.IOException;
import java.io.NotActiveException;
import java.util.Map;

import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.reflect.PofNavigator;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ClassHelper;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.extractor.AbstractExtractor;

public class PofTypeIdExtractor extends AbstractExtractor implements PortableObject {
	
	private static final long serialVersionUID = 20111216L;
	
	private PofNavigator navigator;

	public PofTypeIdExtractor() {
	}

	public PofTypeIdExtractor(int iProp) {
		this(new SimplePofPath(iProp));
	}

	public PofTypeIdExtractor(PofNavigator navigator) {
		this(navigator, VALUE);
	}

	public PofTypeIdExtractor(PofNavigator navigator, int target) {
		azzert(navigator != null, "Navigator must not be null.");

		this.navigator = navigator;
		this.m_nTarget = target;
	}

	@SuppressWarnings("rawtypes")
	public Object extractFromEntry(Map.Entry entry) {
		return extractInternal(entry, this.m_nTarget);
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
			return null;
		}

		PofValue valueRoot = PofValueParser.parse(binTarget, ctx);
		return Integer.valueOf(this.navigator.navigate(valueRoot).getTypeId());
	}

	public PofNavigator getNavigator() {
		return this.navigator;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof PofTypeIdExtractor) {
			PofTypeIdExtractor that = (PofTypeIdExtractor) o;
			return ((this.m_nTarget == that.m_nTarget) && (equals(
					this.navigator, that.navigator)));
		}

		return false;
	}

	public int hashCode() {
		return (this.navigator.hashCode() + this.m_nTarget);
	}

	public String toString() {
		return new StringBuilder()
				.append(ClassHelper.getSimpleName(super.getClass()))
				.append("(target=")
				.append((this.m_nTarget == 0) ? "VALUE" : "KEY")
				.append(", navigator=").append(this.navigator).append(')')
				.toString();
	}

	public void readExternal(PofReader in) throws IOException {
		int id = 1;
		
		this.m_nTarget = in.readInt(id++);
		this.navigator = ((PofNavigator) in.readObject(id++));
	}

	public void writeExternal(PofWriter out) throws IOException {
		PofNavigator navigator = this.navigator;
		if (navigator == null) {
			throw new NotActiveException(
					"PofExtractor was constructed without a navigator");
		}

		int id = 1;
		
		out.writeInt(id++, this.m_nTarget);
		out.writeObject(id++, navigator);
	}
}
