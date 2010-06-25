package com.griddynamics.gridkit.coherence.index.ngram;

import java.util.Comparator;
import java.util.Map;

import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IndexAwareExtractor;

public class NGramExtractor implements IndexAwareExtractor {

    private ValueExtractor extractor;
    private int ngramSize;
    
    NGramExtractor() {
        // POF constructor
    }
    
    public NGramExtractor(int ngramSize) {
        this.extractor = null;
        this.ngramSize = ngramSize;
    }
    
    public NGramExtractor(ValueExtractor extractor, int ngramSize) {
        this.extractor = extractor;
        this.ngramSize = ngramSize;
    }

    @Override
    public MapIndex createIndex(boolean fOrdered, Comparator comparator, Map mapIndex) {
        NGramMapIndex index = new NGramMapIndex(ngramSize, this);
        mapIndex.put(this, index);
        return index;
    }

    @Override
    public MapIndex destroyIndex(Map mapIndex) {
        return (MapIndex) mapIndex.remove(this);
    }

    @Override
    public Object extract(Object oTarget) {
        return oTarget == null ? null : split((String)(extractor == null ? oTarget : extractor.extract(oTarget)));
    }

    private Object split(String string) {
        if (string.length() == 0) {
            return null;
        }
        else if (string.length() <= ngramSize) {
            return new String[]{string};
        }
        else {
            int n = string.length() - ngramSize + 1;
            String[] split = new String[n];
            for(int i = 0; i != n; ++i) {
                split[i] = string.substring(i, i + ngramSize);
            }
            return split;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extractor == null) ? 0 : extractor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NGramExtractor other = (NGramExtractor) obj;
        if (extractor == null) {
            if (other.extractor != null)
                return false;
        } else if (!extractor.equals(other.extractor))
            return false;
        return true;
    }
}
