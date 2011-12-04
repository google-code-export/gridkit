package org.gridkit.flugram;


import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.gridkit.flugram.FluentDSL.TrackedType;
import org.junit.Test;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class FluentDSLTest {
	
	public interface QueryStart<N> {

		@AstPush
		public SpanStart<N> startSpan();
		
		@AstPush
		public PhraseNext<N> startPhrase();
		
		@AstPush
		public BooleanNext<N> startBoolean();
		
		public N term(String text);

	}

	// This class is changing annotations to pop syntatic scope as soon as symbol is formed
	public interface SingleQueryStart<N> extends QueryStart<N> {
		
		@AstReplace
		public SpanStart<N> startSpan();
		
		@AstReplace
		public PhraseNext<N> startPhrase();
		
		@AstReplace
		public BooleanNext<N> startBoolean();
		
		@AstPop
		public N term(String text);
		
	}

	public interface QueryRoot extends SingleQueryStart<String> {

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

		public String endSpan() {
			return "{ " + left + (inOrder ? " next/" : " near/") + distance + " "+ right + " }";
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
		public SingleQueryStart<BooleanNext<N>> must();

		@AstPush
		public SingleQueryStart<BooleanNext<N>> mustNot();
		
		@AstPush
		public SingleQueryStart<BooleanNext<N>> should();
		
		public BooleanNext<N> minShouldCount(int count);
	}	
	
	public class BooleanNode implements AstNodeHandler<String>{
		
		int minShouldCount = 1;
		StringBuilder result  = new StringBuilder();

		public AstNodeHandler<?> must() {
			return new SingleQueryNode();
		}

		public AstNodeHandler<?> mustNot() {
			return new SingleQueryNode();
		}

		public AstNodeHandler<?> should() {
			return new SingleQueryNode();
		}
		
		public void must(Object term) {
			result.append(" must ").append(term);
		};
		
		public void mustNot(Object term) {
			result.append(" mustNot ").append(term);
		};
		
		public void should(Object term) {
			result.append(" should ").append(term);
		};		
		
		public void minShouldCount(int count) {
			minShouldCount = count;
		}

		public String endBoolean() {
			return "{" + result.toString() + " /opt:" + minShouldCount + "}";
		}
	}
	
	@Test
	public void test_type_tracking__term() {
		
		TrackedType tt = new TrackedType(QueryRoot.class);
		Assert.assertEquals(QueryRoot.class, tt.getRawType());
		tt = tt.resolve(getMethodType(tt.getRawType(), "term"));
		Assert.assertEquals(String.class, tt.getRawType());
		
	}

	@Test
	public void test_type_tracking__term2() {
		
		TrackedType tt = new TrackedType(QueryRoot.class);
		Assert.assertEquals(QueryRoot.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryStart.class, "term"));
		Assert.assertEquals(String.class, tt.getRawType());
		
	}

	@Test
	public void test_type_tracking__phrase() {
		
		TrackedType tt = new TrackedType(QueryRoot.class);
		Assert.assertEquals(QueryRoot.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryRoot.class, "startPhrase"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "term"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "term"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "endPhrase"));
		Assert.assertEquals(String.class, tt.getRawType());
		
	}

	@Test
	public void test_type_tracking__span() {
		
		TrackedType tt = new TrackedType(QueryRoot.class);
		Assert.assertEquals(QueryRoot.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryRoot.class, "startSpan"));
		Assert.assertEquals(SpanStart.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryStart.class, "term"));
		Assert.assertEquals(SpanNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(SpanNext.class, "next"));
		Assert.assertEquals(QueryStart.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryStart.class, "term"));
		Assert.assertEquals(SpanEnd.class, tt.getRawType());
		tt = tt.resolve(getMethodType(SpanEnd.class, "endSpan"));
		Assert.assertEquals(String.class, tt.getRawType());
		
	}

	@Test
	public void test_type_tracking__spanAndPharse() {
		
		TrackedType tt = new TrackedType(QueryRoot.class);
		Assert.assertEquals(QueryRoot.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryRoot.class, "startSpan"));
		Assert.assertEquals(SpanStart.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryStart.class, "term"));
		Assert.assertEquals(SpanNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(SpanNext.class, "next"));
		Assert.assertEquals(QueryStart.class, tt.getRawType());
		tt = tt.resolve(getMethodType(QueryStart.class, "startPhrase"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "term"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "term"));
		Assert.assertEquals(PhraseNext.class, tt.getRawType());
		tt = tt.resolve(getMethodType(PhraseNext.class, "endPhrase"));
		Assert.assertEquals(SpanEnd.class, tt.getRawType());
		tt = tt.resolve(getMethodType(SpanEnd.class, "endSpan"));
		Assert.assertEquals(String.class, tt.getRawType());
		
	}
	
	@SuppressWarnings("rawtypes")
	private Type getMethodType(Class type, String name) {
		for(Method m: type.getMethods()) {
			if (m.getName().equals(name)) {
				return m.getGenericReturnType();
			}
		}
		throw new IllegalArgumentException("method not found '" + name + "' at " + type.getName());
	}
	
	@Test
	public void fluent_term() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
					.term("XXX");
		
		Assert.assertEquals("XXX", result);		
	}

	@Test
	public void fluent_phrase() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
				.startPhrase()
					.term("x")
					.term("y")
					.term("z")
				.endPhrase();
		
		Assert.assertEquals("{ x, y, z /1 }", result);		
	}

	@Test
	public void fluent_phrase_nest() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
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
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
				.startSpan()
					.term("x")
					.next(2)
					.term("y")
				.endSpan();
		
		Assert.assertEquals("{ x next/2 y }", result);		
	}

	@Test
	public void fluent_snap_nest() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
		.startSpan()
			.term("x")
			.near(2)
			.startPhrase()
				.term("a")
				.term("b")
				.term("c")
				.slop(1)
			.endPhrase()
		.endSpan();
		
		Assert.assertEquals("{ x near/2 { a, b, c /1 } }", result);		
	}

	@Test
	public void fluent_boolean_nest() {
		
		String result = ((QueryStart<String>)FluentDSL.newNode(QueryRoot.class, new SingleQueryNode()))
		.startBoolean()
			.must().term("x")
			.must().term("y")
			.should().term("z")
			.should().startBoolean()
				.must().term("a")
				.must().term("b")
			.endBoolean()
			.minShouldCount(2)
		.endBoolean();

		Assert.assertEquals("{ must x must y should z should { must a must b /opt:1} /opt:2}", result);		
	}
}
