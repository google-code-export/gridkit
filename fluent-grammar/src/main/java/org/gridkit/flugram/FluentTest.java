package org.gridkit.flugram;


import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.Test;

import junit.framework.Assert;


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
	
	public interface SpanStart<N> extends QueryStart<SpanNext<N>> {
		
	}
	
	public interface SpanNext<N> {
		
		public QueryStart<SpanEnd<N>> near(int distance);

		public QueryStart<SpanEnd<N>> next(int distance);
	}
	
	public interface SpanEnd<N> {

		public N endSpan();
		
	}
	
	public interface PhraseNext<N> extends QueryStart<PhraseNext<N>> {
		
		public N endPhrase();
		
		public PhraseNext<N> slop(int n);
		
	}
	
	public interface BooleanNext<N> {
		
		public N endBoolean();
		
		public QueryStart<BooleanNext<N>> must();

		public QueryStart<BooleanNext<N>> mustNot();
		
		public QueryStart<BooleanNext<N>> should();
		
		public BooleanNext<N> minShouldCount();
	}	

	
	@Test
	@SuppressWarnings("unused")
	public void inferReturnType_SpanStart() throws SecurityException, NoSuchMethodException {
		Method m = SpanStart.class.getMethod("startPhrase", null);
		
		Type rt = m.getGenericReturnType();
		ParameterizedType pt = (ParameterizedType) rt;
		Class rpt = (Class) pt.getRawType();
		for(Type i : rpt.getGenericInterfaces()) {			
			if (i instanceof ParameterizedType && FluentDSL.isAssignable(SyntScope.class, i)) {
				ParameterizedType ps = (ParameterizedType) i;
				Type argT = ps.getActualTypeArguments()[0];
				argT.toString();
			}
		}
		
//		Class<?> c = FluentDSL.inferReturnType(m);
//		Assert.assertEquals(SpanNext.class, c);
	}
	
}
