package eu.wordnice.db.operator;

public enum WFlag {

	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is different from entered
	 * 
	 * Can be combined with (NOT_)ENDS, (NOT_)STARTS, REGEX
	 */
	NOT,
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database starts with entered string
	 * 
	 * Can be combined with (NOT_)ENDS, NOT
	 * Cannot be combined with REGEX
	 */
	STARTS,
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database ends with entered string
	 * 
	 * Can be combined with (NOT_)STARTS, NOT
	 * Cannot be combined with REGEX
	 */
	ENDS,
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not start with entered string
	 * 
	 * Can be combined with (NOT_)ENDS, NOT
	 * Cannot be combined with REGEX
	 */
	NOT_STARTS,
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not end with entered string
	 * 
	 * Can be combined with (NOT_)STARTS, NOT
	 * Cannot be combined with REGEX
	 */
	NOT_ENDS,
	
	/**
	 * For: String
	 * 
	 * Match values from database with regex
	 * 
	 * Can be combined with NOT
	 * Cannot be combined with STARTS nor ENDS
	 */
	REGEX,
	
	/**
	 * For: String, ansi byte[]
	 * 
	 * Match values from database case-insensitive
	 * 
	 * Can be combined with any other flag for String or byte[]
	 */
	INSENSITIVE,
	
	/**
	 * For: Number
	 * 
	 * Check if value in database is bigger than entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER,
	
	/**
	 * For: Number
	 * 
	 * Check if value in database is bigger than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER_EQUAL,
	
	/**
	 * For: Number
	 * 
	 * Check if value in database is smaller than entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER,
	
	/**
	 * For: Number
	 * 
	 * Check if value in database is smaller than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER_EQUAL;
	
	
}
