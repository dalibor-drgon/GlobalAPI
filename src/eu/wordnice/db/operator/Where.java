package eu.wordnice.db.operator;

import java.util.regex.Pattern;

import eu.wordnice.api.Api;
import eu.wordnice.api.ByteString;
import eu.wordnice.db.Database;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.sql.MySQL;
import eu.wordnice.db.sql.SQL;

public class Where {
	
	/**
	 * Column name / key for val
	 */
	public String key;
	
	/**
	 * Value to find
	 */
	public Object val;
	
	/**
	 * Compare type
	 */
	public WType flag;
	
	/**
	 * Compare case-sensitive
	 */
	public boolean sens;
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 */
	public Where(String key, Object val) {
		this.key = key;
		this.val = val;
		this.flag = WType.EQUAL;
		this.sens = true;
	}
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 * @param flag {@link WType} {@link Where#flag}
	 */
	public Where(String key, Object val, WType flag) {
		this.key = key;
		this.val = val;
		this.flag = (flag == null) ? WType.EQUAL : flag;
		this.sens = true;
	}
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 * @param flag {@link WType} {@link Where#flag}
	 * @param sens Compare case-sensitive {@link Where#sens}
	 */
	public Where(String key, Object val, WType flag, boolean sens) {
		this.key = key;
		this.val = val;
		this.flag = (flag == null) ? WType.EQUAL : flag;
		this.sens = sens;
	}
	
	/**
	 * @return SQL format
	 */
	public String toSQL() {
		String str = this.flag.sql;
		if(this.val instanceof Number) {
			return Api.replace(str, new Object[]{
					"1 ", "",
					" 2", "",
					"$", this.key
			});
		}
		if(this.val instanceof byte[]) {
			return Api.replace(str, new Object[]{
					"1", (this.sens) ? "BINARY" : "",
					" 2", "",
					"$", this.key
			});
		} else {
			return Api.replace(str, new Object[]{
					"1 ", "",
					"2", (this.sens) ? "COLLATE utf8_bin" : "",
					"$", this.key
			});
		}
	}
	
	/**
	 * @param rs ResSet with values to compare
	 * @return `true` if values match with this AndOr
	 */
	public boolean match(ResSet rs) {
		switch(this.flag) {
			case BIGGER:
				return rs.getDouble(this.key) > ((Number) this.val).doubleValue();
				
			case BIGGER_EQUAL:
				return rs.getDouble(this.key) >= ((Number) this.val).doubleValue();
				
			case SMALLER:
				return rs.getDouble(this.key) < ((Number) this.val).doubleValue();
				
			case SMALLER_EQUAL:
				return rs.getDouble(this.key) <= ((Number) this.val).doubleValue();
				
			case REGEX:
				if(this.sens) {
					return Pattern.compile((String) this.val).matcher("" + rs.getObject(this.key)).find();
				}
				return Pattern.compile((String) this.val, Pattern.CASE_INSENSITIVE).matcher("" + rs.getObject(this.key)).find();
				
			case NOT_REGEX:
				if(this.sens) {
					return !Pattern.compile((String) this.val).matcher("" + rs.getObject(this.key)).find();
				}
				return !Pattern.compile((String) this.val, Pattern.CASE_INSENSITIVE).matcher("" + rs.getObject(this.key)).find();
			
			case NOT_EQUAL:
				if(this.val instanceof Number) {
					return rs.getDouble(this.key) != ((Number) this.val).doubleValue();
				} else if(this.val instanceof byte[]) {
					if(this.sens) {
						return !ByteString.equals(rs.getBytes(this.key), (byte[]) this.val);
					}
					return !ByteString.equalsIgnoreCase(rs.getBytes(this.key), (byte[]) this.val);
				} else if(this.val == null) {
					return rs.getObject(this.key) != null;
				} else {
					if(!this.sens == this.val instanceof String) {
						return !((String) this.val).equalsIgnoreCase(rs.getString(this.key));
					}
					return !this.val.equals(rs.getObject(this.key));
				}
				
			case EQUAL:
			default:
				if(this.val instanceof Number) {
					return rs.getDouble(this.key) == ((Number) this.val).doubleValue();
				} else if(this.val instanceof byte[]) {
					if(this.sens) {
						return ByteString.equals(rs.getBytes(this.key), (byte[]) this.val);
					}
					return ByteString.equalsIgnoreCase(rs.getBytes(this.key), (byte[]) this.val);
				} else if(this.val == null) {
					return rs.getObject(this.key) == null;
				} else {
					if(!this.sens == this.val instanceof String) {
						return ((String) this.val).equalsIgnoreCase(rs.getString(this.key));
					}
					return this.val.equals(rs.getObject(this.key));
				}
		}
	}
	
	/**
	 * Test
	 */
	public static void main(String... lel_varargs) throws Throwable {
		/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WNDB w = WNDB.createWNDB(new OStream(baos), new String[] {"yolo", "thug"}, new DBType[] {DBType.BOOLEAN, DBType.STRING});
		Database db = new Database(w);*/
		SQL sql = new MySQL("db.mysql-01.gsp-europe.net", "sql_1040", "sql_1040", "2qZ0h1e0nURTWbfiCQpHaz50Not8yuV");
		Database db = new Database(sql, "shets");
		ResSet rs = db.get(null, new And(
				new Where("rekts", "SHREKT", WType.NOT_EQUAL),
				new Where("rekts", "SHREKTy", WType.NOT_EQUAL, true),
				new Where("rekts", "SHREKTy", WType.EQUAL, false),
				new Where("rektb", new byte[] {}, WType.EQUAL, false),
				new Where("rektd", 23.43, WType.SMALLER, false)
		), null, null, new Sort[] {
				new Sort("rekts", SType.ASC_SC, false)
		});
		while(rs.next()) {
			System.out.println(rs.getString("rekts"));
		}
	}
	
	
}
