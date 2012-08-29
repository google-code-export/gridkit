/**
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
