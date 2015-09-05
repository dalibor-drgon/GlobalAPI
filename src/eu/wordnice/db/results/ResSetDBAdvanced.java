package eu.wordnice.db.results;

import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Sort;

public interface ResSetDBAdvanced {
	
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
	public ResSet get(String[] columns, AndOr where, Integer off, Integer limit, Sort[] sort) throws IllegalArgumentException, Exception;
	
}
