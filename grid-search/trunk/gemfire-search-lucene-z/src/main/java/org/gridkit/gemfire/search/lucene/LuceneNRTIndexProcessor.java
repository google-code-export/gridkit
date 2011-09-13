package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.compass.core.lucene.engine.all.AllAnalyzer;

import java.io.IOException;

//TODO allow bulk index updates without interruption by read request
//TODO disable index commit in the middle of bulk write request
public class LuceneNRTIndexProcessor implements IndexProcessor {
    private int changesBeforeCommit;
    private String keyFieldName;

    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;

    private int changeSetSize = 0;

    // when index commit is in progress indexChanged must be false
    private volatile boolean indexChanged = false;

    public LuceneNRTIndexProcessor(Directory directory, IndexWriterConfig indexWriterConfig,
                                   int changesBeforeCommit, String keyFieldName) throws IOException {
        this.indexWriter = new IndexWriter(directory, indexWriterConfig);
        this.indexSearcher = new IndexSearcher(IndexReader.open(directory, true));

        this.changesBeforeCommit = changesBeforeCommit;
        this.keyFieldName = keyFieldName;
    }

    @Override
    public synchronized void insert(ObjectDocument objDoc) throws IOException {
        if (objDoc.getDocument() != null) {
            if (objDoc.getAnalyzer() != null)
                indexWriter.addDocument(objDoc.getDocument(), objDoc.getAnalyzer());
            else
                indexWriter.addDocument(objDoc.getDocument());

            recordIndexChangeAndTryCommit();
        }
    }

    @Override
    public synchronized void update(ObjectDocument objDoc) throws IOException {
        if (objDoc.getDocument() != null) {
            String cacheKey = objDoc.getDocument().getFieldable(keyFieldName).stringValue();

            if (objDoc.getAnalyzer() != null)
                indexWriter.updateDocument(getCacheKeyTerm(cacheKey), objDoc.getDocument(), objDoc.getAnalyzer());
            else
                indexWriter.updateDocument(getCacheKeyTerm(cacheKey), objDoc.getDocument());

            recordIndexChangeAndTryCommit();
        }
    }

    @Override
    public synchronized void delete(String objectKey) throws IOException {
        indexWriter.deleteDocuments(getCacheKeyTerm(objectKey));
        recordIndexChangeAndTryCommit();
    }

    private void recordIndexChangeAndTryCommit() throws IOException {
        changeSetSize += 1;

        if (changeSetSize == changesBeforeCommit) {
            reopenIndexSearcher();
            indexWriter.commit();
            changeSetSize = 0;
        }
        else {
            indexChanged = true;
        }
    }

    private Term getCacheKeyTerm(String cacheKey) {
        return new Term(keyFieldName, cacheKey);
    }

    // TODO avoid double checking locking
    @Override
    public IndexSearcher getIndexSearcher() throws IOException {
        if (indexChanged) {
            synchronized (this) {
                if (indexChanged)
                    reopenIndexSearcher();
            }
        }

        return indexSearcher;
    }

    @Override
    public void releaseIndexSearcher(IndexSearcher indexSearcher) {}

    private void reopenIndexSearcher() throws IOException {
        IndexReader indexReader = IndexReader.open(indexWriter, true);

        indexSearcher.getIndexReader().close();
        indexSearcher = new IndexSearcher(indexReader);

        indexChanged = false;
    }

    public synchronized void close() {
        try {
            indexWriter.close(true);
        } catch (IOException ignored) {}
    }
}
