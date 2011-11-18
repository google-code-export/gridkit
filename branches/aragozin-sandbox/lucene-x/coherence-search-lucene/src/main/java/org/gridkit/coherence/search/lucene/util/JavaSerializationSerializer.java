/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
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

package org.gridkit.coherence.search.lucene.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.tangosol.io.Serializer;
import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.WriteBuffer.BufferOutput;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

/**
 * Coherence serializer using default java serialization.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class JavaSerializationSerializer implements Serializer, PofSerializer {

	public static Object fromBytes(byte[] buf) throws IOException {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object result = ois.readObject();
			return result;
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found '" + e.getMessage() + "'");
		}
	}

	public static byte[] toBytes(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(object);
		oos.close();
		byte[] byteArray = bos.toByteArray();
		return byteArray;
	}

	@Override
	public Object deserialize(BufferInput in) throws IOException {
		byte[] buf = new byte[in.available()];
		in.read(buf);
		return fromBytes(buf);
	}

	@Override
	public Object deserialize(PofReader in) throws IOException {
		byte[] data = in.readByteArray(0);
		in.readRemainder();
		return fromBytes(data);
	}
	
	@Override
	public void serialize(BufferOutput out, Object object) throws IOException {
		byte[] byteArray = toBytes(object);
		out.write(byteArray);
	}

	@Override
	public void serialize(PofWriter out, Object object)	throws IOException {
		byte[] data = toBytes(object);
		out.writeByteArray(0, data);
		out.writeRemainder(null);
	}
}
