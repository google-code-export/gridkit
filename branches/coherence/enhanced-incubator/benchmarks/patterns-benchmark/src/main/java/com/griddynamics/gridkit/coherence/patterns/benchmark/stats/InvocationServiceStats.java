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
