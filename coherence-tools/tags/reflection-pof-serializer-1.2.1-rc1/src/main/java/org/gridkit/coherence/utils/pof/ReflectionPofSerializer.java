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
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tangosol.io.pof.PofHelper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.reflect.ComplexPofValue;
import com.tangosol.io.pof.reflect.PofArray;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.util.ImmutableArrayList;

/**
 * An implementation of generic {@link PofSerializer} capable to serialize any object using
 * reflection.
 * 
 * Important: serializable classes still should be registered in pof-config.xml.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ReflectionPofSerializer implements PofSerializer {
    
    private final static Map<Class<?>, ObjectPropCodec> CODEC = new HashMap<Class<?>, ObjectPropCodec>();
    {
        CODEC.put(Object.class, new ObjectObjectPropCodec());
        
        CODEC.put(String.class, new StringPropCodec());

        CODEC.put(byte.class, new BytePropCodec());
        CODEC.put(Byte.class, new BytePropCodec());
        CODEC.put(short.class, new ShortPropCodec());
        CODEC.put(Short.class, new ShortPropCodec());
        CODEC.put(char.class, new CharPropCodec());
        CODEC.put(Character.class, new CharPropCodec());
        CODEC.put(int.class, new IntegerPropCodec());
        CODEC.put(Integer.class, new IntegerPropCodec());
        CODEC.put(long.class, new LongPropCodec());
        CODEC.put(Long.class, new LongPropCodec());
        
        CODEC.put(byte[].class, new ByteArrayPropCodec());
    }

    /**
     * All custom codecs should be setup before first usage of serializare.
     */
    public static void registerCodec(Class<?> type, ObjectPropCodec codec) {
        CODEC.put(type, codec);
        // TODO codec override exception
    }
    
    private final static ConcurrentMap<String, Object> WELL_KNOWN_OBJECTS = new ConcurrentHashMap<String, Object>();  
    private final static ConcurrentMap<Object, String> WELL_KNOWN_OBJECTS_REV = new ConcurrentHashMap<Object, String>();
    
    /**
     * If you have singleton object in your application. This works similar to readReslve or enumeration serialization logic.
     * @param name unique name of singleton object
     * @param object instance of singleton object
     */
    public static void registerWellKnownObject(String name, Object object) {
        if (WELL_KNOWN_OBJECTS.putIfAbsent(name, object) == null) {
            WELL_KNOWN_OBJECTS_REV.put(object, name);
        }
        else {
            throw new IllegalStateException("Name '" + name + "' is already in use");
        }
    }
    
    private ConcurrentMap<Class<?>, PofSerializer> formats = new ConcurrentHashMap<Class<?>, PofSerializer>();
    
    
    @Override
    public Object deserialize(PofReader in) throws IOException {
        Object result = internalDeserialize(in);
        return result;
    }

    protected Object internalDeserialize(PofReader in) throws IOException {
    	PofSerializer format = null;
    	try {
	        Class<?> type = in.getPofContext().getClass(in.getUserTypeId());
			format = getClassCodec(type);
	        Object result = resolve(format.deserialize(in));
	        return result;
    	}
    	catch(IOException e) {
    		throw new IOException("Deserialization failed, format " + format, e);
    	}
    }

    @Override
    public void serialize(PofWriter out, Object origValue) throws IOException {
        internalSerialize(out, origValue);
    }

    protected void internalSerialize(PofWriter out, Object origValue) throws IOException {
        Object value = replace(origValue);
        Class<?> type = value.getClass();
        PofSerializer format = getClassCodec(type);
        format.serialize(out, value);
    }

    /**
     * Exposed to be used in AutoPofSerializer.
     */
    public PofSerializer getClassCodec(Class<?> type) throws IOException {
        PofSerializer format = formats.get(type);
        if (format == null) {
            try {
                format = createSerializer(type);
            } catch (Exception e) {
                throw new IOException("Failed to create reflection format for " + type.getName(), e);
            }
            formats.putIfAbsent(type, format);
            format = formats.get(type);
        }
        return format;
    }

	private PofSerializer createSerializer(Class<?> type) throws NoSuchMethodException {
		if (Enum.class.isAssignableFrom(type)) {
			return new EnumSerializer(type);
		}
		else if (isCollectionClass(type)) {
			return new CollectionSerializer(type);
		}
		else if (isMapClass(type)) {
			return new MapSerializer(type);
		}
		else {
			return new ObjectFormat(type);
		}
	}

    private boolean isCollectionClass(Class<?> type) {
		if (!Collection.class.isAssignableFrom(type)) {
			return false;
		}
		for(Constructor<?> c: type.getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				return true;
			}
			else if (c.getParameterTypes().length == 1 
					&& (   c.getParameterTypes()[0].isAssignableFrom(Collection.class) 
						|| c.getParameterTypes()[0].isAssignableFrom(Set.class)
						|| c.getParameterTypes()[0].isAssignableFrom(List.class) ) ) {
				return true;
			}
			else if (c.getParameterTypes().length == 1
					&& c.getParameterTypes()[0].isArray()
					&& !c.getParameterTypes()[0].getComponentType().isPrimitive()) {
				return true;
			}
		}
		return false;
	}

    private boolean isMapClass(Class<?> type) {
    	if (!Map.class.isAssignableFrom(type)) {
    		return false;
    	}
    	for(Constructor<?> c: type.getDeclaredConstructors()) {
    		if (c.getParameterTypes().length == 0) {
    			return true;
    		}
    		else if (c.getParameterTypes().length == 1
    				&& c.getParameterTypes()[0].isAssignableFrom(Map.class)) {
    			return true;
    		}
    	}
    	return false;
    }

	private static Object resolve(Object deserialized) {
        if (deserialized.getClass() == WKO.class) {
            return WELL_KNOWN_OBJECTS.get(((WKO)deserialized).objectRef);
        }
        else {
            return deserialized;
        }
    }

    private static Object replace(Object value) {
        String ref = WELL_KNOWN_OBJECTS_REV.get(value);
        if (ref != null) {
            return new WKO(ref);
        }
        else {
            return value;
        }
    }

    private static ObjectPropCodec getCodec(Field field) {
        ObjectPropCodec codec = CODEC.get(Object.class);
        if (Object[].class.isAssignableFrom(field.getType())) {
            codec = new ObjectArrayPropCodec((Object[])Array.newInstance(field.getType().getComponentType(), 0));
        }
        else if (field.getType().isEnum()) {
            codec = new EnumPropCodec(field.getType());
        }
        else {
            codec = CODEC.get(field.getType());
        }
        if (codec == null) {
            codec = CODEC.get(Object.class);
        }
        return codec;
    }

	private static class CollectionSerializer implements PofSerializer {
	
		private final Class<?> type;
		private final Constructor<?> constructor;
		private final ObjectFormat format;
		private final Object[] proto;
		private final boolean listProto;
		
		public CollectionSerializer(Class<?> type)	throws SecurityException, NoSuchMethodException {
			this.type = type;
			this.constructor = initConstructor(type);
			if (constructor.getParameterTypes().length == 0) {
				ObjectFormat of = new ObjectFormat(type);
				format = of;
			}
			else {
				format = null;
			}
			if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isArray()) {
				proto = (Object[]) Array.newInstance(constructor.getParameterTypes()[0].getComponentType(), 0);
			}
			else {
				proto = null;
			}
			if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(List.class)) {
				listProto = true;
			}
			else {
				listProto = false;
			}
		}

		private Constructor<?> initConstructor(Class<?> type) {
			Constructor<?> cc = null;
			for(Constructor<?> c : type.getDeclaredConstructors()) {
				if (c.getParameterTypes().length == 0) {
					cc = c;
					break;
				}
				if (c.getParameterTypes().length == 1 
						&& (   c.getParameterTypes()[0].isAssignableFrom(Collection.class) 
							|| c.getParameterTypes()[0].isAssignableFrom(Set.class)
							|| c.getParameterTypes()[0].isAssignableFrom(List.class) ) ) {
					cc = c;
				}
				if (c.getParameterTypes().length == 1 
						&& c.getParameterTypes()[0].isArray() && !c.getParameterTypes()[0].getComponentType().isPrimitive() ) {
					if (cc == null) {
						cc = c;
					}
				}
			}
			cc.setAccessible(true);
			return cc;
		}
		
		@Override
		public void serialize(PofWriter writer, Object object)	 throws IOException {
			Collection<?> col = (Collection<?>) object;
			if (format != null) {
				PofWriter header = writer.createNestedPofWriter(0);
				format.serialize(header, object);
			}
			writer.writeCollection(1, col);
			writer.writeRemainder(null);			
		}

		@Override
		public Object deserialize(PofReader reader) throws IOException {
			try {
				if (proto != null) {
					Object[] objects = reader.readObjectArray(1, proto);
					reader.readRemainder();
					return constructor.newInstance((Object)objects);
				}
				else if (format != null) {
					PofReader header = reader.createNestedPofReader(0);
					Collection<?> col = (Collection<?>) format.deserialize(header);
					//this is workaround for incorrect transient markers usage in ArrayList
					col.clear();
					reader.readCollection(1, col);
					reader.readRemainder();
					return col;
				}
				else {
					ImmutableArrayList data = new ImmutableArrayList(reader.readObjectArray(1, null));
					Collection<?> col = listProto ? data.getList() : data.getSet();
					reader.readRemainder();
					return constructor.newInstance(col);
				}
			} catch (Exception e) {
				throw new IOException("Failed to deserialize " + type.getName() + " instance", e);
			}
		}

		@Override
		public String toString() {
			return "CollectionFormat(" + constructor.toGenericString() +")";
		}
	}

    private static class MapSerializer implements PofSerializer {
	
		private final Class<?> type;
		private final Constructor<?> constructor;
		private final ObjectFormat format;
		
		public MapSerializer(Class<?> type)	throws SecurityException, NoSuchMethodException {
			this.type = type;
			this.constructor = initConstructor(type);
			if (constructor.getParameterTypes().length == 0) {
				ObjectFormat of = new ObjectFormat(type);
				format = of;
			}
			else {
				format = null;
			}
		}

		private Constructor<?> initConstructor(Class<?> type) {
			Constructor<?> cc = null;
			for(Constructor<?> c : type.getDeclaredConstructors()) {
				if (c.getParameterTypes().length == 0) {
					cc = c;
					break;
				}
				if (c.getParameterTypes().length == 1 
						&& c.getParameterTypes()[0].isAssignableFrom(Map.class)) {
					if (cc == null) {
						cc = c;
					}
				}
			}
			cc.setAccessible(true);
			return cc;
		}

		@Override
		public void serialize(PofWriter writer, Object object)	 throws IOException {
			Map<?, ?> map = (Map<?, ?>) object;
			if (format != null) {
				PofWriter header = writer.createNestedPofWriter(0);
				format.serialize(header, object);
			}
			writer.writeMap(1, map);
			writer.writeRemainder(null);			
		}

		@Override
		public Object deserialize(PofReader reader) throws IOException {
			try {
				Map<?, ?> proto;
				if (format != null) {
					PofReader header = reader.createNestedPofReader(0);
					proto = (Map<?, ?>) format.deserialize(header);
					proto.clear();
				}
				else {
					proto = null;
				}
				Map<?, ?> map = reader.readMap(1, proto);
				if (constructor.getParameterTypes().length != 0) {
					map = (Map<?, ?>) constructor.newInstance(map);
				}
				return map;
			} catch (Exception e) {
				throw new IOException("Failed to deserialize " + type.getName() + " instance", e);
			}
		}
		
		@Override
		public String toString() {
			return "MapFormat(" + constructor.toGenericString() +")";
		}
	}

    private static class EnumSerializer implements PofSerializer {
    	
    	private final Class<?> enumType;
        @SuppressWarnings("rawtypes")
		private final Enum[] universe;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public EnumSerializer(Class<?> type) {
            this.enumType = type;
        	this.universe = (Enum[]) EnumSet.allOf((Class)type).toArray(new Enum[0]);
        }

        @Override
		public Object deserialize(PofReader reader) throws IOException {
            int val = reader.readShort(1);
            reader.readRemainder();
            return universe[val];
		}

		@Override
		public void serialize(PofWriter out, Object obj) throws IOException {
			Enum<?> e = (Enum<?>)enumType.cast(obj);
			out.writeInt(1, e.ordinal());
			out.writeRemainder(null);
		}
    }
    
	private static class ObjectFormat implements PofSerializer {
        private Constructor<?> constructor;
        private ObjectFieldCodec[] propCodec;
        
        public ObjectFormat(Class<?> type) throws SecurityException, NoSuchMethodException {
            List<ObjectFieldCodec> list = new ArrayList<ObjectFieldCodec>();
            
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);            
            init(list, type);            
            propCodec = list.toArray(new ObjectFieldCodec[list.size()]);            
        }

        private void init(List<ObjectFieldCodec> list, Class<?> type) {
            Class<?> parent = type.getSuperclass();
            if (parent != Object.class) {
                init(list, parent);
            }
            
            Field[] fields = type.getDeclaredFields();
            for(int i = 0; i != fields.length; ++i) {
                Field field = fields[i];
                int mod = field.getModifiers();
                if (       !Modifier.isStatic(mod)
                        && !Modifier.isTransient(mod)) {

                    field.setAccessible(true);
                    ObjectPropCodec codec = getCodec(field);
                    ObjectFieldCodec fc = new ObjectFieldCodec(field, codec, Modifier.isTransient(mod));
                    list.add(fc);
                }
            }            
        }

        @Override
        public Object deserialize(PofReader in) throws IOException {
            Object val;
            try {
                val = constructor.newInstance();
            } catch (Exception e) {
                throw new IOException("Cannot init new object", e);
            };
            int propId = 0;
            boolean[] nulls = in.readBooleanArray(propId++);
            
            for(int i = 0; i != propCodec.length; ++i) {
                if (!nulls[i]) {
                    ObjectFieldCodec codec = propCodec[i];
                    try {
	                    if (codec.noOverride && (codec.field.get(val) != null)) {
	                    	// final field has been already initialized in constructor
	                    	continue;
	                    }
                        codec.field.set(val, codec.codec.readProp(in, propId++, codec.field));
                    } catch (Exception e) {
                        throw new IOException("Deserialization failed (" + e.getMessage() + ")", e);
                    }
                }
            }
            in.readRemainder();
            return val;
        }

        @Override
        public void serialize(PofWriter out, Object val) throws IOException {
            try {                    
                Object[] snapshot = new Object[propCodec.length];
                boolean[] nulls = new boolean[propCodec.length];
                for(int i = 0; i != propCodec.length; ++i) {
                    Object fv = propCodec[i].field.get(val);
                    snapshot[i] = fv;
                    nulls[i] = fv == null;
                }
                int propId = 0;
                out.writeBooleanArray(propId++, nulls);
                for(int i = 0; i != propCodec.length; ++i) {
                    if (snapshot[i] != null) {
                        propCodec[i].codec.writeProp(out, propId++, snapshot[i], propCodec[i].field);
                    }
                }
                
                out.writeRemainder(null);
            } catch (Exception e) {
                throw new IOException("Serialization failed (" + e.getMessage() + ")", e);
            }
        }

        public String extract(ComplexPofValue cursor, String path, Object[] value) throws IOException {            
            if (constructor.getDeclaringClass() == WKO.class) {
                WKO ref = (WKO) cursor.getValue();
                value[0] = resolve(ref);
                return path;
            }
            else {
            
                String pHead = ReflectionHelper.getHead(path);
                String pTail = ReflectionHelper.getTail(path);
                
                if (pHead == null || pHead.length() == 0) {
                    value[0] = cursor.getValue(constructor.getDeclaringClass());
                    return pTail;
                }
                else {
                    int propId = 0;
                    boolean[] nulls = (boolean[]) cursor.getChild(propId++).getValue(boolean[].class);
                    
                    for(int i = 0; i != propCodec.length; ++i) {
                        ObjectFieldCodec codec = propCodec[i];
                        if (codec.field.getName().equals(pHead)) {
                            if (nulls[i]) {
                                value[0] = null;
                                return pTail;
                            }
                            else {
                                return codec.codec.extract(cursor.getChild(propId), pTail, value);
                            }
                        }
                        if (!nulls[i]) {
                            ++propId;
                        }
                    }
                    throw new IOException("Cannot extract field [" + pHead + "] from class " + constructor.getDeclaringClass().getName());
                }
            }
        }

		@Override
		public String toString() {
			return "ObjectFormat(" + constructor.getDeclaringClass().toString() + ")";
		}
    }
    
    private static class  ObjectFieldCodec {
        final Field field;
        final ObjectPropCodec codec;
        final boolean noOverride;
        
        public ObjectFieldCodec(Field field, ObjectPropCodec codec, boolean noOverride) {
            this.field = field;
            this.codec = codec;
            this.noOverride = noOverride;
        }        
    }

    public static interface ObjectPropCodec {
        public Object readProp(PofReader reader, int propId, Field field) throws IOException;
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException;
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException;
    }
    
    private static class ObjectArrayPropCodec implements ObjectPropCodec {

        private final Object[] proto;
        
        public ObjectArrayPropCodec(Object[] proto) {
            this.proto = proto;
        }

        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readObjectArray(propId, proto);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeObjectArray(propId, (Object[]) obj);
        }

        @Override
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            String pHead = ReflectionHelper.getHead(path);
            if ("length".equals(pHead)) {
                path = ReflectionHelper.getTail(path);
                PofArray pa = (PofArray) cursor;
                valueOut[0] = pa.getLength();
                return path;
            }
            else {
                // TODO element access
                Object[] array = (Object[]) cursor.getValue(PofHelper.OBJECT_ARRAY_EMPTY.getClass());
                Object result = Arrays.copyOf(proto, array.length);
                System.arraycopy(array, 0, result, 0, array.length);
                valueOut[0] = result;
                return path;
            }
        }
    }

    private static class EnumPropCodec implements ObjectPropCodec {
        
        @SuppressWarnings("rawtypes")
		private final Enum[] universe;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public EnumPropCodec(Class<?> type) {
            this.universe = (Enum[]) EnumSet.allOf((Class)type).toArray(new Enum[0]);
        }

        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            int val = reader.readShort(propId);
            return universe[val];
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeShort(propId, (short) ((Enum<?>)obj).ordinal());
        }
        
        @Override
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            int val = (Integer) cursor.getValue(Integer.class);
            valueOut[0] = universe[val];
            return path;
        }
    }

    private static class ObjectObjectPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return resolve(reader.readObject(propId));
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeObject(propId, replace(obj));
        }

        @Override
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            if (! (cursor instanceof ComplexPofValue)) {
                valueOut[0] = cursor.getValue();
                return path;
            }
            else {
                ComplexPofValue cv = (ComplexPofValue) cursor;
                if (cv.getTypeId() > 0) {
                    PofSerializer codec = cv.getPofContext().getPofSerializer(cursor.getTypeId());
                    if (codec instanceof ReflectionPofSerializer) {
                        return ((ReflectionPofSerializer) codec).extract(cursor, path, valueOut);
                    }
                    else {
                        valueOut[0] = cursor.getValue();
                        return path;
                    }
                }
                else {
                    valueOut[0] = cursor.getValue();
                    return path;
                }
            }
        }
    }

    private static class StringPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readString(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeString(propId, (String) obj);
        }
        
        @Override
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(String.class);
            return path;
        }
    }

    private static class BytePropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readByte(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeByte(propId, (Byte) obj);
        }

        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(Byte.class);
            return path;
        }
    }

    private static class ShortPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readShort(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeShort(propId, (Short) obj);
        }

        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(Short.class);
            return path;
        }
    }

    private static class CharPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readChar(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeChar(propId, (Character) obj);
        }
        
        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(Character.class);
            return path;
        }
    }

    private static class IntegerPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readInt(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeInt(propId, (Integer) obj);
        }

        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(Integer.class);
            return path;
        }
    }

    private static class LongPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readLong(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeLong(propId, (Long) obj);
        }

        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(Long.class);
            return path;
        }
    }

    private static class ByteArrayPropCodec implements ObjectPropCodec {
        @Override
        public Object readProp(PofReader reader, int propId, Field field) throws IOException {
            return reader.readByteArray(propId);
        }
        
        @Override
        public void writeProp(PofWriter writer, int propId, Object obj, Field field) throws IOException {
            writer.writeByteArray(propId, (byte[]) obj);
        }

        public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
            valueOut[0] = cursor.getValue(byte[].class);
            return path;
        }
    }
    
    /**
     * WKO object place holder.
     * Should be included in pof-config.xml if wko is used in application.
     * 
     * @author Alexey Ragozin (alexey.ragozin@gmail.com)
     *
     */
    public static class WKO implements Serializable, PortableObject {

        private static final long serialVersionUID = 20090715L;
        
        private String objectRef;
        
        public WKO() {
        }
        
        public WKO(String objectRef) {
            this.objectRef = objectRef;
        }

        @Override
        public void readExternal(PofReader in) throws IOException {
            objectRef = in.readString(0);
        }

        @Override
        public void writeExternal(PofWriter out) throws IOException {
            out.writeString(0, objectRef);            
        }
    }

    public String extract(PofValue cursor, String path, Object[] valueOut) throws IOException {
        ComplexPofValue cv = (ComplexPofValue) cursor;
        Class<?> type = cv.getPofContext().getClass(cv.getTypeId());
        if (type == null || type == Void.class) {
            throw new NullPointerException("value is null, failed to follow [" + path + "]");
        }
        
        ObjectFormat format;
        try {
            format = (ObjectFormat)getClassCodec(type);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }        
        return format.extract(cv, path, valueOut);
    }
}