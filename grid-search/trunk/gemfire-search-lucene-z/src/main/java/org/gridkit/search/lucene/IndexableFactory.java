package org.gridkit.search.lucene;

import org.apache.lucene.index.Term;

import java.io.IOException;

public interface IndexableFactory {
    Indexable createIndexable(Object key, Object value) throws IOException;

    Term createKeyTerm(Object key) throws IOException;
}
