package eu.wordnice.db.operator;

public class Sort {

	/**
	 * Key / column name
	 */
	public String key;
	
	/**
	 * Sort method type
	 */
	public SType type;
	
	/**
	 * Are data binary
	 */
	public boolean bin;
	
	/**
	 * Create when requesting sorted data
	 * 
	 * @param key Key / column name {@link Sort#key}
	 * @param type Sort method type {@link Sort#type}
	 * @param bin Are data binary {@link Sort#bin}}
	 */
	public Sort(String key, SType type, boolean bin) {
		this.key = key;
		this.type = type;
		this.bin = bin;
	}
	
	/**
	 * Create when requesting sorted data
	 * 
	 * @param key Key / column name {@link Sort#key}
	 * @param type Sort method type {@link Sort#type}
	 */
	public Sort(String key, SType type) {
		this.key = key;
		this.type = type;
		this.bin = false;
	}
	
	/**
	 * Create when requesting sorted data (ascending)
	 * 
	 * @param key Key / column name. {@link Sort#key}
	 */
	public Sort(String key) {
		this.key = key;
		this.type = null;
		this.bin = false;
	}

	
	/**
	 * @return SQL string
	 */
	public String toSQL() {
		return this.key + " " + ((this.type == null) ? SType.ASC.toSQL(this.bin) : this.type.toSQL(this.bin));
	}
	
}
