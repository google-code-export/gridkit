package org.gridkit.coherence.search.comparation;

import java.io.Serializable;

/**
 * @author Alexander Solovyov
 */

public class MockIndexedObject implements Serializable {
    private String stringField;
    private int intField;

    public MockIndexedObject(String stringField, int intField) {
        this.stringField = stringField;
        this.intField = intField;
    }

    public String getStringField() {
        return stringField;
    }

    public int getIntField() {
        return intField;
    }
}
