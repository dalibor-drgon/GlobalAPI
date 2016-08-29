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

package wordnice.db.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import wordnice.api.Api;
import wordnice.api.Api.DataEntry;
import wordnice.db.results.ResSet;
import wordnice.db.sql.SQL;

public class Or implements AndOr {

	public Object[] objects;
	
	public Or(Object... objects) {
		this.objects = objects;
	}

	@Override
	public DataEntry<String, List<Object>> toSQL(SQL sql) {
		StringBuilder sb = new StringBuilder();
		List<Object> vals = new ArrayList<Object>();
		
		for(int i = 0, n = this.objects.length; i < n; i++) {
			if(i != 0) {
				sb.append(" OR ");
			}
			Object obj = this.objects[i];
			if(obj instanceof AndOr) {
				sb.append('(');
				
				Entry<String, List<Object>> tsql = ((AndOr) obj).toSQL(sql);
				sb.append(tsql.getKey());
				vals.addAll(tsql.getValue());
				
				sb.append(')');
			} else if(obj instanceof Where) {
				Where wh = (Where) obj;
				String sql_text = sql.getWhere(wh);
				sb.append(sql_text);
				if(sql_text.indexOf('?') != -1) {
					vals.add(wh.val);
				}
			} else {
				throw Api.illegal("Unknown argument " 
						+ ((obj == null) ? null : obj.getClass().getName()) + " -> " + obj);
			}
		}
		
		return new DataEntry<String, List<Object>>(sb.toString(), vals);
	}

	@Override
	public boolean match(ResSet rs) {
		for(int i = 0, n = this.objects.length; i < n; i++) {
			Object obj = this.objects[i];
			if(obj instanceof AndOr) {
				if(((AndOr) obj).match(rs)) {
					return true;
				}
			} else if(obj instanceof Where) {
				if(((Where) obj).match(rs)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
