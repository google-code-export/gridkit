package org.apteka.insurance.demo.knowledgebase.memory;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apteka.insurance.demo.knowledgebase.KnowledgeBaseService;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
	private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseServiceImpl.class);
	
	private Map<String, KnowledgeBase> knowledgeBases = new HashMap<String, KnowledgeBase>();
	
	public KnowledgeBaseServiceImpl(Map<String, Set<String>> knowledgeBaseLocations) {
		for (Map.Entry<String, Set<String>> knowledgeBase : knowledgeBaseLocations.entrySet()) {
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			
			for (String location : knowledgeBase.getValue())
				kbuilder.add(ResourceFactory.newClassPathResource(location), ResourceType.DRL);
			
			if (kbuilder.hasErrors())
				log.warn(format("Errors creating knowledge base '%s': %s", knowledgeBase.getKey(), kbuilder.getErrors().toString()));
			
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
			
			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			
			knowledgeBases.put(knowledgeBase.getKey().toUpperCase(), kbase);
		}
	}
	
	public KnowledgeBase getKnowledgeBase(String name) {
		return knowledgeBases.get(name.toUpperCase());
	}

	public Set<String> getKnowledgeBaseNames() {
		return knowledgeBases.keySet();
	}
}
