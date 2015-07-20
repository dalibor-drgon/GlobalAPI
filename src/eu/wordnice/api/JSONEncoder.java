package eu.wordnice.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class JSONEncoder {
	
	public static void writeString(OutputStream out, String val) throws IOException {
		out.write('\"');
		out.write(val.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\\n")
				.replace("\t", "\\\t").replace("\b", "\\\b").replace("\r", "\\\r").getBytes());
		out.write('\"');
		
		//ch < ' ' => Integer.toHexString(ch)
	}
	
	public static void writeObject(OutputStream out, Object val) throws IOException {
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
				JSONEncoder.writeString(out, val.toString());
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
				} else if(val instanceof Collection<?>) {
					new Set<Object>((Collection<?>) val).toJsonString(out);
				} else {
					JSONEncoder.writeString(out, val.toString());
				}
			}
		}
	}
	
	
	public static byte[] toJsonString(Object val) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONEncoder.writeObject(baos, val);
			return baos.toByteArray();
		} catch(Throwable t) {}
		return new byte[0]; //not possible!
	}
	
}
