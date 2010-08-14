package org.gridkit.coherence.search.lucene;

import java.io.Serializable;

/**
 * @author Alexander Solovyov
 */

public class MockIndexedObject implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String[] stringField;
    private int[] intField;
    private int currentValue;
    private int step;

    public MockIndexedObject(int startValue, int size, int step) {
        this.currentValue = startValue % step;
        this.step = step;

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
        if (++currentValue >= step) {
            currentValue = 0;
        }

        return currentValue;
    }
}
