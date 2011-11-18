package org.gridkit.gatling.utils;

/** 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com) 
 */
public interface SpeedLimit {
	
	public void accure();

	public void dispose();

	public static class Helper {
	
		private static SpeedLimit noSpeedLimit = new NoSpeedLimit();

		public static SpeedLimit newSpeedLimit(double eventsPerSecond) {
			if (eventsPerSecond < 0) {
				throw new IllegalArgumentException("speedLimit should be >= 0");
			}
			else if (eventsPerSecond == Double.POSITIVE_INFINITY) {
				return noSpeedLimit;
			}
			else {
				int replenishLimit = (int)(eventsPerSecond * 0.1);
				if (replenishLimit < 1) {
					replenishLimit = 1;
				}
				return new SimpleSpeedLimit(eventsPerSecond, replenishLimit);
			}
		}
	}
}
