/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.benchmark.stats;

public class InvocationServiceStats<T> extends TimeDependStats<T>
{
	protected int membersCompleated;
	protected int membersFailed;
	protected int membersLeft;
	
	protected int executionMarksProcessed;
	
	public InvocationServiceStats()
	{
		super();
		membersCompleated = membersFailed = membersLeft = executionMarksProcessed = 0;
	}
	
	public int getMembersCompleated()
	{
		return membersCompleated;
	}
	
	public void setMembersCompleated(int membersCompleated)
	{
		this.membersCompleated = membersCompleated;
	}
	
	public int getMembersFailed()
	{
		return membersFailed;
	}
	
	public void setMembersFailed(int membersFailed)
	{
		this.membersFailed = membersFailed;
	}
	
	public int getMembersLeft()
	{
		return membersLeft;
	}
	
	public void setMembersLeft(int membersLeft)
	{
		this.membersLeft = membersLeft;
	}

	public int getExecutionMarksProcessed()
	{
		return executionMarksProcessed;
	}

	public void setExecutionMarksProcessed(int executionMarksProcessed)
	{
		this.executionMarksProcessed = executionMarksProcessed;
	}

	@Override
	public String toString()
	{
		return "InvocationServiceStats [executionMarksProcessed="
				+ executionMarksProcessed + ", membersCompleated="
				+ membersCompleated + ", membersFailed=" + membersFailed
				+ ", membersLeft=" + membersLeft + "]";
	}
}
