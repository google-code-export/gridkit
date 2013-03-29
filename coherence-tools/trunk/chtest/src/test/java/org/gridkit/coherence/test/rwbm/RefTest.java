/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.test.rwbm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

public class RefTest {

	/* below context of ref-object-pof-config.xml
 
<pof-config>

    <enable-references>true</enable-references>

    <user-type-list>

        <include>coherence-pof-config.xml</include>
        <user-type>
            <type-id>1001</type-id>
            <class-name>org.gridkit.coherence.test.rwbm.RefTest$Test</class-name>
            <serializer>
                <class-name>org.gridkit.coherence.test.rwbm.RefTest$TestSerializer</class-name>
            </serializer>
        </user-type>
    </user-type-list>
</pof-config>	 
	 
	 */
	
	
	@org.junit.Test
	public void code_decode_fail() {
		ConfigurablePofContext ctx = new ConfigurablePofContext("ref-object-pof-config.xml");
		Test test = new Test();
		test.setId("123");
		
		Binary bin = ExternalizableHelper.toBinary(Arrays.asList(test, test), ctx);
		@SuppressWarnings("unchecked")
		List<Test> dtest = (List<Test>) ExternalizableHelper.fromBinary(bin, ctx);
		
		Assert.assertEquals("123", dtest.get(0).getId());
		Assert.assertEquals("123", dtest.get(1).getId());
		Assert.assertSame(dtest.get(0),dtest.get(1));
	}

	@org.junit.Test
	public void code_decode_ok() {
		ConfigurablePofContext ctx = new ConfigurablePofContext("ref-object-pof-config.xml");
		Test test = new Test();
		test.setId("123");
		
		// force xml to get loaded
		ctx.getConfig();
		
		Binary bin = ExternalizableHelper.toBinary(Arrays.asList(test, test), ctx);
		@SuppressWarnings("unchecked")
		List<Test> dtest = (List<Test>) ExternalizableHelper.fromBinary(bin, ctx);
		
		Assert.assertEquals("123", dtest.get(0).getId());
		Assert.assertEquals("123", dtest.get(1).getId());
		Assert.assertSame(dtest.get(0),dtest.get(1));
	}
	
	static public class Test {
	    protected String id;

	    public Test() {
	    }

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }
	}
	
	
	static public class TestSerializer implements PofSerializer {
	    @Override
	    public void serialize(PofWriter pw, Object obj) throws IOException {
	        System.out.println("Serializing " + obj.getClass().getSimpleName());
	        Test value = (Test) obj;
	        pw.writeString(0, value.getId());
	        pw.writeRemainder(null);
	    }

	    @Override
	    public Object deserialize(PofReader pr) throws IOException {
	        Test value = new Test();
	        pr.registerIdentity(value);
	        value.setId(pr.readString(0));
	        pr.readRemainder();
	        return value;
	    }
	}	
}
