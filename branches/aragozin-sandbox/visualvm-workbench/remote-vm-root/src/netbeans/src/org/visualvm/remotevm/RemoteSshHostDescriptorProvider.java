/**
 * Copyright 2012-2014 Alexey Ragozin
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
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("rawtypes")
public class RemoteSshHostDescriptorProvider extends AbstractModelProvider<DataSourceDescriptor,DataSource> {
    
    public DataSourceDescriptor createModelFor(DataSource ds) {
        if (ds instanceof RemoteSshHost) {
            RemoteSshHost host = (RemoteSshHost) ds;
            return new RemoteSshHostDescriptor(host);
        }
        return null;
    }
}
