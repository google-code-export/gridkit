package org.gridkit.data.extractors.protobuf;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.gridkit.data.extractors.common.BinaryExtractor;
import org.gridkit.data.extractors.common.Blob;

public class ProtoBufExtractor<V> implements BinaryExtractor<V>, Serializable {

	private static final long serialVersionUID = 20130125L;

	public static ProtoBufExtractor<Blob> newBlobExtractor(int... path) {
		return new ProtoBufExtractor<Blob>(path, Encoding.BLOB, false);
	}

	public static ProtoBufExtractor<Number> newSignedIntegerExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.SINGNED, false);
	}
	
	public static ProtoBufExtractor<Number> newUnsignedIntegerExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.UNSIGNED, false);
	}

	public static ProtoBufExtractor<Number> newFloatingPointExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.FLOATING_POINT, false);
	}

	public static ProtoBufExtractor<String> newStringExtractor(int... path) {
		return new ProtoBufExtractor<String>(path, Encoding.UTF8, false);
	}
	
	enum Encoding {
		SINGNED,
		UNSIGNED,
		FLOATING_POINT,
		UTF8,
		BLOB
	}
	
	private final int[] path;
	private final Encoding encoding;
	private final boolean group;
	private final BinaryExtractor<V> nested;

	/**
	 * You can use static methods for commonly used extractors
	 * @param path - indexes leading to value
	 * @param encoding - decoding convention
	 * @param group - collect all values for key
	 */
	public ProtoBufExtractor(int[] path, Encoding encoding, boolean group) {
		if (path == null) {
			throw new NullPointerException("Path cannot be null");
		}
		if (encoding == null) {
			throw new NullPointerException("Encoding cannot be null");
		}
		this.path = path;
		this.encoding = encoding;
		this.group = group;
		this.nested = null; 
	}

	/**
	 * You can use static methods for commonly used extractors
	 * @param path - indexes leading to value
	 * @param group - collect all values for key
	 * @param nested - use provided extractor to transform binary
	 */
	public ProtoBufExtractor(int[] path, boolean group, BinaryExtractor<V> nested) {
		if (path == null) {
			throw new NullPointerException("Path cannot be null");
		}
		if (nested == null) {
			throw new NullPointerException("Nested extractor cannot be null");
		}
		this.path = path;
		this.encoding = null;
		this.group = group;
		this.nested = nested; 
	}
	
	protected int[] getPath() {
		return path;
	}
	
	protected Encoding getEncoding() {
		return encoding;
	}
	
	protected boolean group() {
		return group;
	}

	protected BinaryExtractor<?> getNestedExtractor() {
		return nested;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public V extract(ByteBuffer buffer) {
		ProtoBufExtractorSet e = new ProtoBufExtractorSet();
		int n = e.addExtractor(this);
		return (V) e.extractAll(buffer)[n];
	}
}
