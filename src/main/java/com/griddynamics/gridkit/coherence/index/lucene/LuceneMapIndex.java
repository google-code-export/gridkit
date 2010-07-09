package com.griddynamics.gridkit.coherence.index.lucene;

import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;

import java.util.Comparator;
import java.util.Map;
import java.io.IOException;

/**
 * @author Alexander Solovyov
 */

public class LuceneMapIndex implements MapIndex {
    private final ValueExtractor extractor;

    private RAMDirectory directory = new RAMDirectory();
    private Analyzer analyzer = new WhitespaceAnalyzer();

    public LuceneMapIndex(ValueExtractor extractor) {
        this.extractor = extractor;
    }

    public ValueExtractor getValueExtractor() {
        return extractor;
    }

    public boolean isOrdered() {
        return false;
    }

    public boolean isPartial() {
        return false;
    }

    public Map getIndexContents() {
        // TODO: check again
        throw new UnsupportedOperationException();
    }

    public Object get(Object o) {
        // TODO: check again
        return NO_VALUE;
    }

    public Comparator getComparator() {
        return null;
    }

    public void insert(Map.Entry entry) {
        Document doc = new Document();
        String value = (String) extractor.extract(entry.getValue());
        if (value != null) {
            doc.add(new Field("value", value, Field.Store.YES, Field.Index.ANALYZED));

            if (entry instanceof BinaryEntry) {
                doc.add(new Field("key", ((BinaryEntry)entry).getBinaryKey().toByteArray(), Field.Store.YES));
            }
            else {
                doc.add(new Field("key", (String) entry.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            }

            try {
                IndexWriter writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
                writer.addDocument(doc);
                writer.optimize();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void update(Map.Entry entry) {
        // TODO: check again
        insert(entry);
    }

    public void delete(Map.Entry entry) {
        // TODO: check again
        throw new UnsupportedOperationException();
    }

    public RAMDirectory getDirectory() {
        return directory;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }
}
