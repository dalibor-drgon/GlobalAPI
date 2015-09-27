/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.wordnice.db.sql;

import java.sql.SQLException;

import eu.wordnice.api.Api;
import eu.wordnice.db.ColType;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.operator.Where;

public abstract class AbstractSQL implements SQL {

	public AbstractSQL() {}
	
	public static String escapeDM(String in) {
		return Api.getURLEncoded(in);
	}

	@Override
	public void reconnect() throws SQLException {
		try {
			this.close();
		} catch(SQLException sqle) {}
		this.connect();
	}
	
	@Override
	public String getWhere(Where where) {
		String str = where.flag.sql;
		if(where.val instanceof Number) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", "",
					"$", where.key
			});
		} else if(where.val instanceof byte[]) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", ((byte[]) where.val).length,
					"$", where.key
			});
		} else if(where.val instanceof String) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					"222", ((this.useSQLiteSyntax()) 
							? (where.sens) ? "" : "COLLATE NOCASE"
							: (where.sens) ? "COLLATE utf8_bin" : ""),
					"333", ((String) where.val).length(),
					"$", where.key
			});
		} else if(where.val == null) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", "0",
					"$", where.key
			});
		} else {
			throw new IllegalArgumentException("Unknown value type " + where.val.getClass().getName());
		}
	}
	
	@Override
	public String getSort(Sort sort, ColType tp) {
		if(tp == ColType.STRING) {
			if(this.useSQLiteSyntax()) {
				return sort.key + " " + sort.type.sqlite_str;
			} else {
				return sort.key + " " + sort.type.sql_str;
			}
		}
		return sort.key + " " + sort.type.sql;
	}
	
	@Override
	public boolean useSQLiteSyntax() {
		return false;
	}

}
