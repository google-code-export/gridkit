package org.gridkit.coherence.check;

import org.junit.Assert;
import org.junit.Test;

import com.tangosol.util.QueryHelper;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ChainedExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;

public class ExtractorMatchingTest {

	@Test
	public void verify_reflection_not_equals_to_chain() {
		
		ReflectionExtractor re = new ReflectionExtractor("getMyField");
		
		ChainedExtractor ce = new ChainedExtractor("getMyField");
		
		ValueExtractor e = QueryHelper.createExtractor("getMyField");
		
		Assert.assertTrue(re.equals(e));
		Assert.assertFalse(ce.equals(re));
	}
	
}
