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
package com.griddynamics.gridkit.coherence.benchmark.capacity.objects.sample;

import java.io.Serializable;

/**
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
@SuppressWarnings("serial")
public class DomainObject implements Serializable {

    private long id;
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
    
    public DomainObjAttrib getA0() {
        return a0;
    }
    
    public DomainObjAttrib getA1() {
        return a1;
    }
}
