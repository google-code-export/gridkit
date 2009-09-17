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
 */package com.griddynamics.gridkit.coherence.benchmark.capacity.utils;

import java.util.Arrays;

/**
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
public class ArrayUtils {
	
	public static <T> T[] append(T[] base, T object) {
		T[] result = Arrays.copyOf(base, base.length + 1);
		result[result.length - 1] = object;
		return result;
	}
}
