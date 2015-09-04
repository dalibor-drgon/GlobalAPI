package eu.wordnice.db.operator;

import java.util.List;

import eu.wordnice.api.Val;
import eu.wordnice.db.results.ResSet;

public interface AndOr {
	
	/**
	 * @return SQL string with values needed for PreparedStatement
	 */
	public Val.TwoVal<String, List<Object>> toSQL();
	
	/**
	 * @param rs ResSet with values to compare
	 * @return `true` if values match with this AndOr
	 */
	public boolean match(ResSet rs);
	
}
