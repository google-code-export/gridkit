package org.gridkit.search.gemfire;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.gridkit.search.lucene.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

//TODO check if results are buffered by GemFire before send
public class IndexSearchFunction implements Function {
    private static Logger log = LoggerFactory.getLogger(IndexSearchFunction.class);

    public static final String Id = IndexSearchFunction.class.getName();

    public static final String lastResultMarker = "#lr#";
    public static final String searchEngineNotFoundMarker = "#senf#";

    private final String keyFieldName;
    private final SearchEngineRegistry searchEngineRegistry;

    private FieldSelector keyFieldSelector = new KeyFieldSelector();

    public IndexSearchFunction(String keyFieldName, SearchEngineRegistry searchEngineRegistry) {
        this.keyFieldName = keyFieldName;
        this.searchEngineRegistry = searchEngineRegistry;
    }

    @Override
    public void execute(FunctionContext functionContext) {
        Object[] arguments = (Object[])functionContext.getArguments();

        String indexProcessorName = (String)arguments[0];
        Query query = (Query)arguments[1];

        ResultSender<String> resultSender = functionContext.getResultSender();

        SearchEngine searchEngine = searchEngineRegistry.getSearchEngine(indexProcessorName);

        if (searchEngine == null) {
            resultSender.lastResult(searchEngineNotFoundMarker);
            return;
        }

        IndexSearcher indexSearcher = null;

        try {
            indexSearcher = searchEngine.acquireSearcher();
            Collector documentCollector = new DocumentCollector(resultSender);

            indexSearcher.search(query, documentCollector);
            resultSender.lastResult(lastResultMarker);
        } catch (IOException e) {
            log.warn("Exception during query execution " + query, e);
            functionContext.getResultSender().sendException(e);
        } finally {
            searchEngine.releaseSearcher(indexSearcher);
        }
    }

    @Override
    public String getId() {
        return Id;
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
        private IndexReader reader;

        private ResultSender<String> resultSender;

        private DocumentCollector(ResultSender<String> resultSender) {
            this.resultSender = resultSender;
        }

        @Override
        public void collect(int docNum) throws IOException {
            Document document = reader.document(docNum, keyFieldSelector);
            String stringKey = document.getFieldable(keyFieldName).stringValue();
            resultSender.sendResult(stringKey);
        }

        @Override
        public boolean acceptsDocsOutOfOrder() { return true; }

        @Override
        public void setScorer(Scorer scorer) throws IOException {}

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
            this.reader = reader;
        }
    }

    public static Function getIndexSearchFunctionStub() {
        return new Function() {
            @Override
            public boolean hasResult() { return true; }

            @Override
            public void execute(FunctionContext functionContext) {
                functionContext.getResultSender().lastResult(searchEngineNotFoundMarker);
            }

            @Override
            public String getId() {
                return Id;
            }

            @Override
            public boolean optimizeForWrite() { return false; }

            @Override
            public boolean isHA() { return false; }
        };
    }
}
