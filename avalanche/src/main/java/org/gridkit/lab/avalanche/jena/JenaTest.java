package org.gridkit.lab.avalanche.jena;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class JenaTest {

	public static void main(String[] args) {

		Model rawData = ModelFactory.createNonreifyingModel();
		
		Resource rA = rawData.createResource("eg/A");
		Resource rB = rawData.createResource("eg/B");
		Resource rC = rawData.createResource("eg/C");

		Property pConcatFirst = rawData.createProperty("eg/concatFirst");
		Property pConcatSecond = rawData.createProperty("eg/concatSecond");
		Property pR = rawData.createProperty("eg/r");
		Property pP = rawData.createProperty("eg/p");
		Property pQ = rawData.createProperty("eg/q");

		rawData.add(pR.asResource(), pConcatFirst, pP.asResource());
		rawData.add(pR.asResource(), pConcatSecond, pQ.asResource());
		
		rawData.add(rA, pP, rB);
		rawData.add(rB, pQ, rC);
		
		String rules =
			    "[r1: (?c eg/concatFirst ?p), (?c eg/concatSecond ?q) -> " +
			    "     [r1b: (?x ?c ?y) <- (?x ?p ?z) (?z ?q ?y)] ]";
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		reasoner.setDerivationLogging(true);
		
		InfModel inf = ModelFactory.createInfModel(reasoner, rawData);
		System.out.println("A * * =>");
		Iterator list = inf.listStatements(null, null, (RDFNode)null);
		while (list.hasNext()) {
		    System.out.println(" - " + list.next());
		}
	}
	
}
