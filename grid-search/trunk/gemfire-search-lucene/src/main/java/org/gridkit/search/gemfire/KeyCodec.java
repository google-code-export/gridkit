package org.gridkit.search.gemfire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.gemstone.gemfire.DataSerializer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class KeyCodec {

	public static Object stringToObject(String str) {
		byte[] bytes = Base64.base64ToByteArray(str);
		try {
			return DataSerializer.readObject(new DataInputStream(new ByteArrayInputStream(bytes)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String objectToString(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			DataSerializer.writeObject(o, dos, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		byte[] bytes = bos.toByteArray();
		return Base64.byteArrayToBase64(bytes);
	}
}
