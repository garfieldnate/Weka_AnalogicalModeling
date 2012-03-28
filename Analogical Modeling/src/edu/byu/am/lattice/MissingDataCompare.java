package edu.byu.am.lattice;

public enum MissingDataCompare {
	/**
	 * Always returns 0, considering the missing data to match anything
	 */
	MATCH {
		@Override
		public int outcome(int var1, int var2) {
			return 0;
		}

	},

	/**
	 * Always returns 1, considering the missing data to be a mismatch
	 */
	MISMATCH {
		@Override
		public int outcome(int var1, int var2) {
			return 1;
		}

	},

	/**
	 * Treats missing data a an outcome of its own; comparing two missing
	 * data will return 0, but otherwise 1.
	 */
	VARIABLE {
		@Override
		public int outcome(int var1, int var2) {
			return var1 != var2 ? 1 : 0;
		}

	};
	/**
	 * 
	 * @param var1
	 * @param var2
	 * @return 0 or 1, depending on how the comparison is done.
	 */
	public abstract int outcome(int var1, int var2);
}