package org.gridkit.lab.avalanche.iris;

import java.io.IOException;
import java.io.InputStream;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.ProgramNotStratifiedException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.builtins.list.MakeListBuiltin;
import org.deri.iris.compiler.BuiltinRegister;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.evaluation.topdown.oldt.OLDTEvaluationStrategyFactory;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.gridkit.lab.avalanche.jena.JenaPoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrisPoc {

	private static final Logger LOGGER = LoggerFactory.getLogger(IrisPoc.class);
	
	public static void main(String[] args) throws ParserException, IOException, EvaluationException {
		
		Parser parser = new Parser();
		registerFunctors(parser);
//		parser.parse(readText("avalanche.dl"));
		parser.parse(readText("avalanche_world.dl"));
//		parser.parse(readText("test-model.dl"));
		
		
		Configuration cf = KnowledgeBaseFactory.getDefaultConfiguration();
//		cf.evaluationStrategyFactory = new WellFoundedEvaluationStrategyFactory(); 
		cf.evaluationStrategyFactory = new OLDTEvaluationStrategyFactory(); 
//		cf.evaluationStrategyFactory = new SLDNFEvaluationStrategyFactory(); 
		IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(parser.getFacts(), parser.getRules(), cf);

		LOGGER.info("Starting health check");
		if (true) {
			verify(kb, "world_test_1");
			verify(kb, "world_test_2");
			verify(kb, "world_test_3");
		}
		verifyErrors(kb);
		LOGGER.info("Health check - Ok");
		
		for(IQuery q: parser.getQueries()) {
//			System.out.println(" -> " + kb.execute(q));
			IRelation r = kb.execute(q);
			System.out.println(q.toString());
			for(int i = 0; i != r.size(); ++i) {
				System.out.println(" -> " + r.get(i).toString());
			}			
		}
	}
	
	private static void verify(IKnowledgeBase kb, String testName) throws ProgramNotStratifiedException, RuleUnsafeException, EvaluationException {
		IRelation r = kb.execute(createTestQuery(testName));
		if (r.size() == 0) {
			throw new RuntimeException("Test [" + createTestQuery(testName) + "] has failed");
		}		
	}

	private static void verifyErrors(IKnowledgeBase kb) throws ProgramNotStratifiedException, RuleUnsafeException, EvaluationException {
		IRelation r = kb.execute(createTestQuery("error"));
		for(int i = 0; i != r.size(); ++i) {
			System.err.println("ERROR -> " + r.get(i).toString());
		}			
	}
	
	private static IQuery createTestQuery(String name) {
		IBasicFactory basicFactory = BasicFactory.getInstance();
		
		ITerm t = Factory.TERM.createVariable( "M" );
		ITuple tt = basicFactory.createTuple(t);
		IPredicate p = basicFactory.createPredicate(name, 1);
		IAtom atom = basicFactory.createAtom(p, tt);
		ILiteral lit = basicFactory.createLiteral(true, atom);
		return basicFactory.createQuery(lit);
	}

	private static void registerFunctors(Parser parser) {
		
		ITerm t1 = Factory.TERM.createVariable( "a" );
		ITerm t2 = Factory.TERM.createVariable( "b" );
		ITerm t3 = Factory.TERM.createVariable( "c" );
		ITerm t4 = Factory.TERM.createVariable( "d" );
		ITerm t5 = Factory.TERM.createVariable( "e" );

		BuiltinRegister br = parser.getBuiltinRegister();
		br.registerBuiltin(new MakeListBuiltin(t1));
		br.registerBuiltin(new MakeListBuiltin(t1, t2));
		br.registerBuiltin(new MakeListBuiltin(t1, t2, t3));
		br.registerBuiltin(new MakeListBuiltin(t1, t2, t3, t4));
	}
	
	

	private static String readText(String resource) throws IOException {
		InputStream is = JenaPoc.class.getClassLoader().getResourceAsStream(resource);
		byte[] buffer = new byte[4 << 20];
		int n = is.read(buffer);
		is.close();
		return n < 0 ? "" : new String(buffer, 0, n);
	}
	
}
