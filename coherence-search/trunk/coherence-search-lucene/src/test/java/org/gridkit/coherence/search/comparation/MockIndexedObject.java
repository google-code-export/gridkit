package org.gridkit.coherence.search.comparation;

import java.io.Serializable;

/**
 * @author Alexander Solovyov
 */

public class MockIndexedObject implements Serializable {
    private String[] stringField;
    private int[] intField;
    private int currentValue;
    private int size;

    public MockIndexedObject(int startValue, int size) {
        this.currentValue = startValue;
        this.size = size;

        this.stringField = new String[size];
        this.intField = new int[size];

        for (int i = 0; i < stringField.length; i++) {
            stringField[i] = String.valueOf(nextValue());
        }

        for (int i = 0; i < stringField.length; i++) {
            intField[i] = nextValue();
        }
    }

    public String getStringField(int index) {
        return stringField[index];
    }

    public int getIntField(int index) {
        return intField[index];
    }

    private int nextValue() {
        if (++currentValue >= size) {
            currentValue = 0;
        }

        return currentValue;
    }
}
