package eu.wordnice.db.operator;

public enum SType {
	
	/**
	 * Ascending = From the smallest to the biggest
	 */
	ASC("$ ASC", "ASC"),
	
	/**
	 * Descending = From the biggest to the smallest
	 */
	DESC("DESC", "DESC"),
	
	/**
	 * Case-insensitive
	 * Ascending = From the smallest to the biggest
	 */
	ASC_SC("ASC", "ASC"),
	
	/**
	 * Case-insensitive
	 * Descending = From the biggest to the smallest
	 */
	DESC_SC("DESC", "DESC");
	
	/**
	 * SQL ORDER BY syntax for text
	 */
	private String sql_text;
	
	/**
	 * SQL ORDER BY syntax for binary byte[]
	 */
	private String sql_bin;
	
	SType(String text, String bin) {
		this.sql_text = text;
		this.sql_bin = bin;
	}
	
	/**
	 * @param bin Is for binary data
	 * @return ORDER BY $table _
	 */
	public String toSQL(boolean bin) {
		return (bin) ? this.sql_bin : this.sql_text;
	}
	
}
