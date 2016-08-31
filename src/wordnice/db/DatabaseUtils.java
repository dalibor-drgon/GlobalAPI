/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.coll.CollUtils;
import wordnice.db.results.ArraysResSet;
import wordnice.db.results.MapsResSet;
import wordnice.db.results.ResSet;
import wordnice.db.results.ResSetDB;
import wordnice.db.serialize.SerializeException;
import wordnice.db.sql.DriverManagerSQL;
import wordnice.db.sql.MySQL;
import wordnice.db.sql.SQL;
import wordnice.db.sql.SQLite;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;
import wordnice.utils.FilesAPI;

public class DatabaseUtils {

	/**
	 * @see 
	 */
	public static Database createDatabaseFromConfig(Map<String, Object> data, Map<String, ColType> cols)
			throws IllegalArgumentException, SerializeException, IOException, SQLException {
		return DatabaseUtils.createDatabaseFromConfig(data, cols, null);
	}
	
	/**
	 * Create database for given type
	 * 
	 * Formats:
	 * <pre>
	 * {@code
	 * - type: mysql
	 * - host: localhost:3306
	 * - db: testdb
	 * - user: admin
	 * - pass: Passw0rd!
	 * - table: test_table
	 * 
	 * - type: sqlite
	 * - file: ./test.sqlite
	 * - table: test_table
	 * 
	 * - type: driver
	 * - target: jdbc:somevendor:somedata
	 * - table: test_table
	 * - pass: Passw0rd!      # optional
	 * - user: admin          # optional
	 * - driver: org.hi.wrld  # optional (driver to load)
	 * - useSQLite: false     # optional (use sqlite syntax)
	 * - onConnect:           # optional (commands on every re-connect)
	 *     - SET CHARSET 'utf8'
	 *     - SET NAMES 'utf8' COLLATE 'utf8_unicode_ci'
	 * 
	 * 
	 * # Types below are good for small amount of data
	 * # If you have more than 10MB of data, 
	 * # use professional databases like mysql
	 * 
	 * - type: wndb           # binary
	 * - file: ./test.wndb
	 * 
	 * - type: flatfile (todo)
	 * - file: ./test.txt
	 * 
	 * - type: yaml (todo)
	 * - file: ./test.yml
	 * 
	 * - type: json (todo)
	 * - file: ./test.json
	 * }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param data Map
	 * @param cols Columns names and types
	 * @param root Root directory (null = current)
	 * 
	 * @throws IllegalArgumentException when not enought information were provided
	 *                                  or entered database type does not exist
	 * @throws SerializeException Error while parsing file-based database
	 * @throws IOException Error while reading file-based database
	 * @throws SQLException Error while connecting or quering SQL-based database
	 */
	public static Database createDatabaseFromConfig(Map<String, Object> data, Map<String, ColType> cols, File root)
			throws IllegalArgumentException, SerializeException, IOException, SQLException {
		Object otype = data.get("type");
		if(otype == null) {
			throw Nice.illegal("Entered type is null!");
		}
		String type = otype.toString().toLowerCase();
		if(type.equals("sqlite")) {
			Object file = data.get("file");
			if(file == null) {
				throw Nice.illegal("SQLite file is null!");
			}
			Object table = data.get("table");
			if(table == null) {
				throw Nice.illegal("SQLite table is null!");
			}
			SQLDatabase db = new SQLDatabase(new SQLite(FilesAPI.getRealPath(root, file.toString())), table.toString(), cols);
			db.sql.connect();
			return db;
		} else if(type.equals("mysql")) {
			Object host = data.get("host");
			if(host == null) {
				throw Nice.illegal("MySQL host is null!");
			}
			Object db = data.get("db");
			if(db == null) {
				throw Nice.illegal("MySQL database name is null!");
			}
			Object user = data.get("user");
			if(user == null) {
				throw Nice.illegal("MySQL user is null!");
			}
			Object pass = data.get("pass");
			if(pass == null) {
				throw Nice.illegal("MySQL pass is null!");
			}
			Object table = data.get("table");
			if(table == null) {
				throw Nice.illegal("MySQL table is null!");
			}

			SQLDatabase sdb = new SQLDatabase(new MySQL(host.toString(), db.toString(), user.toString(), pass.toString()), table.toString(), cols);
			sdb.sql.connect();
			return sdb;
		} else if(type.equals("driver")) {
			Object target = data.get("target");
			if(target == null) {
				throw Nice.illegal("JDBC target is null!");
			}
			Object table = data.get("table");
			if(table == null) {
				throw Nice.illegal("JDBC table is null!");
			}
			Object user = data.get("user");
			Object pass = data.get("pass");
			boolean single = true;
			if(user != null || pass != null) {
				single = false;
			}
			
			Object driver = data.get("driver");
			if(driver != null) {
				try {
					if(Class.forName(driver.toString()) == null) {
						throw new NullPointerException("JVM returned null class!");
					}
				} catch(Throwable t) {
					throw new SQLException("Cannot load driver (" + driver + ")", t);
				}
			}
			
			DriverManagerSQL dmsql = new DriverManagerSQL(target.toString(), 
					((user == null) ? null : user.toString()),
					((pass == null) ? null : pass.toString()),
					single);
			
			Object useSQLite = data.get("useSQLite");
			if(useSQLite != null) {
				String sqlt = "" + useSQLite;
				if(sqlt.equalsIgnoreCase("true") || sqlt.equalsIgnoreCase("yes")
						|| sqlt.equalsIgnoreCase("y") || sqlt.equals("1")) {
					dmsql.useSQLite = true;
				}
			}
			
			Object onConnect = data.get("onConnect");
			if(onConnect != null) {
				if(onConnect instanceof String[]) {
					dmsql.onConnect = (String[]) onConnect;
				} else if(onConnect instanceof Collection) {
					dmsql.onConnect = ((Collection<?>) onConnect).toArray(new String[0]);
				}
			}
			SQLDatabase sdb = new SQLDatabase(dmsql, table.toString(), cols);
			sdb.sql.connect();
			return sdb;
		} else if(type.equals("wndb")) {
			Object file = data.get("file");
			if(file == null) {
				throw Nice.illegal("WNDB file is null!");
			}
			File fl = (root == null) ? new File(file.toString()) : new File(root, file.toString());
			ResSetDatabase rsdb = new ResSetDatabase(ArraysResSet.loadOrCreateSafe(fl, 
					cols.keySet().toArray(new String[0]),
					cols.values().toArray(new ColType[0])), fl);
			return rsdb;
		} else {
			throw Nice.illegal("Unknown database type " + type);
		}
		
	}
	
	
	/**
	 * Convert sql-unsupported object types to supported (e.g. serialize collections
	 * and maps to bytes)
	 * 
	 * @param obj Object to convert
	 * 
	 * @return If given object is recognized as not supported by sql, is converted to
	 *         any other supported type.
	 *         
	 * @throws SerializeException Exception during serialization
	 *         
	 * @see {@link SQLDatabase#fromSQLObject(Object)}
	 */
	public static Object toSQLObject(Object obj) throws SerializeException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(DatabaseUtils.toSQLObject(baos, obj)) {
			return baos.toByteArray();
		}
		return obj;
	}
	
	/**
	 * Serialize object and write it to OutputStream stream
	 * 
	 * @return true If object was serialized, false if object is not known
	 * 
	 * @see {@link DatabaseUtils}
	 */
	public static boolean toSQLObject(OutputStream out, Object obj) throws SerializeException {
		if(obj instanceof Object[]) {
			try {
				OUtils.writeInt(out, 0x71830);
				OUtils.serializeKnownObject(out, (Object[]) obj);
				return true;
			} catch(IOException ioe) {
				throw new SerializeException("Unexpected IO error occured", ioe);
			}
		} else if(obj instanceof Map) {
			try {
				OUtils.writeInt(out, 0x71830);
				OUtils.serializeKnownObject(out, (Map<?, ?>) obj);
				return true;
			} catch(IOException ioe) {
				throw new SerializeException("Unexpected IO error occured", ioe);
			}
		} else if(obj instanceof Collection) {
			try {
				OUtils.writeInt(out, 0x71830);
				OUtils.serializeKnownObject(out, (Collection<?>) obj);
				return true;
			} catch(IOException ioe) {
				throw new SerializeException("Unexpected IO error occured", ioe);
			}
		}
		return false;
	}
	
	/**
	 * Convert back (possibly from serialized) sql-supported object back to original type
	 * 
	 * @param obj Object to convert (deserialize)
	 * 
	 * @return If conversion was successful, returns new converted object,
	 *         otherwise {@code obj}
	 * 
	 * @see {@link SQLDatabase#toSQLObject(Object)}
	 */
	public static Object fromSQLObject(Object obj) {
		if(obj instanceof byte[]) {
			try {
				InputStream in = new ByteArrayInputStream((byte[]) obj);
				if(IUtils.readInt(in) != 0x71830) {
					return obj;
				}
				Object readed = IUtils.deserializeKnownObject(in);
				in.close();
				if(readed instanceof Collection || readed instanceof Map) {
					return readed;
				}
			} catch(Exception ign) {}
		} else if(obj instanceof Blob) {
			try {
				return DatabaseUtils.fromSQLObject((byte[]) ((Blob) obj).getBytes(0, (int) ((Blob) obj).length()));
			} catch(Exception exc) {
				return obj;
			}
		}
		return obj;
	}
	
	/**
	 * Create copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @param rs ResSet to copy
	 * 
	 * @return copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @throws DatabaseException Implementation specific exception
	 * @throws SQLException Exception from JDBC
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static ResSetDB copy(ResSet rs) throws SQLException, DatabaseException {
		if(rs.isTable()) {
			ResSetDB nev = new ArraysResSet(CollUtils.<String>toArray(rs.getKeys(), String.class));
			while(rs.next()) {
				nev.insertRaw(rs.getValues());
			}
			return nev;
		}
		ResSetDB nev = new MapsResSet();
		while(rs.next()) {
			nev.insert(rs.getEntries());
		}
		return nev;
	}
	
	/**
	 * Copy values from second to first argument (insert rs to out)
	 * 
	 * @param out Destination
	 * @param rs Source
	 * 
	 * @throws DatabaseException Implementation specific exception
	 * @throws SQLException Exception from JDBC
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static void copy(ResSetDB out, ResSet rs) throws SQLException, DatabaseException {
		if(rs.isTable() && out.isTable() && out.isRaw() && out.getKeys().equals(rs.getKeys())) {
			while(rs.next()) {
				out.insertRaw(rs.getValues());
			}
		} else {
			while(rs.next()) {
				out.insert(rs.getEntries());
			}
		}
	}
	
	
	/**
	 * CREATE TABLE IF NOT EXISTS [table]
	 * 
	 * @param map Map with column names and types
	 * @param sql SQL connection
	 * @param table Table name
	 * 
	 * @throws SQLException Exception while processing command
	 */
	public static void createTable(Map<String, ColType> map, SQL sql, String table) throws SQLException {
		if(!sql.useSQLiteSyntax()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ");
			sb.append(table);
			sb.append(" (");
			
			Iterator<java.util.Map.Entry<String, ColType>> it = map.entrySet().iterator();
			int i = 0;
			while(it.hasNext()) {
				if(i++ != 0) {
					sb.append(',').append(' ');
				}
				java.util.Map.Entry<String, ColType> ent = it.next();
				//sb.append('`');
				sb.append(ent.getKey());
				sb.append(' ');
				sb.append(ent.getValue().sql);
			}
			
			sb.append(") DEFAULT CHARSET=utf8 DEFAULT COLLATE utf8_unicode_ci");
			
			sql.command(sb.toString());
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ");
			sb.append(table);
			sb.append(" (");
			
			Iterator<java.util.Map.Entry<String, ColType>> it = map.entrySet().iterator();
			int i = 0;
			while(it.hasNext()) {
				if(i++ != 0) {
					sb.append(',').append(' ');
				}
				java.util.Map.Entry<String, ColType> ent = it.next();
				sb.append('`');
				sb.append(ent.getKey());
				sb.append('`').append(' ');
				sb.append(ent.getValue().sql_sqlite);
			}
			
			sb.append(')');
			
			sql.command(sb.toString());
		}
	}
	
}
