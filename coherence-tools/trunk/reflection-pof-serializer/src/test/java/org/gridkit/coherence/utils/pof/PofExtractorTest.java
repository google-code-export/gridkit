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

package org.gridkit.coherence.utils.pof;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.utils.pof.ReflectionHelper;
import org.gridkit.coherence.utils.pof.ReflectionPofExtractor;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.reflect.ComplexPofValue;
import com.tangosol.io.pof.reflect.PofArray;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.extractor.ReflectionExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PofExtractorTest {

    @Test
    public void testTypeA() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        Binary blob = ExternalizableHelper.toBinary(new TypeA(), ctx);
        PofValue root = PofValueParser.parse(blob, ctx);
    
        Assert.assertTrue(root instanceof ComplexPofValue);
        
        ComplexPofValue cf = (ComplexPofValue) root;
        @SuppressWarnings("unused")
		PofValue p0 = cf.getChild(0);
        PofValue p1 = cf.getChild(1);
        PofValue p2 = cf.getChild(2);
        PofValue p3 = cf.getChild(3);
        PofValue p4 = cf.getChild(4);
        PofValue p5 = cf.getChild(5);
        PofValue p6 = cf.getChild(6);
        @SuppressWarnings("unused")
        PofValue p7 = cf.getChild(6);
        
        Assert.assertEquals("ABC", p1.getValue(String.class));
        Assert.assertEquals("", p2.getValue(String.class));
        Assert.assertEquals(null, p3.getValue(String.class));
        Assert.assertEquals(10, p4.getValue(Integer.class));
        Assert.assertEquals(Arrays.toString(new boolean[]{true, true, false, false}), Arrays.toString((boolean[])p5.getValue(boolean[].class)));
        
        Object[] array = (Object[]) p6.getValue(ReflectionExtractor[].class);
        ReflectionExtractor e = (ReflectionExtractor) ((PofArray)p6).getChild(0).getValue();
        
        Assert.assertEquals("A", ((ReflectionExtractor)array[0]).getMethodName());
        Assert.assertEquals("A", e.getMethodName());
        
    }
    
    @Test
    public void helperTest() {
        
        ReflectionHelper helper = new ReflectionHelper("count");
        
        Assert.assertEquals(3, helper.extract("ABC"));
        
        @SuppressWarnings("unused")
        class TT {
			String a = "ABC";
            String[] b = {"A", "B", "C"};
        }
        
        TT t = new TT();
        
        helper = new ReflectionHelper("a");
        Assert.assertEquals("ABC", helper.extract(t));
        helper = new ReflectionHelper("a.count");
        Assert.assertEquals(3, helper.extract(t));
        helper = new ReflectionHelper("b.length");
        Assert.assertEquals(3, helper.extract(t));
    }
    
    @Test
    public void testTypeB_String() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = "ABC";
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter1 = new ReflectionPofExtractor("string");
        Assert.assertEquals("ABC", extracter1.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_String_length() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = "ABC";
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("string.count");
        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_String_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = null;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("string");
        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_TU() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.tu = TimeUnit.MILLISECONDS;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("tu");
        
        Assert.assertEquals(TimeUnit.MILLISECONDS, extracter.extractFromBinary(ctx, blob));        
    }
    
    @Test
    public void testTypeB_TU_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.tu = null;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("tu");
        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_StringArray() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.stringArray = new String[]{"A","B","C"};
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("stringArray");        
        Assert.assertArrayEquals(new String[]{"A","B","C"}, (String[])extracter.extractFromBinary(ctx, blob));        

        extracter = new ReflectionPofExtractor("stringArray.length");        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_StringArray_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.stringArray = null;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("stringArray");
        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_IntArray() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        int[] array = {1, 2, 3};
        TypeB b = new TypeB();
        b.intArray = array;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("intArray");        
        Assert.assertArrayEquals(array, (int[])extracter.extractFromBinary(ctx, blob));        

        extracter = new ReflectionPofExtractor("intArray.length");        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeB_IntArray_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.intArray = null;
        
        Binary blob = ExternalizableHelper.toBinary(b, ctx);
        
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("intArray");
        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_String() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = "ABC";
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};
        
        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.string");
        Assert.assertEquals("ABC", extracter.extractFromBinary(ctx, blob));        
        extracter = new ReflectionPofExtractor("barray.length");
        Assert.assertEquals(2, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_String_length() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = "ABC";
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};
        
        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.string.count");        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_String_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.string = null;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.string");        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_TU() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.tu = TimeUnit.MILLISECONDS;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        
        extracter = new ReflectionPofExtractor("b.tu");        
        Assert.assertEquals(TimeUnit.MILLISECONDS, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_TU_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.tu = null;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};
        
        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        
        extracter = new ReflectionPofExtractor("b.tu");        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_StringArray() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.stringArray = new String[]{"A","B","C"};
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.stringArray");        
        Assert.assertArrayEquals(new String[]{"A","B","C"}, (String[])extracter.extractFromBinary(ctx, blob));        
    
        extracter = new ReflectionPofExtractor("b.stringArray.length");        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_StringArray_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.stringArray = null;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        
        extracter = new ReflectionPofExtractor("b.stringArray");        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_IntArray() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        int[] array = {1, 2, 3};
        TypeB b = new TypeB();
        b.intArray = array;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.intArray");        
        Assert.assertArrayEquals(array, (int[])extracter.extractFromBinary(ctx, blob));        
    
        extracter = new ReflectionPofExtractor("b.intArray.length");        
        Assert.assertEquals(3, extracter.extractFromBinary(ctx, blob));        
    }

    @Test
    public void testTypeC_IntArray_null() {
        ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
        
        TypeB b = new TypeB();
        b.intArray = null;
        TypeC c = new TypeC();
        c.b = b;
        c.barray = new TypeB[]{b, b};

        Binary blob = ExternalizableHelper.toBinary(c, ctx);
        
        ReflectionPofExtractor extracter;
        extracter = new ReflectionPofExtractor("b.intArray");
        
        Assert.assertEquals(null, extracter.extractFromBinary(ctx, blob));        
    }

    public static class TypeA implements PortableObject {

        @Override
        public void readExternal(PofReader in) throws IOException {
        }

        @Override
        public void writeExternal(PofWriter out) throws IOException {
            out.writeString(1, "ABC");
            out.writeString(2, "");
            out.writeString(3, null);
            out.writeInt(4, 10);
            out.writeBooleanArray(5, new boolean[]{true, true, false, false});
            out.writeObjectArray(6, new ReflectionExtractor[]{new ReflectionExtractor("A")});
        }
    }
    
    public static class TypeB {
        
        public int integer = 3;
        public Integer intObj;
        public String string; 
        public TimeUnit tu;
        public String[] stringArray;
        public int[] intArray;
        public boolean bool;
        
        public TypeB() {};
    }
    
    public static class TypeC {
        
        public TypeB b;
        public TypeB[] barray;
        
        public TypeC() {};        
    }
}
