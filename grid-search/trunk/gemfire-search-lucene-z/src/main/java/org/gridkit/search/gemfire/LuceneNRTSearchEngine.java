package org.gridkit.search.gemfire;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.gridkit.search.lucene.Indexable;
import org.gridkit.search.lucene.SearchEngine;

import java.io.IOException;

//TODO allow bulk index updates without interruption by read request
//TODO disable index commit in the middle of bulk write request
public class LuceneNRTSearchEngine implements SearchEngine {
    private int changesBeforeCommit;

    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;

    private int changeSetSize = 0;

    // when index commit is in progress indexChanged must be false
    private volatile boolean indexChanged = false;

    public LuceneNRTSearchEngine(Directory directory,
                                 IndexWriterConfig indexWriterConfig,
                                 int changesBeforeCommit) throws IOException {
        this.indexWriter = new IndexWriter(directory, indexWriterConfig);
        this.indexSearcher = new IndexSearcher(IndexReader.open(directory, true));
        this.changesBeforeCommit = changesBeforeCommit;
    }

    @Override
    public synchronized void insert(Indexable indexable) throws IOException {
        if (indexable.getAnalyzer() != null)
            indexWriter.addDocument(indexable.getDocument(), indexable.getAnalyzer());
        else
            indexWriter.addDocument(indexable.getDocument());

            recordIndexChangeAndTryCommit();
    }

    @Override
    public synchronized void update(Indexable indexable) throws IOException {
        if (indexable.getAnalyzer() != null)
            indexWriter.updateDocument(indexable.getKeyTerm(), indexable.getDocument(), indexable.getAnalyzer());
        else
            indexWriter.updateDocument(indexable.getKeyTerm(), indexable.getDocument());

        recordIndexChangeAndTryCommit();
    }

    @Override
    public synchronized void delete(Term term) throws IOException {
        indexWriter.deleteDocuments(term);
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


    // TODO avoid double checking locking
    @Override
    public IndexSearcher acquireSearcher() throws IOException {
        if (indexChanged) {
            synchronized (this) {
                if (indexChanged)
                    reopenIndexSearcher();
            }
        }

        return indexSearcher;
    }

    @Override
    public void releaseSearcher(IndexSearcher indexSearcher) {}

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
