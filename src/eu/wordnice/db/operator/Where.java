package eu.wordnice.db.operator;

public class Where {
	
	public class Entry {
		
		/**
		 * Column name / key for val
		 */
		public String key;
		
		/**
		 * Value to find
		 */
		public Object val;
		
		/**
		 * Flags
		 */
		public WFlag[] flags;
		
		/**
		 * Create Entry comparator
		 * 
		 * @param key Column name / key for val {@link Where#key}
		 * @param val Value to find {@link Where#val}
		 * @param flags Flags {@link Where#flags}
		 */
		public Entry(String key, Object val, WFlag... flags) {
			this.key = key;
			this.val = val;
			this.flags = flags;
		}
		
	}
	
}
