/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.search.lucene;

import java.io.Serializable;

import org.junit.Ignore;

@Ignore
@SuppressWarnings("serial")
public class TestDoc implements Serializable {

	private String text;

	public TestDoc(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public String toString() {
		return text;
	}
}
