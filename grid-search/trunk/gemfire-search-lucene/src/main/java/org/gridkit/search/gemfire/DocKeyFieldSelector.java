package org.gridkit.search.gemfire;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class DocKeyFieldSelector implements FieldSelector {

	private static final long serialVersionUID = 20110823L;
	
	public static final DocKeyFieldSelector INSTANCE = new DocKeyFieldSelector();

	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (LuceneIndexManager.DOCUMENT_KEY.equals(fieldName)) {
			return FieldSelectorResult.LOAD_AND_BREAK;
		}
		else {
			return FieldSelectorResult.NO_LOAD;
		}
	}
}
