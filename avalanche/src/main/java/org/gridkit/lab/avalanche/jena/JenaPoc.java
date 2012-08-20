package org.gridkit.lab.avalanche.jena;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.hp.hpl.jena.n3.turtle.parser.TurtleParser;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;


public class JenaPoc {

	public static void main(String[] args) throws IOException {
		
		ClassLoader cl = JenaPoc.class.getClassLoader();
		
		Model baseModel = ModelFactory.createNonreifyingModel();
		baseModel.read(cl.getResourceAsStream("test-model.n3"), "", "N3");

		Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(cl.getResource("avalanche.rules").toString()));
		reasoner.setDerivationLogging(true);
		
		InfModel inf = ModelFactory.createInfModel(reasoner, baseModel);
		
		Resource solution = baseModel.createResource("avc:/solution");
		printFacts("avc:/solution", null, null, inf);
		printFacts(null, "avc:/ready", null, inf);
		printFacts(null, null, "avc:/class/transition", inf);
	}
	
	private static void printFacts(String subj, String pred, String obj, Model model) {
		StringBuilder sb = new StringBuilder();
		sb.append(subj == null ? "*" : subj).append(' ');
		sb.append(pred == null ? "*" : pred).append(' ');
		sb.append(obj  == null ? "*" : obj ).append(' ');
		sb.append("=>");
		System.out.println(sb);
		Resource r = subj == null ? null : model.createResource(subj);
		Property p = pred == null ? null : model.createProperty(pred);
		Resource o = obj == null ? null : model.createResource(obj);
		Iterator<?> list = model.listStatements(r, p, o);
		while (list.hasNext()) {
		    System.out.println(" - " + list.next());
		}		
		System.out.println();
	}

	private static String readText(String resource) throws IOException {
		InputStream is = JenaPoc.class.getClassLoader().getResourceAsStream(resource);
		byte[] buffer = new byte[4 << 20];
		int n = is.read(buffer);
		is.close();
		return n < 0 ? "" : new String(buffer, 0, n);
	}

}
