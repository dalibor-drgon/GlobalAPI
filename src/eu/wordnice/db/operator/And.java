package eu.wordnice.db.operator;

import java.util.ArrayList;
import java.util.List;

import eu.wordnice.api.Val;
import eu.wordnice.api.Val.TwoVal;
import eu.wordnice.db.results.ResSet;

public class And implements AndOr {

	public Object[] objects;
	
	public And(Object... objects) {
		this.objects = objects;
	}

	@Override
	public TwoVal<String, List<Object>> toSQL() {
		StringBuilder sb = new StringBuilder();
		List<Object> vals = new ArrayList<Object>();
		
		for(int i = 0, n = this.objects.length; i < n; i++) {
			if(i != 0) {
				sb.append(" AND ");
			}
			Object obj = this.objects[i];
			if(obj instanceof AndOr) {
				sb.append('(');
				
				Val.TwoVal<String, List<Object>> tsql = ((AndOr) obj).toSQL();
				sb.append(tsql.one);
				vals.addAll(tsql.two);
				
				sb.append(')');
			} else if(obj instanceof Where) {
				Where wh = (Where) obj;
				sb.append(wh.toSQL());
				vals.add(wh.val);
			} else {
				throw new IllegalArgumentException("Unknown argument " 
						+ ((obj == null) ? null : obj.getClass().getName()) + " -> " + obj);
			}
		}
		
		return new Val.TwoVal<>(sb.toString(), vals);
	}

	@Override
	public boolean match(ResSet rs) {
		for(int i = 0, n = this.objects.length; i < n; i++) {
			Object obj = this.objects[i];
			if(obj instanceof AndOr) {
				if(((AndOr) obj).match(rs) == false) {
					return false;
				}
			} else if(obj instanceof Where) {
				if(((Where) obj).match(rs) == false) {
					return false;
				}
			}
		}
		return true;
	}
	
}
