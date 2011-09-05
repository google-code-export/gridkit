package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IndexSearchFunction implements Function {
    private static Logger log = LoggerFactory.getLogger(IndexSearchFunction.class);

    public static final String lastResultMarker = "#lr#";

    private String id;

    private String keyFieldName;
    private LuceneIndexProcessor indexProcessor;

    private FieldSelector keyFieldSelector = new KeyFieldSelector();

    public IndexSearchFunction(LuceneIndexProcessor indexProcessor,
                               String keyFieldName, String regionFullPath) {
        this.indexProcessor = indexProcessor;
        this.keyFieldName = keyFieldName;

        this.id = getId(regionFullPath);
    }

    @Override
    public void execute(FunctionContext functionContext) {
        Query query = (Query)functionContext.getArguments();
        ResultSender<String> resultSender = functionContext.getResultSender();

        try {
            IndexSearcher indexSearcher = indexProcessor.getIndexSearcher();
            Collector documentCollector = new DocumentCollector(indexSearcher, resultSender);

            indexSearcher.search(query ,documentCollector);
            resultSender.lastResult(lastResultMarker);
        } catch (IOException e) {
            log.warn("Exception during query execution " + query, e);
            functionContext.getResultSender().sendException(e);
        }
    }

    public static String getId(String regionFullPath) {
        return String.format("%s(%s)", IndexSearchFunction.class.getName(), regionFullPath);
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

    private class KeyFieldSelector implements FieldSelector {
        @Override
        public FieldSelectorResult accept(String fieldName) {
            if (fieldName.equals(keyFieldName))
                return FieldSelectorResult.LOAD_AND_BREAK;
            else
                return FieldSelectorResult.NO_LOAD;
        }
    }

    private class DocumentCollector extends Collector {
        private IndexSearcher indexSearcher;
        private ResultSender<String> resultSender;

        private DocumentCollector(IndexSearcher indexSearcher,
                                  ResultSender<String> resultSender) {
            this.indexSearcher = indexSearcher;
            this.resultSender = resultSender;
        }

        @Override
        public void collect(int docNum) throws IOException {
            Document document = indexSearcher.doc(docNum, keyFieldSelector);
            String stringKey = document.getFieldable(keyFieldName).stringValue();
            resultSender.sendResult(stringKey);
        }

        @Override
        public boolean acceptsDocsOutOfOrder() { return true; }

        @Override
        public void setScorer(Scorer scorer) throws IOException {}

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {}
    }

    public static Function getIndexSearchFunctionStub(final String regionFullPath) {
        return new Function() {
            @Override
            public boolean hasResult() { return true; }

            @Override
            public void execute(FunctionContext paramFunctionContext) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getId() {
                return IndexSearchFunction.getId(regionFullPath);
            }

            @Override
            public boolean optimizeForWrite() { return false; }

            @Override
            public boolean isHA() { return false; }
        };
    }
}
