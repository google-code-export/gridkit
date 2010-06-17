/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
package sample;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("serial")
public class DomainObject implements Serializable {

    long id;
    DomainObjAttrib a0;
    DomainObjAttrib a1;
    DomainObjAttrib a2;
    DomainObjAttrib a3;
    
    public DomainObject() {
    }

    public DomainObject(long id) {
        super();
        this.id = id;
    }
    
    public DomainObjKey getKey() {
        return new DomainObjKey(id);
    }
    
    public int getHashSegment() {
        return (int)(id % 1000);
    }
    
    public DomainObjAttrib getA0() {
        return a0;
    }
    
    public DomainObjAttrib getA1() {
        return a1;
    }
    
    public List<DomainObjAttrib> getAs() {
        DomainObjAttrib[] attrs = {a0, a1, a2, a3};
        return Arrays.asList(attrs);
    }
}
