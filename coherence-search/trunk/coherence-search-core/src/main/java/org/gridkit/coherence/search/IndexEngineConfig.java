package org.gridkit.coherence.search;

public interface IndexEngineConfig {
	public int getIndexUpdateQueueSizeLimit();
	public void setIndexUpdateQueueSizeLimit(int queueSize);
	public int getIndexUpdateDelay();
	public void setIndexUpdateDelay(int indexUpdateDelay);
	public boolean isAttributeIndexEnabled();
	public void setAttributeIndexEnabled(boolean enabled);
	public boolean isOldValueOnUpdateEnabled();
	public void setOldValueOnUpdateEnabled(boolean enabled);
}
