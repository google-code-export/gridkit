package org.gridkit.lab.tentacle;

import org.gridkit.lab.mcube.Value;
import org.gridkit.lab.mcube.Values;

/**
 * Marker interface for sample tuple interfaces
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface Sample {

	
	/**
	 * Internal fields of each sample.
	 * 
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	public interface SampleMeta extends Sample {

		public static Value STAR = Values.capture(Values.call(SampleMeta.class).star());
		public static Value SOURCE = Values.capture(Values.call(SampleMeta.class).source());
		public static Value TIMESTAMP = Values.capture(Values.call(SampleMeta.class).timestamp());

		/**
		 * Special field to use in count aggregator.
		 * @return <code>null</code>
		 */
		public Object star();
		
		/**
		 * @return unique ID of monitoring target associated with sample
		 */
		public String source();

		/**
		 * @return for {@link Timestamp} sample dubs existing timestamp, otherwise timestamp of sample reporting event
		 */
		@Timestamp
		public double timestamp();
		
	}
}
