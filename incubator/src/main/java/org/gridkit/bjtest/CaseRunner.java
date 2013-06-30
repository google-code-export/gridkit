/**
 * Copyright 2012 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.bjtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class CaseRunner extends ParentRunner<Case> {
	
	public interface TestBoundStatement<T> {
		
		public T getTestObject();
		
	}
	
	private static class TestBoundStatementWrapper<T> extends Statement implements TestBoundStatement<T> {
		
		private Object test;
		private Statement runnable;
		
		public TestBoundStatementWrapper(Statement runnable, Object test) {
			this.runnable = runnable;
			this.test = test;
		}

		public void evaluate() throws Throwable {
			runnable.evaluate();
		}
		
		@SuppressWarnings("unchecked")
		public T getTestObject() {
			return (T)test;
		}

		public String toString() {
			return runnable.toString();
		}
	}
	
	/**
	 * Annotation for a method which provides parameters to be injected into the
	 * test class constructor by <code>Parameterized</code>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface CasesProvider {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface RunWithCases {
		String value();
	}

	private static class BlockClassRunner extends BlockJUnit4ClassRunner {

		public BlockClassRunner(Class<?> klass) throws InitializationError {
			super(klass);
		}

		@Override
		protected List<FrameworkMethod> getChildren() {
			return super.getChildren();
		}

		@Override
		@SuppressWarnings("deprecation")
		protected Statement methodBlock(FrameworkMethod method) {
			Object test;
			try {
				test= new ReflectiveCallable() {
					@Override
					protected Object runReflectiveCall() throws Throwable {
						return createTest();
					}
				}.run();
			} catch (Throwable e) {
				return new Fail(e);
			}

			Statement statement= methodInvoker(method, test);
			statement= possiblyExpectingExceptions(method, test, statement);
			statement= withPotentialTimeout(method, test, statement);
			statement= withBefores(method, test, statement);
			statement= withAfters(method, test, statement);
			statement= withRules(method, test, statement);
			return new TestBoundStatementWrapper<Object>(statement, test);
		}
		
		private Statement withRules(FrameworkMethod method, Object target,
				Statement statement) {
			Statement result= statement;
			result= withMethodRules(method, target, result);
			result= withTestRules(method, target, result);
			return result;
		}

		@SuppressWarnings("deprecation")
		private Statement withMethodRules(FrameworkMethod method, Object target,
				Statement result) {
			List<TestRule> testRules= getTestRules(target);
			for (org.junit.rules.MethodRule each : getMethodRules(target))
				if (! testRules.contains(each))
					result= each.apply(result, method, target);
			return result;
		}

		@SuppressWarnings("deprecation")
		private List<org.junit.rules.MethodRule> getMethodRules(Object target) {
			return rules(target);
		}

		/**
		 * @param target
		 *            the test case instance
		 * @return a list of MethodRules that should be applied when executing this
		 *         test
		 * @deprecated {@link org.junit.rules.MethodRule} is a deprecated interface. Port to
		 *             {@link TestRule} and
		 *             {@link BlockJUnit4ClassRunner#getTestRules(Object)}
		 */
		@Deprecated
		protected List<org.junit.rules.MethodRule> rules(Object target) {
			return getTestClass().getAnnotatedFieldValues(target, Rule.class,
					org.junit.rules.MethodRule.class);
		}

		/**
		 * Returns a {@link Statement}: apply all non-static {@link Value} fields
		 * annotated with {@link Rule}.
		 *
		 * @param statement The base statement
		 * @return a RunRules statement if any class-level {@link Rule}s are
		 *         found, or the base statement
		 */
		private Statement withTestRules(FrameworkMethod method, Object target,
				Statement statement) {
			List<TestRule> testRules= getTestRules(target);
			return testRules.isEmpty() ? statement :
				new RunRules(statement, testRules, describeChild(method));
		}

		/**
		 * @param target
		 *            the test case instance
		 * @return a list of TestRules that should be applied when executing this
		 *         test
		 */
		protected List<TestRule> getTestRules(Object target) {
			return getTestClass().getAnnotatedFieldValues(target,
					Rule.class, TestRule.class);
		}
	}
	
	private TestClass testKlass;
	private BlockClassRunner vanilaRunner;
	
	private Map<String, CaseList> caseLists = new HashMap<String, CaseList>();
	private Map<String, Throwable> caseErrors = new HashMap<String, Throwable>();
	
	public CaseRunner(Class<?> klass) throws InitializationError {
		super(klass);
		testKlass = new TestClass(klass);
		vanilaRunner = new BlockClassRunner(klass);
	}

	private Map<String, TestRule> getCases(String caseSetName) {
		if (caseErrors.get(caseSetName) != null) {
			return null;
		}
		CaseList cl = caseLists.get(caseSetName); 
		
		if (cl == null) {
			for(FrameworkMethod fm : testKlass.getAnnotatedMethods(CasesProvider.class)) {
				String name = fm.getAnnotation(CasesProvider.class).value();
				if (caseSetName.equals(name)) {
					if (cl != null) {
						caseLists.remove(caseSetName);
						caseErrors.put(caseSetName, new RuntimeException("Ambigous cases provider for @CasesProvider(\"" + caseSetName + "\")"));
						return null;
					}
					cl = createCaseList(caseSetName, fm);
					if (cl == null) {
						break;
					}
					caseLists.put(caseSetName, cl);
				}
			}
		}
		if (cl == null) {
			if (caseErrors.get(caseSetName) == null) {
				caseErrors.put(caseSetName, new RuntimeException("No method annotated with @CasesProvider(\"" + caseSetName + "\") is found"));
			}
			return null;
		}
		return cl.rules;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CaseList createCaseList(String name, final FrameworkMethod fm) {
		try {
			Map<String, TestRule> rules = (Map)new ReflectiveCallable() {
				@Override
				protected Object runReflectiveCall() throws Throwable {
					return fm.invokeExplosively(null);
				}
			}.run();

			Map<String, TestRule> safeMap = new LinkedHashMap<String, TestRule>();
			for(String cn: rules.keySet()) {
				TestRule rule = (TestRule)rules.get(cn);
				if (rule == null) {
					throw new NullPointerException("No rule for case '" + cn + "'");
				}
				safeMap.put(cn, rule);
			}
			
			return new CaseList(safeMap);
		} catch (Throwable e) {
			caseErrors.put(name, e);
			return null;
		}
	}

	@Override
	public Description getDescription() {		
		Description root = Description.createSuiteDescription(testKlass.getName());
		for(FrameworkMethod fm: vanilaRunner.getChildren()) {
			if (fm.getAnnotation(RunWithCases.class) != null) {
				// if class name will be supplied Eclipse will display class instead of method
				Description testDescription = Description.createSuiteDescription(fm.getName());
				root.addChild(testDescription);
				String caseSetName = fm.getAnnotation(RunWithCases.class).value();
				Map<String, TestRule> cases = getCases(caseSetName);
				if (cases != null) {
					for(String caseName: cases.keySet()) {
						Description caseDescription = createCaseDescription(fm, caseName);
						testDescription.addChild(caseDescription);
					}
				}
			}
			else {
				root.addChild(Description.createTestDescription(testKlass.getJavaClass(), fm.getName()));
			}
		}
		return root;
	}

	@Override
	protected List<Case> getChildren() {
		List<Case> children = new ArrayList<Case>();
		
		for(FrameworkMethod fm: vanilaRunner.getChildren()) {
			if (fm.getAnnotation(RunWithCases.class) != null) {
				String caseSetName = fm.getAnnotation(RunWithCases.class).value();
				Map<String, TestRule> cases = getCases(caseSetName);
				if (cases != null) {
					for(String caseName: cases.keySet()) {
						Case c = new Case(fm, caseName, cases.get(caseName));
						children.add(c);
					}
				}
				else {
					Case c = new Case(fm);
					children.add(c);					
				}
			}
			else {
				Case c = new Case(fm);
				children.add(c);
			}
		}
		return children;
	}

	@Override
	protected Description describeChild(Case child) {
		return Description.createTestDescription(testKlass.getJavaClass(), child.getDescription());
	}

	@Override
	protected void runChild(Case child, RunNotifier notifier) {
		Description description= describeChild(child);
		if (child.getMethod().getAnnotation(Ignore.class) != null) {
			notifier.fireTestIgnored(description);
		} else {
			runLeaf(methodBlock(child), description, notifier);
		}
	}

	private Statement methodBlock(Case child) {
		if (child.caseRule == null && child.getMethod().getAnnotation(RunWithCases.class) !=null) {
			String caseSetName = child.getMethod().getAnnotation(RunWithCases.class).value();
			return new Fail(caseErrors.get(caseSetName));
			
		}
		else {
			Statement stm = vanilaRunner.methodBlock(child.getMethod());
			if (child.getCaseRule() != null) {
				stm = withCaseRule(stm, child);
			}
			return stm;
		}
	}

	private Statement withCaseRule(Statement stm, Case c) {
		Object test = ((TestBoundStatement<?>)stm).getTestObject();		
		return new TestBoundStatementWrapper<Object>(new RunRules(stm, Collections.singleton(c.getCaseRule()), describeChild(c)), test);
	}

	private Description createCaseDescription(FrameworkMethod fm, String caseName) {
		return Description.createTestDescription(testKlass.getJavaClass(), fm.getName() + " | " + caseName);
	}

	private static class CaseList {
		Map<String, TestRule> rules;

		public CaseList(Map<String, TestRule> rules) {
			super();
			this.rules = rules;
		}		
	}	
}
