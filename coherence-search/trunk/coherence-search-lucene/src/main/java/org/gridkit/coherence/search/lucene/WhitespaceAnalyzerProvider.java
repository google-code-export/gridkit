package org.gridkit.coherence.search.lucene;

import java.io.Serializable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class WhitespaceAnalyzerProvider implements LuceneAnalyzerProvider, Serializable {

	private static final long serialVersionUID = 20100720L;

	public Analyzer getAnalyzer() {
		return new WhitespaceAnalyzer();
	}
	
}
