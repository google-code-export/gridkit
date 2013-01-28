package org.gridkit.data.extractors.protobuf;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.gridkit.data.extractors.common.BinaryExtractor;
import org.gridkit.data.extractors.common.BinaryExtractorSet;
import org.gridkit.data.extractors.common.Blob;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class ProtoBufExtractor<V> implements BinaryExtractor<V>, Serializable {

	private static final long serialVersionUID = 20130125L;

	public static ProtoBufExtractor<Blob> newBlobExtractor(int... path) {
		return new ProtoBufExtractor<Blob>(path, Encoding.BLOB, false);
	}

	public static ProtoBufExtractor<Number> newSignedIntegerExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.SINGNED_INT32, false);
	}
	
	public static ProtoBufExtractor<Number> newUnsignedIntegerExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.UNSIGNED_INT32, false);
	}

	public static ProtoBufExtractor<Number> newSignedLongExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.SINGNED_INT64, false);
	}
	
	public static ProtoBufExtractor<Number> newUnsignedLongExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.UNSIGNED_INT64, false);
	}
	
	public static ProtoBufExtractor<Number> newFloatingPointExtractor(int... path) {
		return new ProtoBufExtractor<Number>(path, Encoding.FLOATING_POINT, false);
	}

	public static ProtoBufExtractor<String> newStringExtractor(int... path) {
		return new ProtoBufExtractor<String>(path, Encoding.UTF8, false);
	}
	
	enum Encoding {
		SINGNED_INT32,
		UNSIGNED_INT32,
		SINGNED_INT64,
		UNSIGNED_INT64,
		FLOATING_POINT,
		UTF8,
		BLOB
	}
	
	private final int[] path;
	private final Encoding encoding;
	private final boolean group;
	private final BinaryExtractor<V> nested;

	// copy constructor
	private ProtoBufExtractor(int[] path, Encoding encoding, boolean group, BinaryExtractor<V> nested) {
		this.path = path;
		this.encoding = encoding;
		this.group = group;
		this.nested = nested;
	}

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
	
	protected boolean isLeaf() {
		return path.length == 0;
	}
	
	protected int getPrefix() {
		return path[0];
	}
	
	protected ProtoBufExtractor<V> trim() {
		int[] subpath = new int[path.length - 1];
		System.arraycopy(path, 0, subpath, 0, subpath.length);
		return new ProtoBufExtractor<V>(subpath, encoding, group, nested);
		
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

	protected Object decode(int wireType, ByteBuffer buffer) throws IOException {
		switch(encoding) {
		case SINGNED_INT32:
			return decodeSignedInt(wireType, buffer);
		case UNSIGNED_INT32:
			return decodeUnsignedInt(wireType, buffer);
		case SINGNED_INT64:
			return decodeSignedLong(wireType, buffer);
		case UNSIGNED_INT64:
			return decodeUnsignedLong(wireType, buffer);
		case FLOATING_POINT:
			return decodeFloat(wireType, buffer);
		case UTF8:
			return decodeUTF8(wireType, buffer);
		case BLOB:
			return decodeBlob(wireType, buffer);
		default:
			throw new IllegalArgumentException("Unknown encoding");
		}
	}
	
	private int decodeSignedInt(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_VARINT:
			return PBHelper.inputStream(buffer).readSInt32();
		case WireFormat.WIRETYPE_FIXED32:
			return PBHelper.inputStream(buffer).readFixed32();
		case WireFormat.WIRETYPE_FIXED64:
			return (int) PBHelper.inputStream(buffer).readFixed64();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as integer");
		}
	}

	private int decodeUnsignedInt(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_VARINT:
			return PBHelper.inputStream(buffer).readInt32();
		case WireFormat.WIRETYPE_FIXED32:
			return PBHelper.inputStream(buffer).readFixed32();
		case WireFormat.WIRETYPE_FIXED64:
			return (int) PBHelper.inputStream(buffer).readFixed64();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as integer");
		}
	}

	private long decodeSignedLong(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_VARINT:
			return PBHelper.inputStream(buffer).readSInt64();
		case WireFormat.WIRETYPE_FIXED32:
			return PBHelper.inputStream(buffer).readFixed32();
		case WireFormat.WIRETYPE_FIXED64:
			return PBHelper.inputStream(buffer).readFixed64();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as integer");
		}
	}
	
	private long decodeUnsignedLong(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_VARINT:
			return PBHelper.inputStream(buffer).readInt64();
		case WireFormat.WIRETYPE_FIXED32:
			return PBHelper.inputStream(buffer).readFixed32();
		case WireFormat.WIRETYPE_FIXED64:
			return PBHelper.inputStream(buffer).readFixed64();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as integer");
		}
	}

	private double decodeFloat(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_FIXED32:
			return PBHelper.inputStream(buffer).readFloat();
		case WireFormat.WIRETYPE_FIXED64:
			return PBHelper.inputStream(buffer).readDouble();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as floating point");
		}
	}

	private String decodeUTF8(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_LENGTH_DELIMITED:
			CodedInputStream cis = PBHelper.inputStream(buffer);
			return cis.readString();
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as floating point");
		}
	}

	private Object decodeBlob(int wireType, ByteBuffer buffer) throws IOException {
		int wireFormat = wireType & 0x7;
		switch(wireFormat) {
		case WireFormat.WIRETYPE_LENGTH_DELIMITED:
			CodedInputStream cis = PBHelper.inputStream(buffer);
			int len = cis.readInt32();
			int offs = cis.getTotalBytesRead();
			ByteBuffer bb = buffer.slice();
			bb.position(offs);
			bb.limit(offs + len);
			return new Blob(bb);
		default:
			throw new IOException("Wire format " + wireFormat + " cannot be interpreted as binary");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((encoding == null) ? 0 : encoding.hashCode());
		result = prime * result + (group ? 1231 : 1237);
		result = prime * result + ((nested == null) ? 0 : nested.hashCode());
		result = prime * result + Arrays.hashCode(path);
		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProtoBufExtractor other = (ProtoBufExtractor) obj;
		if (encoding != other.encoding)
			return false;
		if (group != other.group)
			return false;
		if (nested == null) {
			if (other.nested != null)
				return false;
		} else if (!nested.equals(other.nested))
			return false;
		if (!Arrays.equals(path, other.path))
			return false;
		return true;
	}

	@Override
	public BinaryExtractorSet newExtractorSet() {
		return new ProtoBufExtractorSet();
	}

	@Override
	public boolean isCompatible(BinaryExtractorSet set) {
		return set instanceof ProtoBufExtractorSet;
	}
}
