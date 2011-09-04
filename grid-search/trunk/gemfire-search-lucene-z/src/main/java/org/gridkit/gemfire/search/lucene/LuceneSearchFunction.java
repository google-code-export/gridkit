package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LuceneSearchFunction implements Function {
    public static final String lastResult = "#lr#";

    private static Logger log = LoggerFactory.getLogger(LuceneSearchFunction.class);

    private String id;
    private String keyFieldName;

    private LuceneIndexProcessor indexProcessor;
    private FieldSelector fieldSelector = new KeyFieldSelector();

    public LuceneSearchFunction(LuceneIndexProcessor indexProcessor,
                                String id, String keyFieldName) {
        this.indexProcessor = indexProcessor;

        this.id = id;
        this.keyFieldName = keyFieldName;
    }

    @Override
    public void execute(FunctionContext functionContext) {
        Query query = (Query)functionContext.getArguments();
        ResultSender<String> resultSender = functionContext.getResultSender();

        try {
            IndexSearcher indexSearcher = indexProcessor.getIndexSearcher();
            TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

            for(int i = 0; i < topDocs.totalHits; ++i) {
                Document document = indexSearcher.doc(topDocs.scoreDocs[i].doc, fieldSelector);
                String stringKey = document.getFieldable(keyFieldName).stringValue();
                resultSender.sendResult(stringKey);
            }

            resultSender.lastResult(lastResult);
        } catch (IOException e) {
            log.warn("Exception during query execution " + query, e);
            functionContext.getResultSender().sendException(e);
        }
    }

    private class KeyFieldSelector implements FieldSelector {
        @Override
        public FieldSelectorResult accept(String fieldName) {
            if (fieldName.equals(keyFieldName))
                return FieldSelectorResult.LOAD_AND_BREAK;
            else
                return FieldSelectorResult.NO_LOAD;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }

    public static Function getStubSearchFunction(final String id) {
        return new StubSearchFunction(id);
    }

    private static class StubSearchFunction implements Function {
        private String id;

        public StubSearchFunction(String id) {
            this.id = id;
        }

        public boolean hasResult() { return true; }

        public void execute(FunctionContext functionContext) {
            functionContext.getResultSender().lastResult(lastResult);
        }

        public String getId() { return id; }

        public boolean optimizeForWrite() { return false; }

        public boolean isHA() { return false; }
    }
}
