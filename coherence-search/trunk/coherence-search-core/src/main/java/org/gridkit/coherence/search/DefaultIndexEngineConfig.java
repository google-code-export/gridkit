package org.gridkit.coherence.search;

public class DefaultIndexEngineConfig implements IndexEngineConfig {

	private int indexUpdateQueueSizeLimit = 1024;
	private int indexUpdateDelay = 5000;
	private boolean attributeIndexEnabled = false;
	private boolean oldValueOnUpdateEnabled = false;
	
	public int getIndexUpdateQueueSizeLimit() {
		return indexUpdateQueueSizeLimit;
	}
	
	public void setIndexUpdateQueueSizeLimit(int indexUpdateQueueSizeLimit) {
		this.indexUpdateQueueSizeLimit = indexUpdateQueueSizeLimit;
	}
	
	public int getIndexUpdateDelay() {
		return indexUpdateDelay;
	}
	
	public void setIndexUpdateDelay(int indexUpdateDelay) {
		this.indexUpdateDelay = indexUpdateDelay;
	}
	
	public boolean isAttributeIndexEnabled() {
		return attributeIndexEnabled;
	}
	
	public void setAttributeIndexEnabled(boolean attributeIndexEnabled) {
		this.attributeIndexEnabled = attributeIndexEnabled;
	}
	
	public boolean isOldValueOnUpdateEnabled() {
		return oldValueOnUpdateEnabled;
	}

	public void setOldValueOnUpdateEnabled(boolean oldValueOnUpdateEnabled) {
		this.oldValueOnUpdateEnabled = oldValueOnUpdateEnabled;
	}
}
