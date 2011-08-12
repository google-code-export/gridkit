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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.reflect.PofNavigator;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.extractor.AbstractExtractor;
import com.tangosol.util.extractor.PofExtractor;

/**
 * @author Alexey Ragozin (alaexey.ragozin@gmail.com)
 */
public class ReflectionPofExtractor extends PofExtractor implements PortableObject {

    private static final long serialVersionUID = 20100713L;
    
    private String path = "";

    private transient Map<String, ReflectionHelper> reflCache = new HashMap<String, ReflectionHelper>();
    
    public ReflectionPofExtractor() {
        // deserialization constructor
    }
    
    public ReflectionPofExtractor(String path) {
        this(path, AbstractExtractor.VALUE);
    }

    public ReflectionPofExtractor(String path, int target) {
        this.path = path == null ? "" : path;
        this.m_nTarget = AbstractExtractor.VALUE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o == null) {
            return false;
        }
        else if (o.getClass() == this.getClass()) {
            ReflectionPofExtractor that = (ReflectionPofExtractor) o;
            return this.path.equals(that.path) && this.m_nTarget == that.m_nTarget;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return path.hashCode() ^ (31 * m_nTarget);
    }

    @Override
    public String toString() {
        String target = m_nTarget == KEY ? "KEY" 
                : m_nTarget == VALUE ? "VALUE"
                : "ORIGINAL_VALUE";
        return "ReflectionPofExtractor(" + target + ":" + path + ")";
    }

	@Override
	@SuppressWarnings("rawtypes")
    public Object extractFromEntry(Entry entry) {
        return extractInternal(entry, m_nTarget);
    }

    public Object extractOriginalFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
        return extractInternal(entry, -1);
    }
    
    @SuppressWarnings("rawtypes")
    private Object extractInternal(Entry entry, int target) {
        if (entry instanceof BinaryEntry) {
            BinaryEntry be = (BinaryEntry) entry;
            if (be.getSerializer() instanceof PofContext) {
                PofContext ctx = (PofContext) be.getSerializer();
                Binary bt;
                if (target == AbstractExtractor.KEY) {
                    bt = be.getBinaryKey(); 
                }
                else if (target == AbstractExtractor.VALUE) {
                    bt = be.getBinaryValue();
                }
                else {
                    //bt = be.getOriginalBinaryValue();
                	throw new IllegalArgumentException("unknown targte: " + target);
                }
                
                if (bt == null) {
                    return null;
                }                
                return extractFromBinary(ctx, bt);
            }
        }
        return extractPojo(entry, target);
    }

    public Object extractFromBinary(PofContext ctx, Binary bin) {
        PofValue root = PofValueParser.parse(bin, ctx);
        PofSerializer codec = ctx.getPofSerializer(root.getTypeId());
        if (codec instanceof ReflectionPofSerializer) {
            Object[] holder = new Object[1];
            String reminder;
            try {
                reminder = ((ReflectionPofSerializer)codec).extract(root, path, holder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return extractFinal(holder[0], reminder);
        }
        else {
            Object value = root.getValue();
            return extractFinal(value, path);
        }
    }

    @Override
    public Object extract(Object target) {
        return extractFinal(target, path);
    }

    private Object extractFinal(Object value, String path) {
        if (path == null || path.length() == 0) {
            return value;
        }
        else {
            ReflectionHelper helper = reflCache.get(path);
            if (helper == null) {
                helper = new ReflectionHelper(path);
                reflCache.put(path, helper);
            }
            return helper.extract(value);
        }
    }

    @SuppressWarnings("rawtypes")
	private Object extractPojo(Entry entry, int target) {
        Object ot;
        if (target == AbstractExtractor.KEY) {
            ot = entry.getKey();
        }
        else if (target == AbstractExtractor.VALUE) {
            ot = entry.getValue();
        }
        else {
            ot = ((MapTrigger.Entry)entry).getOriginalValue();
        }
        
        return extract(ot);
    }

    @Override
    public PofNavigator getNavigator() {
        // TODO Auto-generated method stub
        return super.getNavigator();
    }

    @Override
    public void readExternal(PofReader in) throws IOException {
        this.path = in.readString(0);        
        this.m_nTarget = in.readInt(1);        
    }

    @Override
    public void writeExternal(PofWriter out) throws IOException {
        out.writeString(0, this.path);
        out.writeInt(1, m_nTarget);
    }
}
