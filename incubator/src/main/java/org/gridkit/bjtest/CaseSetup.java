package org.gridkit.bjtest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gridkit.bjtest.CaseRunner.TestBoundStatement;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class CaseSetup<T> implements TestRule {

	@SuppressWarnings("unchecked")
	public Statement apply(final Statement base, Description description) {
		final T test = ((TestBoundStatement<T>)base).getTestObject();
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setup(test);
				base.evaluate();
			}
		};
	}
	
	protected abstract void setup(T test);
	
	public static class Builder<T> {
		
		private Map<String, TestRule> cases = new LinkedHashMap<String, TestRule>();
		
		public Map<String, TestRule> getCases() {
			return cases;
		}
		
		public Builder<T> addCase(String name, CaseSetup<T> setup) {
			cases.put(name, setup);
			return this;
		}
	}
}
