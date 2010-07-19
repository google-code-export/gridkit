package org.gridkit.coherence.search;

import java.util.Map;
import java.util.Set;

public interface PlugableSearchIndex<I, IC, Q> {

	public Object createIndexCompatibilityToken(IC indexConfig);
	
    public I createIndexInstance(IC indexConfig);

    public void configure(IndexEngineConfig config);
    
    public void updateIndexEntries(I index, Map<Object, IndexUpdateEvent> events, IndexInvocationContext context);
    
    public int calculateEffectiveness(I index, Q query, Set<Object> keySet, IndexInvocationContext context);
    
    public boolean applyIndex(I index, Q query, Set<Object> keySet, IndexInvocationContext context);
    
    public boolean evaluate(Q query, Object document);
    
}
