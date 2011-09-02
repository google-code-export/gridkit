package org.gridkit.search.gemfire;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Collections;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LuceneIndexDiscoveryTask implements Function {

	private static final long serialVersionUID = 20110901L;

	static DistributedMember execute(String regionName) throws InterruptedException {
		DistributedSystem ds = CacheFactory.getAnyInstance().getDistributedSystem();
		DM dm = ((InternalDistributedSystem)ds).getDistributionManager();
		Set<DistributedMember> members = new HashSet<DistributedMember>(dm.getAllOtherMembers());
		members.add(ds.getDistributedMember());
		for(DistributedMember member: members) {
			ResultCollector<?, ?> collector = FunctionService.onMembers(ds)
				.withArgs(regionName).execute(new LuceneIndexDiscoveryTask());
			List<?> results = (List<?>) collector.getResult(5, TimeUnit.SECONDS);
			results.removeAll(Collections.singleton(null));
			if (results.isEmpty()) {
				continue;
			}
			else {
				return (DistributedMember)results.get(0);
			}
		}
		return null;
	}
	
	@Override
	public void execute(FunctionContext context) {
		String regionName = (String) context.getArguments();
		
		if (LuceneIndexManager.getInstance().getSearcherForRegion(regionName) != null) {
			DistributedMember localMember = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember();
			context.getResultSender().lastResult((Serializable) localMember);
		}
		else {
			context.getResultSender().lastResult(null);
		}		
	}

	@Override
	public String getId() {
		return this.getClass().getName();
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return false;
	}

	@Override
	public boolean optimizeForWrite() {
		return false;
	}
}
