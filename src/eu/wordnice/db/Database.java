package eu.wordnice.db;

import java.io.File;
import java.util.Map;

import eu.wordnice.db.results.ResSet;
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
	 */
	public SQL sql;
	
	/**
	 * Set when WNDB, FLATFILE, JSON or any other ResSet-based database is used
	 */
	public ResSet rs;
	
	/**
	 * @see {@link Database#init(Map)}
	 */
	public Database(Map<String, String> data) throws IllegalArgumentException, Exception {
		this.init(data);
	}
	
	/**
	 * @see {@link Database#init(SQL)}
	 */
	public Database(SQL sql) {
		this.init(sql);
	}
	
	/**
	 * @see {@link Database#init(ResSet)}
	 */
	public Database(ResSet rs) {
		this.init(rs);
	}
	
	/**
	 * Create database for given type
	 * 
	 * Formats:
	 * - type: sqlite
	 * - file: ./test.sql
	 * 
	 * - type: wndb
	 * - file: ./test.wndb
	 * 
	 * - type: flatfile
	 * - file: ./test.txt
	 * 
	 * - type: json
	 * - file: ./test.json
	 * 
	 * - type: mysql
	 * - host: localhost:3306
	 * - db: testdb
	 * - user: admin
	 * - pass: Passw0rd!
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
			this.init(new SQLite(new File(file)));
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

			this.init(new MySQL(host, db, user, pass));
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
	 */
	public void init(SQL sql) {
		this.sql = sql;
	}
	
	/**
	 * Set database type to ResSet
	 * 
	 * @param rs ResSet instance
	 */
	public void init(ResSet rs) {
		this.rs = rs;
	}
	
	
	
}
