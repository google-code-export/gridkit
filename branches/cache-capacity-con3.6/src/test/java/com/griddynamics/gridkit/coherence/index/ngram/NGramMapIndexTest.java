package com.griddynamics.gridkit.coherence.index.ngram;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class NGramMapIndexTest {

    
    @Test 
    public void test1() {
        
        Map<String, String> content = new HashMap<String, String>();
        content.put("ABCD", "ABCD");
        content.put("ABCE", "ABCE");
        content.put("X", "X");
        content.put("XYZ", "XYZ");
        content.put("XABZ", "XABZ");
        
        Assert.assertEquals(search(2, content, "AB"), new String[]{"ABCD", "ABCE", "XABZ"});
        Assert.assertEquals(search(2, content, "ABC"), new String[]{"ABCD", "ABCE"});
        Assert.assertEquals(search(2, content, "X"), new String[]{"X", "XABZ", "XYZ"});
    }

    private Object[] search(int i, Map<String, String> content, String string) {
        Map mapIndex = new HashMap();
        NGramExtractor extractor = new NGramExtractor(i);
        NGramMapIndex index = (NGramMapIndex) extractor.createIndex(false, null, mapIndex);
        ContainsSubstringFilter filter = new ContainsSubstringFilter(string);
        
        for(Map.Entry<String, String> entry: content.entrySet()) {
            index.insert(entry);
        };
        
        Set keys = new TreeSet(content.keySet());
        filter.applyIndex(mapIndex, keys);
        
        return keys.toArray();
    }
    
    
}
