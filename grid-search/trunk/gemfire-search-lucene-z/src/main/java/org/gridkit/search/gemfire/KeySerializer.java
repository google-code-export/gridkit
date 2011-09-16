package org.gridkit.search.gemfire;

import com.gemstone.gemfire.DataSerializer;
import org.gridkit.util.Base64;

import java.io.*;

public class KeySerializer {
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
