package org.gridkit.coherence.search.comparation;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Alexander Solovyov
 */

public class MockIndexedObject implements Serializable {
    private String[] stringField;
    private int[] intField;

    public MockIndexedObject(String stringValue, int intValue, int size) {
        this.stringField = new String[size];
        Arrays.fill(stringField, stringValue);

        this.intField = new int[size];
        Arrays.fill(intField, intValue);
    }

    public String getStringField(int index) {
        return stringField[index];
    }

    public int getIntField(int index) {
        return intField[index];
    }
}
