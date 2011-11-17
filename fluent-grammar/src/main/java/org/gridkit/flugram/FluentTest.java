package org.gridkit.flugram;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.gridkit.flugram.FluentDSL.SimpleTypeResolver;
import org.junit.Test;


public class FluentTest {

	
	public interface QueryStart<N> extends SyntScope<N> {

		@AstPush
		public SpanStart<N> startSpan();
		
		@AstPush
		public PhraseNext<N> startPhrase();
		
		@AstPush
		public BooleanNext<N> startBoolean();
		
		public N term(String text);

	}
	
	public interface SingleQueryStart extends QueryStart<String> {

		@AstReplace
		public SpanStart<String> startSpan();
		
		@AstReplace
		public PhraseNext<String> startPhrase();
		
		@AstReplace
		public BooleanNext<String> startBoolean();
		
		@AstPop
		public String term(String text);
		
	}
	
	public abstract class QueryNode<N> implements AstNodeHandler<N> {
		
		public AstNodeHandler<String> startSpan() {
			return new SpanNode();
		}
		
		public void startSpan(Object span) {
			subQuery((String) span);
		}

		public AstNodeHandler<String> startPhrase() {
			return new PhraseNode();
		}

		public void startPhrase(Object span) {
			subQuery((String) span);
		}
		
		public AstNodeHandler<String> startBoolean() {
			return new BooleanNode();
		}

		public void startBoolean(Object span) {
			subQuery((String) span);
		}
		
		public String term(String text) {
			subQuery((String) text);
			// return is used for single term variant
			return text;
		}
				
		protected abstract void subQuery(String query);
	}
	
	public class SingleQueryNode extends QueryNode<String> {

		String result;
				
		@Override
		protected void subQuery(String query) {
			result = query;
		}
	}
	
	public interface SpanStart<N> extends QueryStart<SpanNext<N>> {
		
	}
	
	public interface SpanNext<N> {
		
		public QueryStart<SpanEnd<N>> near(int distance);

		public QueryStart<SpanEnd<N>> next(int distance);
	}

	public class SpanNode extends QueryNode<String> implements AstNodeHandler<String> {

		private String left;
		private String right;
		private int distance;
		private boolean inOrder;

		@Override
		protected void subQuery(String query) {
			if (left == null) {
				left = query;
			}
			else {
				right = query;
			}			
		}

		public void near(int distance) {
			this.distance = distance;
			this.inOrder = false;
		}
		
		public void next(int distance) {
			this.distance = distance;
			this.inOrder = true;
		}

		public String evaluate() {
			return "{ " + left + (inOrder ? " next/" : " near/") + distance + right + " }";
		}
	}
	
	public interface SpanEnd<N> {

		@AstPop
		public N endSpan();
		
	}
	
	public interface PhraseNext<N> extends QueryStart<PhraseNext<N>> {
		
		@AstPop
		public N endPhrase();
		
		public PhraseNext<N> slop(int n);
		
	}
	
	public class PhraseNode extends QueryNode<String> {
		
		private List<String> elements = new ArrayList<String>();
		private int slop = 1;
		
		public void slop(int n) {
			slop = n;
		}
		
		@Override
		protected void subQuery(String query) {
			elements.add(query);
		}

		public String endPhrase() {
			String terms = elements.toString().substring(1);
			terms = terms.substring(0, terms.length() - 1);
			return "{ " + terms + " /" + slop + " }";
		}
	}
	
	public interface BooleanNext<N> {
		
		@AstPop
		public N endBoolean();
		
		@AstPush
		public QueryStart<BooleanNext<N>> must();

		@AstPush
		public QueryStart<BooleanNext<N>> mustNot();
		
		@AstPush
		public QueryStart<BooleanNext<N>> should();
		
		public BooleanNext<N> minShouldCount();
	}	
	
	public class BooleanNode implements AstNodeHandler<String>{
		
		StringBuilder result  = new StringBuilder();

		public void must(Object term) {};
		public void mustNot(Object term) {};
		public void should(Object term) {};
		
		public AstNodeHandler<String> must() {
			return new QueryNode<String>() {

				public String evaluate() {
					return null;
				}

				@Override
				protected void subQuery(String query) {
					result.append("+").append(query).append(" ");
				}
				
			};
		}

		public AstNodeHandler<String> mustNot() {
			return new QueryNode<String>() {
				
				public String evaluate() {
					return null;
				}
				
				@Override
				protected void subQuery(String query) {
					result.append("-").append(query).append(" ");
				}
				
			};
		}

		public AstNodeHandler<String> should() {
			return new QueryNode<String>() {
				
				public String evaluate() {
					return null;
				}
				
				@Override
				protected void subQuery(String query) {
					result.append("?").append(query).append(" ");
				}
				
			};
		}
				
		public String evaluate() {
			return "{" + result.toString() + "}";
		}
	}
	
	@Test
	public void fluent_term() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(SingleQueryStart.class, new SingleQueryNode()))
					.term("XXX");
		
		Assert.assertEquals("XXX", result);		
	}

	@Test
	public void fluent_phrase() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(SingleQueryStart.class, new SingleQueryNode()))
				.startPhrase()
					.term("x")
					.term("y")
					.term("z")
				.endPhrase();
		
		Assert.assertEquals("{ x, y, z /1 }", result);		
	}

	@Test
	public void fluent_phrase_nest() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(SingleQueryStart.class, new SingleQueryNode()))
				.startPhrase()
					.term("x")
					.startPhrase()
						.term("yx")
						.slop(10)
						.term("xz")
					.endPhrase()
					.term("z")
				.endPhrase();
		
		Assert.assertEquals("{ x, { yx, xz /10 }, z /1 }", result);		
	}

	@Test
	public void fluent_snap() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(SingleQueryStart.class, new SingleQueryNode()))
				.startSpan()
					.term("x")
					.next(2)
					.term("y")
				.endSpan();
		
		Assert.assertEquals("{ x, { yx, xz /10 }, z /1 }", result);		
	}

	@Test
	public void lookupTopInterface_SpanStart() throws SecurityException, NoSuchMethodException {
		Method m = QueryStart.class.getMethod("startPhrase", null);
		Class[] interfaces = {SpanStart.class};
		SpanStart.class.getGenericInterfaces();
		Assert.assertEquals(SpanStart.class, FluentDSL.lookupTopInterface(interfaces, m));
	}

	@Test
	public void testScopeResolver_SpanStart() {

		SimpleTypeResolver resolver = new SimpleTypeResolver();
		
		resolver.bind(SpanStart.class);
		
		resolver.toString();
		
		Class c = resolver.findSyntaticScope();
	
	}
	
//	@Test @Ignore
//	@SuppressWarnings("unused")
//	public void inferReturnType_SpanStart() throws SecurityException, NoSuchMethodException {
//		Method m = SpanStart.class.getMethod("startPhrase", null);
//		
//		Type rt = m.getGenericReturnType();
//		ParameterizedType pt = (ParameterizedType) rt;
//		Class rpt = (Class) pt.getRawType();
//		for(Type i : rpt.getGenericInterfaces()) {			
//			if (i instanceof ParameterizedType && FluentDSL.isAssignable(SyntScope.class, i)) {
//				ParameterizedType ps = (ParameterizedType) i;
//				Type argT = ps.getActualTypeArguments()[0];
//				argT.toString();
//			}
//		}
		
//		Class<?> c = FluentDSL.inferReturnType(m);
//		Assert.assertEquals(SpanNext.class, c);
//	}
	
}
