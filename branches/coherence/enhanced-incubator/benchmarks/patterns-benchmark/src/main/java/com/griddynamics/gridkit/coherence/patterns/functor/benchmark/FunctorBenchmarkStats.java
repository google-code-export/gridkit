package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

public class FunctorBenchmarkStats
{
	protected int membersCompleated;
	protected int membersFailed;
	protected int membersLeft;
	
	public FunctorBenchmarkStats()
	{
		membersCompleated = membersFailed = membersLeft = 0;
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
}
