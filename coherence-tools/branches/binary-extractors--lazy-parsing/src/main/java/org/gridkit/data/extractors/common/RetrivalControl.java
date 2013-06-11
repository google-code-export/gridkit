package org.gridkit.data.extractors.common;

public interface RetrivalControl {

	enum InputStatus {
		POSTPONE,
		ACCEPT,
		DROP,
		;

		/**
		 * Merges status
		 * @param that
		 * @return
		 */
		public InputStatus merge(InputStatus that) {
			if (this == that) {
				return this;
			}
			else if (this == ACCEPT || that == ACCEPT) {
				return ACCEPT;
			}
			else if (this == POSTPONE || that == POSTPONE) {
				return POSTPONE;
			}
			throw new Error("Unreachable case");
		}
	}

	/**
	 * <p>
	 * Starts extraction process. This method will return one extraction is completed.
	 * Extraction could be paused by {@link #done()} method, which will freeze extraction process.
	 * Unless extraction process is paused by {@link #done()}, it will continue until underlying 
	 * binary data will be exhausted.
	 * </p>
	 * <p>
	 * Extraction process could be resumed any number of times. 
	 * </p>
	 */
	public void extract();
	
	public void requireAll();

	public void dropAll();
	
	public void setInputStatus(int inputId, InputStatus status);
	
	public void done();
	
}
