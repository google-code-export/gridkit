package org.gridkit.coherence.util.thrift;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.Binary;

public class ThriftPofSerializer implements PofSerializer {

	private Constructor<?> constructor;
	private TSerializer serializer;
	private TDeserializer deserializer;
	
	
	public ThriftPofSerializer(int typeId, Class<?> type) {
		try {
			this.constructor = type.getConstructor();
			this.constructor.setAccessible(true);
			this.serializer = new TSerializer();
			this.deserializer = new TDeserializer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void serialize(PofWriter out, Object obj) throws IOException {
		TBase tobj = (TBase) obj;
		byte[] data;
		try {
			data = serializer.serialize(tobj);
		} catch (TException e) {
			throw new IOException(e);
		}
		out.writeBinary(0, new Binary(data));
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object deserialize(PofReader in) throws IOException {
		try {
			byte[] data = in.readByteArray(0);
			TBase stub = (TBase) constructor.newInstance();
			deserializer.deserialize(stub, data);
			return stub;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
