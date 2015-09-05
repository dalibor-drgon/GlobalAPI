package eu.wordnice.db.operator;

import java.util.Comparator;

import eu.wordnice.api.ByteChar;

public enum SType {
	
	/**
	 * Ascending = From the smallest to the biggest
	 */
	ASC("ASC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			if(o1 instanceof CharSequence && o2 instanceof CharSequence) {
				CharSequence s1 = (CharSequence) o1;
				CharSequence s2 = (CharSequence) o2;
				int n1 = s1.length();
				int n2 = s2.length();
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					char c1 = s1.charAt(i);
					char c2 = s2.charAt(i);
					if(c1 != c2) {
						c1 = Character.toUpperCase(c1);
						c2 = Character.toUpperCase(c2);
						if(c1 != c2) {
							c1 = Character.toLowerCase(c1);
							c2 = Character.toLowerCase(c2);
							if(c1 != c2) {
								return c1 - c2;
							}
						}
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof byte[] && o2 instanceof byte[]) {
				byte[] b1 = (byte[]) o1;
				byte[] b2 = (byte[]) o2;
				int n1 = b1.length;
				int n2 = b2.length;
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					byte c1 = b1[i];
					byte c2 = b2[i];
					if(c1 != c2) {
						c1 = ByteChar.toLower(c1);
						c2 = ByteChar.toLower(c2);
						if(c1 != c2) {
							return c1 - c2;
						}
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof Number && o2 instanceof Number) {
				double d1 = ((Number) o1).doubleValue();
				double d2 = ((Number) o2).doubleValue();
				if(d1 > d2) {
					return 1;
				} else if(d1 < d2) {
					return -1;
				} else {
					return 0;
				}
			}
			
			if(o1 != null && o2 != null) {
				return o1.hashCode() - o2.hashCode();
			} else if(o1 == null && o2 == null) {
				return 0;
			} else if(o2 == null) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}),
	
	/**
	 * Descending = From the biggest to the smallest
	 */
	DESC("DESC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			return SType.ASC.getComp().compare(o2, o1);
		}
		
	}),
	
	/**
	 * Case-insensitive
	 * Ascending = From the smallest to the biggest
	 */
	ASC_SC("COLLATE utf8_bin ASC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			if(o1 instanceof CharSequence && o2 instanceof CharSequence) {
				CharSequence s1 = (CharSequence) o1;
				CharSequence s2 = (CharSequence) o2;
				int n1 = s1.length();
				int n2 = s2.length();
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					char c1 = s1.charAt(i);
					char c2 = s2.charAt(i);
					if(c1 != c2) {
						return c1 - c2;
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof byte[] && o2 instanceof byte[]) {
				byte[] b1 = (byte[]) o1;
				byte[] b2 = (byte[]) o2;
				int n1 = b1.length;
				int n2 = b2.length;
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					byte c1 = b1[i];
					byte c2 = b2[i];
					if(c1 != c2) {
						return c1 - c2;
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof Number && o2 instanceof Number) {
				double d1 = ((Number) o1).doubleValue();
				double d2 = ((Number) o2).doubleValue();
				if(d1 > d2) {
					return 1;
				} else if(d1 < d2) {
					return -1;
				} else {
					return 0;
				}
			}
			
			if(o1 != null && o2 != null) {
				return o1.hashCode() - o2.hashCode();
			} else if(o1 == null && o2 == null) {
				return 0;
			} else if(o2 == null) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}),
	
	/**
	 * Case-insensitive
	 * Descending = From the biggest to the smallest
	 */
	DESC_SC("COLLATE utf8_bin DESC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			return SType.ASC_SC.getComp().compare(o2, o1);
		}
		
	});
	
	/**
	 * SQL ORDER BY syntax for text
	 */
	private String sql_text;
	
	/**
	 * Comparision of values
	 */
	public Comparator<Object> comp;
	
	/**
	 * Internal creation of SType
	 * 
	 * @param text SQL for text and numbers
	 * @param bin SQL for blob
	 * @param comp Comparision of values
	 */
	SType(String text, Comparator<Object> comp) {
		this.sql_text = text;
		this.comp = comp;
	}
	
	/**
	 * @param bin Is for binary data
	 * @return ORDER BY $table _
	 */
	public String toSQL(boolean bin) {
		return this.sql_text;
	}
	
	/**
	 * @return Comparator for values
	 */
	public Comparator<Object> getComp() {
		return this.comp;
	}
	
}
