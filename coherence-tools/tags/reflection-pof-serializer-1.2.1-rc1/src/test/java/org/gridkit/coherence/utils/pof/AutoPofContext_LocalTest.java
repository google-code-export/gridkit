/**
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

package org.gridkit.coherence.utils.pof;




import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

public class AutoPofContext_LocalTest extends AutoPofContext_FunctionalTest{

	private static NamedCache typeMap = new WrapperNamedCache(new LocalCache(), "");
	private static AutoPofSerializer ctx1 = new AutoPofSerializer("coherence-pof-config.xml", typeMap);
	private static AutoPofSerializer ctx2 = new AutoPofSerializer("coherence-pof-config.xml", typeMap);

	
	@Override
	public Object serDeser(Object value) {
		Binary bin = ExternalizableHelper.toBinary(value, ctx1);
		return ExternalizableHelper.fromBinary(bin, ctx2);
	}
}
