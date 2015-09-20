package eu.wordnice.db;

import eu.wordnice.db.sql.SQL;
import gnu.trove.map.hash.THashMap;

public class Columns extends THashMap<String, DBType> {
	
	/**
	 * CREATE TABLE IF NOT EXISTS [table]
	 * 
	 * @param sql SQL connection
	 * @param table Table name
	 */
	public void sendCommand(SQL sql, String table) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(table);
		sb.append(" (");
		
		sb.append(')');
	}
	
}
