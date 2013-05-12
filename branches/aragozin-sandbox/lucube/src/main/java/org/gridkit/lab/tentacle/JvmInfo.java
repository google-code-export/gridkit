package org.gridkit.lab.tentacle;

import java.io.Serializable;

public class JvmInfo {

	public static Sampler<JvmProcess, SysProp> sysProperty(String propName) {
		return new SysPropertySampler(propName);
	}

	public interface SysProp extends Sample {
		
		public String name();
		
		public String value();
		
	}

	private static SysProp propSample(final String name, final String value) {
		return new SysProp() {
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public String value() {
				return value;
			}
		};
	}

	private static class SysPropertySampler implements Sampler<JvmProcess, SysProp>, Serializable {

		private static final long serialVersionUID = 20130510L;
		
		private final String propName;
		
		public SysPropertySampler(String propName) {
			this.propName = propName;
		}

		@Override
		public SysProp sample(JvmProcess target) {
			return propSample(propName, target.getSystemProperty(propName));
		}
	}
}
