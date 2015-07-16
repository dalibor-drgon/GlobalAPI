package eu.wordnice.api;

import java.io.IOException;
import java.io.OutputStream;

public class JSONEncoder {
	
	public static void writeString(OutputStream out, String val) throws IOException {
		out.write('\"');
		out.write(val.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'").getBytes());
		out.write('\"');
	}
	
	public static void writeValue(OutputStream out, Object val) throws IOException {
		if(val == null) {
			out.write(new byte[] { 'n', 'u', 'l', 'l' });
		} else if(val instanceof CharSequence) {
			JSONEncoder.writeString(out, val.toString());
		} else {
			Class<?> clz = val.getClass();
			
			if(val == (Object) true) {
				out.write(new byte[] { 't', 'r', 'u', 'e' });
			} else if(val == (Object) false) {
				out.write(new byte[] { 'f', 'a', 'l', 's', 'e' });
			} else if(clz.isArray()) {
				try {
					Set<Object> temp_set = new Set<Object>();
					temp_set.addAllX((Object[]) val);
					temp_set.toJsonString(out);
					return;
				} catch(Throwable t) {
					if(t instanceof IOException) {
						throw (IOException) t;
					}
				}
				out.write(new byte[] { '"', 'A', 'R', 'R', '"' });
			} else if(clz.equals(Byte.class)) {
				out.write(String.valueOf((Byte) val).getBytes());
			} else if(clz.equals(Short.class)) {
				out.write(String.valueOf((Short) val).getBytes());
			} else if(clz.equals(Character.class)) {
				out.write(String.valueOf((Character) val).getBytes());
			} else if(clz.equals(Integer.class)) {
				out.write(String.valueOf((Integer) val).getBytes());
			} else if(clz.equals(Long.class)) {
				out.write(String.valueOf((Long) val).getBytes());
			} else if(clz.equals(Float.class)) {
				out.write(String.valueOf((Float) val).getBytes());
			} else if(clz.equals(Double.class)) {
				out.write(String.valueOf((Double) val).getBytes());
			} else {
				if(val instanceof Jsonizable) {
					((Jsonizable) val).toJsonString(out);
				} else {
					JSONEncoder.writeString(out, val.toString());
				}
			}
		}
	}
	
}
