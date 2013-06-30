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
package org.gridkit.vicluster;

import java.util.Map;
import java.util.regex.Pattern;

import org.gridkit.bjtest.CaseRunner;
import org.gridkit.bjtest.CaseRunner.CasesProvider;
import org.gridkit.bjtest.CaseRunner.RunWithCases;
import org.gridkit.bjtest.CaseSetup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(CaseRunner.class)
public class GlobHelperTest2 {

	private static void addPattern(CaseSetup.Builder<GlobHelperTest2> builder, final String pattern, final String line, final boolean result) {
		builder.addCase("'" + pattern + "\' " + (result ? "matching" : "not matching") + " \'" + line + "\'", new CaseSetup<GlobHelperTest2>(){
			@Override
			protected void setup(GlobHelperTest2 test) {
				test.glob = pattern;
				test.line = line;
				test.match = result;
			}
		});
	}
	
	@CasesProvider("PATTERNS")
	public static Map<String, TestRule> getCases() {
		CaseSetup.Builder<GlobHelperTest2> builder = new CaseSetup.Builder<GlobHelperTest2>();
		
		addPattern(builder, "*", "x", true);
		addPattern(builder, "*", "x", true);
		addPattern(builder, "**", "x", true);
		addPattern(builder, "*", "x/y", false);
		addPattern(builder, "*", "x\\y", false);
		addPattern(builder, "**", "x/y", true);
		addPattern(builder, "**", "x\\y", true);
		addPattern(builder, "x/*/z", "x/y/z", true);
		addPattern(builder, "*/x/*/z", "x/y/z", false);
		addPattern(builder, "x/*/z/*", "x/y/z", false);
		addPattern(builder, "x/**/z", "x/y/z", true);
		addPattern(builder, "x/*/z", "x/y1/y2/z", false);
		addPattern(builder, "x/**/z", "x/y1/y2/z", true);
		addPattern(builder, "**/x/**/z", "x/y1/y2/z", true);
		addPattern(builder, "**/x/**/z", "1/x/y1/y2/z", true);
		addPattern(builder, "**/x/**/z", "1/2/x/y1/y2/z", true);
		addPattern(builder, "x/**/z/**", "x/y1/y2/z", true);
		addPattern(builder, "x/**/z/**", "x/y1/y2/z/1", true);
		addPattern(builder, "x/**/z/**", "x/y1/y2/z/1/2", true);
		addPattern(builder, "x/**/$/**", "x/y1/y2/$/1/2", true);
		addPattern(builder, "x/**/$$/**", "x/y1/y2/$$/1/2", true);
		return builder.getCases();
	}
	
	String glob;
	String line;
	boolean match;
	
	@Test
	public void simple_test() {
		
	}
	
	@Test
	@RunWithCases("PATTERNS")
	public void verify_match() {
		Pattern pattern = GlobHelper.translate(glob, "\\/");
		boolean matches = pattern.matcher(line).matches();
		Assert.assertTrue(glob + " match " + line + " -> " + match, matches == match);
	}
	
}
