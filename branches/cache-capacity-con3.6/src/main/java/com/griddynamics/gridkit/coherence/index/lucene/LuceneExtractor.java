package com.griddynamics.gridkit.coherence.index.lucene;

import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IndexAwareExtractor;

import java.util.Comparator;
import java.util.Map;

/**
 * @author Alexander Solovyov
 */

public class LuceneExtractor implements IndexAwareExtractor {

    private final ValueExtractor extractor;

    public LuceneExtractor(ValueExtractor extractor) {
        this.extractor = extractor;
    }

    public MapIndex createIndex(boolean b, Comparator comparator, Map map) {
        LuceneMapIndex index = new LuceneMapIndex(this);
        map.put(this, index);
        return index;
    }

    public MapIndex destroyIndex(Map map) {
        return (MapIndex) map.remove(this);
    }

    public Object extract(Object obj) {
        return obj == null
                ? null
                : (String) (extractor == null ? obj : extractor.extract(obj));
    }

    @Override
    public int hashCode() {
        final int prime = 117;
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
        LuceneExtractor other = (LuceneExtractor) obj;
        if (extractor == null) {
            if (other.extractor != null)
                return false;
        } else if (!extractor.equals(other.extractor))
            return false;
        return true;
    }
}
