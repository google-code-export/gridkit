package com.griddynamics.gridkit.coherence.index.ngram;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.IndexAwareFilter;

public class ContainsSubstringFilter implements IndexAwareFilter {

    private ValueExtractor extractor;
    private String substring;
    
    ContainsSubstringFilter() {
        // POF consructor
    }
    
    public ContainsSubstringFilter(String substring) {
        this.extractor = new NGramExtractor(null, Integer.MAX_VALUE);
        this.substring = substring;
    }

    public ContainsSubstringFilter(ValueExtractor extractor, String substring) {
        this.extractor = new NGramExtractor(extractor, Integer.MAX_VALUE);
        this.substring = substring;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Filter applyIndex(Map mapIndexes, Set setKeys) {
        NGramMapIndex index = (NGramMapIndex) mapIndexes.get(extractor);
        return ngramFilter(index, setKeys);
    }

    private Filter ngramFilter(NGramMapIndex index, Set setKeys) {
        if (index.getNGramSize() <= substring.length()) {
            filterSet(index, setKeys);
            return null;
        }
        else {
            unionFilterSet(index, setKeys);
            return null;
        }
    }

    private void unionFilterSet(NGramMapIndex index, Set setKeys) {
        Set result = new HashSet();
        for(Object entry: index.getIndexContents().entrySet()) {
            String ngram = (String) ((Map.Entry)entry).getKey();
            NGramRefList list = (NGramRefList) ((Map.Entry)entry).getValue();
            
            if (ngram.indexOf(substring) >= 0) {
                for(Object key: list.references.keySet()) {
                    if (setKeys.contains(key)) {
                        result.add(key);
                    }
                }
            }            
        }
        setKeys.retainAll(result);
    }

    private void filterSet(NGramMapIndex index, Set setKeys) {
        int n = 0;
        int ngramSize = index.getNGramSize();
        while(true) {
            String ngram = substring.substring(n, n + ngramSize);
            setKeys.retainAll(index.lookUpNGram(ngram));     
            if (setKeys.isEmpty()) {
                break;
            }
            n += ngramSize;
            if (n == substring.length()) {
                break;
            }
            else if (n + ngramSize > substring.length()) {
                int over = n + ngramSize - substring.length();
                n -= over;
            }
        }
    }

    @Override
    public int calculateEffectiveness(Map mapIndexes, Set setKeys) {
        NGramMapIndex index = (NGramMapIndex) mapIndexes.get(extractor);
        return index == null ? Integer.MAX_VALUE : 1;
    }

    @Override
    public boolean evaluateEntry(Entry entry) {
        return evaluate(entry);
    }

    @Override
    public boolean evaluate(Object o) {
        String[] text = (String[]) extractor.extract(o);
        return text != null && text[0].indexOf(substring) >= 0;
    }
}
