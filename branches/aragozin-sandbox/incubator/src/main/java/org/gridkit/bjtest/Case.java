package org.gridkit.bjtest;

import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;

class Case {
	
	FrameworkMethod fm;
	String caseName;
	TestRule caseRule;
	
	public Case(FrameworkMethod fm) {
		this(fm, null, null);
	}

	public Case(FrameworkMethod fm, String caseName, TestRule caseRule) {
		this.fm = fm;
		this.caseName = caseName;
		this.caseRule = caseRule;
	}

	public FrameworkMethod getMethod() {
		return fm;
	}
	
	public String getCaseName() {
		return caseName;
	}
	
	public TestRule getCaseRule() {
		return caseRule;
	}

	public String getDescription() {
		if (caseName == null) {
			return fm.getName();
		}
		else {
			return fm.getName() + " | " + caseName;
		}
	}
}