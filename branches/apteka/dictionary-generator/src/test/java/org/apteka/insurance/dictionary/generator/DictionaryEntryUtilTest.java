package org.apteka.insurance.dictionary.generator;

import org.apteka.insurance.attribute.annotation.AttrToDict;
import org.junit.Test;

public class DictionaryEntryUtilTest {
	public static interface TestInterface {
		@AttrToDict
		boolean isBoolean();
		
		@AttrToDict
		boolean isPrimitiveBoolean();
		
		@AttrToDict
		int getPrimitiveInteger();
		
		@AttrToDict
		Integer getInteger();
	}
	
	@Test
	public void describeTest() {
		//System.out.println(DictionaryEntryUtil.toXML(DictionaryEntryUtil.describe(TestInterface.class, 0, "org.apteka.insurance.dictionary.generator").get(0)));
	}
}
