package com.griddynamics.gridkit.coherence.index.ngram;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;

public class NGramMapIndex implements MapIndex {

    final int ngramSize;
    final ValueExtractor extractor;
    final Map<String, NGramRefList> index = new HashMap<String, NGramRefList>();
    final Map<Object, String[]> extract = new HashMap<Object, String[]>();  
    
    public NGramMapIndex(int ngramSize, ValueExtractor extractor) {
        this.ngramSize = ngramSize;
        this.extractor = extractor;
    }

    public int getNGramSize() {
        return ngramSize;
    }
    
    @Override
    public void insert(Entry entry) {
        String[] split = (String[]) extractor.extract(entry.getValue());
        if (split != null) {
            Object entryKey = getEntryKey(entry);
            updateIndex(entryKey, split);
            extract.put(entryKey, normalize(split));
        }
    }

    Object getEntryKey(Entry entry) {
        if (entry instanceof BinaryEntry) {
            return ((BinaryEntry)entry).getBinaryKey();
        }
        else {
            return entry.getKey();
        }
    }
    
    private String[] normalize(String[] split) {
        for(int i = 0 ; i != split.length; ++i) {
            NGramRefList list = index.get(split[i]);
            if (list != null) {
                split[i] = list.ngram;
            }
        }
        return split;
    }

    private void updateIndex(Object key, String[] split) {
        for(int i = 0; i != split.length; ++i) {
            insertToIndex(split[i], key, i);
        }
    }

    @Override
    public void update(Entry entry) {
        delete(entry);
        insert(entry);
    }

    @Override
    public void delete(Entry entry) {
        Object key = entry.getKey();
        String[] split = extract.get(key);
        if (split != null) {
            for(String ngram : split) {
                removeFromIndex(ngram, key);
            }
        }
    }

    
    
    private void insertToIndex(String ngram, Object key, int position) {
        NGramRefList list = index.get(ngram);
        if (list == null) {
            list = new NGramRefList(new String(ngram));
            index.put(list.ngram, list);
        }
        
        list.addRef(key, position);
    }
    
    private void removeFromIndex(String ngram, Object key) {
        NGramRefList list = index.get(ngram);
        if (list != null) {
            list.references.remove(key);
            if (list.references.size() == 0) {
                index.remove(ngram);
            }
        }
    }

    @Override
    public Object get(Object oKey) {
        return NO_VALUE;
    }

    @Override
    public Comparator getComparator() {
        return null;
    }

    @Override
    public Map getIndexContents() {
        return index;
    }

    @Override
    public ValueExtractor getValueExtractor() {
        return extractor;
    }

    @Override
    public boolean isOrdered() {
        return false;
    }
    
    @Override
    public boolean isPartial() {
        return false;
    }

    public Collection lookUpNGram(String ngram) {
        NGramRefList refList = index.get(ngram); 
        return refList == null ? Collections.EMPTY_SET : refList.references.keySet();
    }
}
