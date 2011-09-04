package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
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
public class LuceneIndexProcessor {
    private int changesBeforeCommit;
    private String keyFieldName;

    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;

    private int changeSetSize = 0;

    // when index commit is in progress indexChanged must be false
    private volatile boolean indexChanged = false;

    public LuceneIndexProcessor(Directory directory, IndexWriterConfig indexWriterConfig,
                                int changesBeforeCommit, String keyFieldName) throws IOException {
        this.indexWriter = new IndexWriter(directory, indexWriterConfig);
        this.indexSearcher = new IndexSearcher(IndexReader.open(directory, true));

        this.changesBeforeCommit = changesBeforeCommit;
        this.keyFieldName = keyFieldName;
    }

    public synchronized void insert(Document document, Analyzer analyzer) throws IOException {
        indexWriter.addDocument(document, getVerifiedAnalyzer(analyzer));
        recordIndexChangeAndTryCommit();
    }

    public synchronized void update(Document document, Analyzer analyzer) throws IOException {
        String cacheKey = document.getFieldable(keyFieldName).stringValue();

        indexWriter.updateDocument(getCacheKeyTerm(cacheKey), document, getVerifiedAnalyzer(analyzer));
        recordIndexChangeAndTryCommit();
    }

    public synchronized void delete(String cacheKey) throws IOException {
        indexWriter.deleteDocuments(getCacheKeyTerm(cacheKey));
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

    //TODO fix org.compass.core.lucene.engine.all.AllAnalyzer
    private Analyzer getVerifiedAnalyzer(Analyzer analyzer) {
        if (analyzer.getClass().getName().equals(AllAnalyzer.class.getName()))
            return indexWriter.getAnalyzer();
        else
            return analyzer;
    }

    private Term getCacheKeyTerm(String cacheKey) {
        return new Term(keyFieldName, cacheKey);
    }

    // TODO avoid double checking locking
    public IndexSearcher getIndexSearcher() throws IOException {
        if (indexChanged) {
            synchronized (this) {
                if (indexChanged)
                    reopenIndexSearcher();
            }
        }

        return indexSearcher;
    }

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
