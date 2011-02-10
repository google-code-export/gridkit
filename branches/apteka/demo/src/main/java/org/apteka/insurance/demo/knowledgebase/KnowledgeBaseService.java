package org.apteka.insurance.demo.knowledgebase;

import java.util.Set;

import org.drools.KnowledgeBase;

public interface KnowledgeBaseService {
	KnowledgeBase getKnowledgeBase(String name);
	
	Set<String> getKnowledgeBaseNames();
}
