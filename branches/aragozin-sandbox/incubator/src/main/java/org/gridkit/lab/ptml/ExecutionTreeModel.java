package org.gridkit.lab.ptml;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joox.JOOX;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExecutionTreeModel {

	
	private Map<String, Object> constants = new HashMap<String, Object>();
	private Map<String, Object> variables = new HashMap<String, Object>();
	private List<Aspect> postExpansion = new ArrayList<Aspect>();
	
	private Element root;
	private Map<Node, RichNode> shadowTree = new HashMap<Node, RichNode>();
	
	public void init(Element root) {
		if (this.root != null) {
			throw new IllegalStateException("Already initialized");
		}
		transform();
	}
	
	
	private void transform() {
		List<Element> l1 = getElements(root);
		
		checkValidElement(l1, "configuration", "topology", "execution", "reporting");
		processConfiguration(l1);
		checkValidElement(l1, "topology", "execution", "reporting");
		processExpansion(l1);
		checkValidElement(l1, "topology", "execution", "reporting");
		processTopology(l1);
		checkValidElement(l1, "execution", "reporting");
		processExecution(l1);
		checkValidElement(l1, "reporting");
		if (!l1.isEmpty()) {
			// TODO improve error reporting
			throw new IllegalArgumentException();
		}
	}

	private void processConfiguration(List<Element> elements) {
		while(!elements.isEmpty()) {
			Element e = elements.get(0);
			if ("configuration".equals(e.getTagName())) {
				processConfigurationElement(e);
				destroy(e);
			}
		}		
	}

	private void processConfigurationElement(Element e) {
		List<Element> ol = getElements(e);
		for(Element s: ol) {
			String tag = s.getTagName();
			if ("assign".equals(tag)) {
				processAssign(s);
				destroy(s);
			}
			else if ("aspect".equals(tag)) {
				processAspect(s);
			}
			else if ("declare".equals(tag)) {
				processDeclare(s);
			}
		}
	}

	private void processAssign(Element s) {
		String name = s.getAttribute("name");
		String content = getTextContent(s);
		
		Object value = evaluateTemplate(content);
		assign(name, content);
		destroy(s);
	}


	private void processAspect(Element s) {
		boolean post = "true".equals(s.getAttribute("postexpand"));
		boolean bulk = !"true".equals(s.getAttribute("postexpand"));
		String selector = s.getAttribute("match");
		String lambda = getTextContent(s);
		Object value = evaluateLambda(s);
		
	}


	private void processDeclare(Element s) {
		// TODO Auto-generated method stub
		
	}


	private void processExpansion(List<Element> l1) {
		// TODO Auto-generated method stub
		
	}

	private void processTopology(List<Element> l1) {
		// TODO Auto-generated method stub
		
	}

	private void processExecution(List<Element> l1) {
		// TODO Auto-generated method stub
		
	}


	private void destroy(Element e) {
		e.getParentNode().removeChild(e);
		
	}

	private void checkValidElement(List<Element> elements, String... tags) {
		// TODO implement		
	}

	private List<Element> getElements(Element parent) {
		NodeList nl = parent.getChildNodes();
		List<Element> list = new ArrayList<Element>();
		for(int i = 0; i != nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				list.add((Element) n);
			}
		}
		return list;
	}

	private String getTextContent(Element node) {
		return node.getTextContent();
	}

	private abstract class Aspect {
		
		public abstract void apply();
		
	}
	
	private abstract class RichNode {
		
	}
}
