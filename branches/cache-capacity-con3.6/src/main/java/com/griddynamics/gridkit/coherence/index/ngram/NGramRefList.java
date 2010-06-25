package com.griddynamics.gridkit.coherence.index.ngram;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NGramRefList {    
    final String ngram;
    final Map<Object, int[]> references = new HashMap<Object, int[]>();
    
    public NGramRefList(String ngarm) {
        this.ngram = ngarm;
    }

    public void addRef(Object key, int position) {
        int[] pList = references.get(key);
        if (pList == null) {
            pList = new int[]{position};
        }
        else {
            pList = Arrays.copyOf(pList, pList.length + 1);
            pList[pList.length - 1] = position;
        }
        references.put(key, pList);
    }
}
