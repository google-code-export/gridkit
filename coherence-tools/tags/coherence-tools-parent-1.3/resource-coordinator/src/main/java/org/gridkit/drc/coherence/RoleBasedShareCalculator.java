/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.drc.coherence;

import java.util.Set;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

/**
 * Oracle Coherence role-based fair share
 * 
 * @author malekseev
 * 20.04.2011
 */
public class RoleBasedShareCalculator implements ShareCalculator {
	
	@Override
	public int getShare(int sourcesSize) {
		Cluster cluster = CacheFactory.ensureCluster();
		String localRoleName = cluster.getLocalMember().getRoleName();
		
		@SuppressWarnings("unchecked")
		Set<Member> memberSet = cluster.getMemberSet();
		int sameMemberCount = 0;
		
		for (Member member : memberSet) {
			if (member.getRoleName().equals(localRoleName)) {
				sameMemberCount++;
			}
		}

		return (int) Math.ceil(((double) sourcesSize) / sameMemberCount);
	}	
}
