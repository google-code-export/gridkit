package org.apteka.insurance.demo.controller;

import java.io.IOException;
import java.util.Map;

import org.apteka.insurance.demo.cache.CacheService;
import org.apteka.insurance.demo.knowledgebase.KnowledgeBaseService;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class Main {
	@Autowired
	private KnowledgeBaseService knowledgeBaseService;
	
	@Autowired
	private CacheService cacheService;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@RequestMapping("/")
	public String index() {
		return "index";
	}
	
	@RequestMapping(value = "/process", method = RequestMethod.POST)
	public String process(@RequestParam("knowledgeBase") String knowledgeBase,
						  @RequestParam("object") String object) throws JsonParseException, JsonMappingException, IOException {
		StatefulKnowledgeSession knowledgeSession = knowledgeBaseService.getKnowledgeBase(knowledgeBase).newStatefulKnowledgeSession();
		
		knowledgeSession.setGlobal("cacheService", cacheService);
		
		knowledgeSession.insert(objectMapper.readValue(object, Map.class));
		
		knowledgeSession.fireAllRules();
		
		return "redirect:";
	}
}
