package eu.wordnice.db;

import java.util.Iterator;

import eu.wordnice.db.sql.SQL;
import gnu.trove.map.hash.THashMap;

public class Columns extends THashMap<String, ColType> {
	
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
		
		Iterator<java.util.Map.Entry<String, ColType>> it = this.entrySet().iterator();
		while(it.hasNext()) {
			java.util.Map.Entry<String, ColType> ent = it.next();
			sb.append(ent.getKey());
			sb.append(" ");
			sb.append(ent.getValue().sql);
		}
		
		sb.append(')');
	}
	
}
