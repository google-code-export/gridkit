package com.griddynamics.gridkit.coherence.index.lucene;

import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

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
        // TODO: is it needed?? hardly supported by Lucene
        throw new UnsupportedOperationException();
    }

    public Object get(Object o) {
        // TODO: check for optimization. Maybe not needed at all
        return NO_VALUE;
    }

    public Comparator getComparator() {
        return null;
    }

    public void insert(Map.Entry entry) {
        String value = (String) extractor.extract(entry.getValue());
        
        if (value != null) {
            Document doc = new Document();

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
        delete(entry);
        insert(entry);
    }

    public void delete(Map.Entry entry) {
        try {
            String value = (String) extractor.extract(entry.getValue());

            IndexReader reader = IndexReader.open(directory);
            reader.deleteDocuments(new Term("value", value));
            
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RAMDirectory getDirectory() {
        return directory;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }
}
