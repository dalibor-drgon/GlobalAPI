package eu.wordnice.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import eu.wordnice.api.Api;
import eu.wordnice.api.Val;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.results.CollResSet;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResSetDBAdvanced;
import eu.wordnice.db.results.ResSetDBSnap;
import eu.wordnice.db.results.ResultResSet;
import eu.wordnice.db.results.SetSetResSet;
import eu.wordnice.db.sql.MySQL;
import eu.wordnice.db.sql.SQL;
import eu.wordnice.db.sql.SQLite;
import eu.wordnice.db.wndb.WNDB;

/**
 * This class allows you to easily create database of any available
 * type from entered data / configuration
 * 
 * @author wordnice
 */
public class Database {
	
	/**
	 * Set when MySQL, SQLite or any other SQL-based database is used
	 * Pair with sql_table
	 */
	public SQL sql;
	
	/**
	 * SQL table name
	 * Part with sql
	 */
	public String sql_table;
	
	/**
	 * Set when WNDB, FLATFILE, JSON or any other ResSet-based database is used
	 */
	public ResSetDB rs;
	
	/**
	 * @see {@link Database#init(Map)}
	 */
	public Database(Map<String, String> data) throws IllegalArgumentException, Exception {
		this.init(data);
	}
	
	/**
	 * @see {@link Database#init(SQL)}
	 */
	public Database(SQL sql, String table) throws SQLException {
		this.init(sql, table);
	}
	
	/**
	 * @see {@link Database#init(ResSet)}
	 */
	public Database(ResSetDB rs) {
		this.init(rs);
	}
	
	/**
	 * Create database for given type
	 * 
	 * Formats:
	 * - type: sqlite
	 * - file: ./test.sql
	 * - table: test_table
	 * 
	 * - type: wndb
	 * - file: ./test.wndb
	 * 
	 * - type: flatfile (todo)
	 * - file: ./test.txt
	 * 
	 * - type: yaml (todo)
	 * - file: ./test.yml
	 * 
	 * - type: json
	 * - file: ./test.json
	 * 
	 * - type: mysql
	 * - host: localhost:3306
	 * - db: testdb
	 * - user: admin
	 * - pass: Passw0rd!
	 * - table: test_table
	 * 
	 * 
	 * @param data Info
	 * 
	 * @throws IllegalArgumentException when not enought information were provided
	 *                                  or entered database type does not exist
	 * @throws Exception If error occured while parsing / connecting to database
	 */
	public void init(Map<String, String> data) throws IllegalArgumentException, Exception {
		String type = data.get("type");
		if(type == null) {
			throw new IllegalArgumentException("Entered type is null!");
		}
		type = type.toLowerCase();
		if(type.equals("sqlite")) {
			String file = data.get("file");
			if(file == null) {
				throw new IllegalArgumentException("SQLite file is null!");
			}
			String table = data.get("table");
			if(table == null) {
				throw new IllegalArgumentException("SQLite table is null!");
			}
			this.init(new SQLite(new File(file)), table);
			this.sql.connect();
		} else if(type.equals("mysql")) {
			String host = data.get("host");
			if(host == null) {
				throw new IllegalArgumentException("MySQL host is null!");
			}
			String db = data.get("db");
			if(db == null) {
				throw new IllegalArgumentException("MySQL database name is null!");
			}
			String user = data.get("user");
			if(user == null) {
				throw new IllegalArgumentException("MySQL user is null!");
			}
			String pass = data.get("pass");
			if(pass == null) {
				throw new IllegalArgumentException("MySQL pass is null!");
			}
			String table = data.get("table");
			if(table == null) {
				throw new IllegalArgumentException("MySQL table is null!");
			}

			this.init(new MySQL(host, db, user, pass), table);
			this.sql.connect();
		} else if(type.equals("wndb")) {
			String file = data.get("file");
			if(file == null) {
				throw new IllegalArgumentException("SQLite file is null!");
			}
			this.init(new WNDB(new File(file)));
		}
	}
	
	/**
	 * Set database type to SQL
	 * 
	 * @param sql SQL instance
	 * @param Table name
	 */
	public void init(SQL sql, String table) throws SQLException {
		this.rs = null;
		this.sql = sql;
		this.sql_table = table;
	}
	
	/**
	 * Set database type to ResSet
	 * 
	 * @param rs ResSet instance
	 */
	public void init(ResSetDB rs) {
		this.sql = null;
		this.sql_table = null;
		this.rs = rs;
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Integer, Integer, Sort[])}
	 * @return ResSet with all values
	 */
	public ResSet get() throws IllegalArgumentException, Exception {
		return this.get(null, null, null, null, null);
	}
	
	/**
	 * @param columns Columns to get. Returned value can contain more or all
	 *                available columns.
	 *                If null, then there are selected all available columns
	 * @param where Filter values
	 * @param off Offset
	 * @param limit Limit
	 * @param sort Sort by
	 * 
	 * @throws IllegalArgumentException Where (off != null && off < 0) or (limit != null && limit <= 0)
	 * @throws Exception Implementation specific exception
	 * @return Results
	 */
	public ResSet get(String[] columns, AndOr where, Integer off, Integer limit, Sort[] sort) throws IllegalArgumentException, Exception {
		if(this.sql != null) {
			StringBuilder suf = new StringBuilder();
			if(sort != null && sort.length != 0) {
				suf.append(" ORDER BY ");
				for(int i = 0, n = sort.length; i < n; i++) {
					if(i != 0) {
						suf.append(", ");
					}
					suf.append(sort[i].toSQL());
				}
			}
			if(off != null) {
				if(off < 0) {
					throw new IllegalArgumentException("Invalid offset " + off);
				} else if(off > 0) {
					suf.append(" OFFSET ");
					suf.append(off.toString());
				}
			}
			if(limit != null) {
				if(limit <= 0) {
					throw new IllegalArgumentException("Invalid limit " + limit);
				} else {
					suf.append(" LIMIT ");
					suf.append(limit.toString());
				}
			}
			if(columns == null && where == null) {
				return sql.query("SELECT * FROM " + this.sql_table + suf.toString());
			}
			String cols = null;
			if(columns == null) {
				cols = "*";
			} else {
				cols = Api.join(columns, ", ");
			}
			String cmd = "SELECT " + cols + " FROM " + this.sql_table;
			if(where == null) {
				return this.sql.query(cmd + suf.toString());
			}
			Val.TwoVal<String, List<Object>> whproc = where.toSQL();
			PreparedStatement ps = this.sql.prepare(cmd + " WHERE " + whproc.one + suf.toString());
			List<Object> list = whproc.two;
			for(int i = 0, n = list.size(); i < n;) {
				Object v = list.get(i++);
				ps.setObject(i, v);
			}
			//ps.getConnection().setC
			return new ResultResSet(ps.executeQuery());
		} else {
			if(this.rs instanceof ResSetDBAdvanced) {
				return ((ResSetDBAdvanced) this.rs).get(columns, where, off, limit, sort);
			}
			ResSetDBSnap rs = this.rs.getSnapshot();
			if(where != null) {
				while(rs.next()) {
					if(where.match(rs) == false) {
						rs.remove();
					}
				}
			}
			if(sort != null) {
				rs.sort(sort);
			}
			return rs;
		}
	}
	
	
	public static ResSetDB copy(ResSetDBSnap rs) throws IllegalArgumentException, Exception {
		if(rs.isTable()) {
			ResSetDB nev = new SetSetResSet(Api.<String, String>toArray(rs.getKeys()));
			if(rs.isRaw()) {
				while(rs.next()) {
					nev.insertRaw(Api.toArray(rs.getValues()));
				}
			}
		} else {
			ResSetDB nev = new CollResSet();
		}
		return null;
	}
	
	public static ResSetDB sort(ResSetDBSnap rs, Sort[] sort) throws IllegalArgumentException, Exception {
		return Database.sortLimit(rs, null, null, sort);
	}
	
	public static ResSetDB sortLimit(ResSetDBSnap rs, Integer off, Integer len, Sort[] sort) throws IllegalArgumentException, Exception {
		if(rs.isTable()) {
			ResSetDB nev = new SetSetResSet(Api.<String, String>toArray(rs.getKeys()));
			if(rs.isRaw()) {
				while(rs.next()) {
					nev.insertRaw(Api.toArray(rs.getValues()));
				}
			}
		} else {
			ResSetDB nev = new CollResSet();
		}
		return null;
	}
	
}
