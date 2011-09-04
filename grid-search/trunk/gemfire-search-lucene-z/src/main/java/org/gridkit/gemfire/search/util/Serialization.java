package org.gridkit.gemfire.search.util;

import com.gemstone.gemfire.DataSerializer;

import java.io.*;

public class Serialization {
    public static String toString(Object key) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(64);
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        DataSerializer.writeObject(key, dataOutputStream, true);
        byte[] binaryKey = outputStream.toByteArray();

        dataOutputStream.close();

        return Base64.encodeToString(binaryKey, false);
    }

    public static Object toObject(String stringKey) throws ClassNotFoundException, IOException {
        byte[] binaryKey = Base64.decodeFast(stringKey);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryKey);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        Object key = DataSerializer.readObject(dataInputStream);

        dataInputStream.close();

        return key;
    }
}
