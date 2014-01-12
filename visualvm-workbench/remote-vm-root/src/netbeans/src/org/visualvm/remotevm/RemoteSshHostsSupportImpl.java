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

import com.sun.tools.visualvm.core.datasource.Storage;
import java.io.File;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public final class RemoteSshHostsSupportImpl {

    public static final String LOCALHOST_PROPERTIES_FILENAME = "localhost" + Storage.DEFAULT_PROPERTIES_EXT; // NOI18N

    private static final String HOSTS_STORAGE_DIRNAME = "remotevm.hosts";    // NOI18N
    private static String HOSTS_STORAGE_DIRECTORY_STRING;


    public static synchronized String getStorageDirectoryString() {
        if (HOSTS_STORAGE_DIRECTORY_STRING == null)
            HOSTS_STORAGE_DIRECTORY_STRING = Storage.getPersistentStorageDirectoryString() +
                    File.separator + HOSTS_STORAGE_DIRNAME;
        return HOSTS_STORAGE_DIRECTORY_STRING;
    }
}
