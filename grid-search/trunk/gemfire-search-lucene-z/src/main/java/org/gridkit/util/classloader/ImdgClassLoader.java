package org.gridkit.util.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

public class ImdgClassLoader extends URLClassLoader {
	private final List<String> isolatePrefixList;
    private final List<String> parentPrefixList;

	public ImdgClassLoader(URL[] urls, List<String> isolatePrefixList, List<String> parentPrefixList) {
		super(urls);

		this.isolatePrefixList = isolatePrefixList;
		this.parentPrefixList = parentPrefixList;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (shouldUseParentClassLoader(name))
            return super.loadClass(name, resolve);

		Class<?> c = findLoadedClass(name);

		if (c == null)
			c = findClass(name);

		if (resolve)
			resolveClass(c);

		return c;
	}

	private boolean shouldUseParentClassLoader(String className) {
		return !isInPrefixList(className, isolatePrefixList) || isInPrefixList(className, parentPrefixList);
	}

    private boolean isInPrefixList(String className, List<String> prefixList) {
        for (String prefix : prefixList)
            if (className.startsWith(prefix))
                return true;
        return false;
    }
}
