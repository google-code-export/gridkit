package org.gridkit.coherence.search.lucene;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Solovyov
 */

public class TestDocument implements Serializable {

	private static final long serialVersionUID = 20090805L;
	
	private static int[] INT_SET = {0, 1, 2, 3, 10, 20, 30, 40, 100, 200, 300, 400, 1000, 2000, 3000, 4000}; 
	private static String[] STRING_SET = {"A", "ABC", "ABCDE", "BCDE", "X", "XY", "XYZ", "YZ", "D", "E", "F", "G", "AD", "AE", "AF", "AG"}; 
	
	private String[] stringField;
    private int[] intField;
    private int seqNo;

    public TestDocument(int seqNo) {
        this.seqNo = seqNo;

        int dim = ((32 - Integer.numberOfLeadingZeros(seqNo)) / 4) + 1; 
        
        this.stringField = new String[dim];
        this.intField = new int[dim];

        int n = seqNo;
        for (int i = 0; i < stringField.length; i++) {
            stringField[i] = STRING_SET[n % 16];
            n /= 16;
        }

        n = seqNo;
        for (int i = 0; i < intField.length; i++) {
            intField[i] = INT_SET[n % 16];
            n /= 16;
        }
    }

    public String getStringField(int index) {
        return index < stringField.length ? stringField[index] : STRING_SET[0];
    }

    public int getIntField(int index) {
        return index < intField.length ? intField[index] : INT_SET[0];
    }
    
    public static Map<Integer, TestDocument> generate(int from, int to) {
    	Map<Integer, TestDocument> map = new HashMap<Integer, TestDocument>();
    	for(int i = from; i != to; ++i) {
    		map.put(i, new TestDocument(i));
    	}
    	return map;
    }
}
