package org.gridkit.lab.meterx;

public interface HierarchyAppender {

	public void addAttribute(String name, String value);
	
	public void addTrait(String trait);
	
	public void metaSkip();
	
	public void metaDone();
	
}
