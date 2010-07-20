package org.gridkit.coherence.search;

/**
 * API to configure options of Coherence indexing engine.
 * @author aragozin
 *
 */
public interface IndexEngineConfig {
	
	/**
	 * @return max queue size for asynchronous index updates. 0 - updates are synchronous.
	 */
	public int getIndexUpdateQueueSizeLimit();
	
	/**
	 * Sets asynchronous update queue size limit. 0 - updates are synchronous.
	 * @param queueSize
	 */
	public void setIndexUpdateQueueSizeLimit(int queueSize);
	
	/**
	 * Max delay in ms before update will be applied to index if asynchronous updates are enables.
	 */
	public int getIndexUpdateDelay();

	/**
	 * Sets Max delay in ms before update will be applied to index if asynchronous updates are enables. 
	 * @param indexUpdateDelay
	 */
	public void setIndexUpdateDelay(int indexUpdateDelay);
	
	/**
	 * Search engine can maintain separate standard index based of same
	 * extractor. This index is available for common Coherence filters
	 * and can be used to retrieve original values on updates.
	 * @return <code>true</code> if attribute index enabled 
	 */
	public boolean isAttributeIndexEnabled();

	/**
	 * Set attributeIndex option. Seee {@link #isAttributeIndexEnabled()}
	 */
	public void setAttributeIndexEnabled(boolean enabled);
	
	/**
	 * Up on updates or deletes search engine may put some effort to retrieve value of attribute before update.
	 * If particular index does not need this, option can be turned off to save some processing.
	 * @return <code>true</code> original value will be retrieved
	 */
	public boolean isOldValueOnUpdateEnabled();
	
	/**
	 * Set oldValueOnUpdate option. See {@link #isOldValueOnUpdateEnabled()}
	 */
	public void setOldValueOnUpdateEnabled(boolean enabled);
	
}
