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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexey Ragozin (alaexey.ragozin@gmail.com)
 */
class ReflectionHelper {

    public static void validate(String path) {
        // TODO
    }
    
    public static String getHead(String path) {
        return path == null ? null 
                : path.indexOf('.') > 0 ? path.substring(0, path.indexOf('.')) : path;
    }

    public static String getTail(String path) {
        return path.indexOf('.') > 0 ? path.substring(path.indexOf('.') + 1) : null;
    }
    
    public static int getIndex(String path) {
        if (path.charAt(0) != '[' || path.charAt(path.length() - 1) != ']') {
            throw new IllegalArgumentException("Wrong extraction path '" + path + "'");
        }
        path = path.substring(1, path.length() - 1).trim();
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong extraction path '[" + path + "]'");
        }
    }
    
    private static Extractor[] parsePath(String path) {
        List<Extractor> chain = new ArrayList<Extractor>();
        String remainder = path;
        while(remainder != null) {
            String pHead = getHead(remainder);
            remainder = getTail(remainder);
            // TODO check valid java identifier
            chain.add(new FieldExtractor(pHead));
        }
        return chain.toArray(new Extractor[chain.size()]);
    }
    
    private Extractor[] chain;
    
    public ReflectionHelper(String path) {
        chain = parsePath(path);
    }
    
    public Object extract(Object target) {
        Object result = target;
        for(Extractor ring: chain) {
            if (result == null) {
                break;
            }
            result = ring.extract(result);
        }
        return result;
    }
    
    private static interface Extractor {
        public Object extract(Object target);
    }
    
    private static class FieldExtractor implements Extractor {
        
        private final Map<Class<?>, Field> accessers = new HashMap<Class<?>, Field>();
        private final String field;
        
        public FieldExtractor(String field) {
            this.field = field;
        }

        @Override
        public Object extract(Object target) {
            for(Map.Entry<Class<?>, Field> entry: accessers.entrySet()) {
                if (entry.getKey().isAssignableFrom(target.getClass())) {
                    try {
                        return entry.getValue().get(target);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to access field '" + field + "' at class " + target.getClass().getName(), e);
                    }
                }
            }
            
            Class<?> tclass = target.getClass();
            while(tclass != Object.class) {
                try {
                    Field f = tclass.getDeclaredField(field);
                    if ((f.getModifiers() & Modifier.STATIC) == 0) {
                        f.setAccessible(true);
                        accessers.put(tclass, f);
                        return extract(target);
                    }
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    // ignore
                }
                tclass = tclass.getSuperclass();
            }
            
            if (field.equals("length") && target.getClass().isArray()) {
                return Array.getLength(target);
            }
            
            throw new IllegalArgumentException("Field '" + field + "' is not found in class " + target.getClass().getName());
        }
    }
    
//    private static class ArrayLengthExtractor implements Extractor {
//        
//    }
}
