package eu.wordnice.db.operator;

public enum WType {
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is too same as entered
	 */
	EQUAL("$ = 1 ? 2"),

	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is different from entered
	 */
	NOT_EQUAL("$ != 1 ? 2"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database starts with entered string
	 */
	START("$ LIKE 1 '%' || ? 2"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database ends with entered string
	 */
	END("$ LIKE 1 ? || '%' 2"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not start with entered string
	 */
	NOT_START("not($ LIKE 1 '%' || ? 2)"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not end with entered string
	 */
	NOT_END("not($ LIKE 1 ? || '%' 2)"),
	
	/**
	 * For: String
	 * 
	 * Match values from database with regex
	 */
	REGEX("$ REGEXP ?"),
	
	/**
	 * For: String
	 * 
	 * Get values from database which does not match with entered regex
	 */
	NOT_REGEX("$ NOT_REGEXP 1 ? 2"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is bigger than entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER("$ > 1 ? 2"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is bigger than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER_EQUAL("$ >= 1 ? 2"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is smaller than entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER("$ < 1 ? 2"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is smaller than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER_EQUAL("$ <= 1 ? 2");
	
	public String sql;
	
	WType(String sql) {
		this.sql = sql;
	}
	
}
