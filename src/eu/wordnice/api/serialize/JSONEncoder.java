/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.api.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.Api;

public class JSONEncoder {
	
	public static void writeString(OutputStream out, String val) throws IOException {
		if(val == null) {
			out.write(new byte[] {'n','u','l','l'});
			return;
		}
		out.write('\"');
		out.write(val.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
				.replace("\t", "\\t").replace("\b", "\\b").replace("\r", "\\r").getBytes());
		out.write('\"');
	}
	
	public static void writeObject(OutputStream out, Object val) throws IOException {
		if(val == null) {
			out.write(new byte[] { 'n', 'u', 'l', 'l' });
		} else {
			Class<?> clz = val.getClass();
			
			if(val instanceof CharSequence) {
				JSONEncoder.writeString(out, val.toString());
			} else if(val instanceof byte[]) {
				JSONEncoder.writeString(out, new String((byte[]) val));
			} else if(clz.isArray()) {
				Object[] arr = null;
				try {
					arr = (Object[]) val;
				} catch(Throwable t) {}
				if(arr != null) {
					out.write((byte) '[');
					int i = 0;
					int size = arr.length;
					for(; i < size; i++) {
						JSONEncoder.writeObject(out, arr[i]);
						if(i != (size - 1)) {
							out.write((byte) ',');
						}
					}
					out.write((byte) ']');
				} else {
					JSONEncoder.writeString(out, val.toString());
				}
			} else if(val == Boolean.TRUE) {
				out.write(new byte[] { 't', 'r', 'u', 'e' });
			} else if(val == Boolean.FALSE) {
				out.write(new byte[] { 'f', 'a', 'l', 's', 'e' });
			} else if(val instanceof Number) {
				out.write(((Number) val).toString().getBytes());
			} else if(val instanceof Iterable<?>) {
				Iterable<?> col = (Iterable<?>) val;
				Iterator<?> it = col.iterator();
				out.write((byte) '[');
				if(it.hasNext()) {
					while(true) {
						JSONEncoder.writeObject(out, it.next());
						if(it.hasNext()) {
							out.write((byte) ',');
						} else {
							break;
						}
					}
				}
				out.write((byte) ']');
			} else if(val instanceof Map<?,?>) {
				Map<?,?> map = (Map<?,?>) val;
				Iterator<? extends Entry<?,?>> it = map.entrySet().iterator();
				out.write((byte) '{');
				if(it.hasNext()) {
					while(true) {
						Entry<?, ?> ent = it.next();
						JSONEncoder.writeObject(out, ent.getKey());
						out.write((byte) ':');
						JSONEncoder.writeObject(out, ent.getValue());
						if(it.hasNext()) {
							out.write((byte) ',');
						} else {
							break;
						}
					}
				}
				out.write((byte) '}');
			} else {
				JSONEncoder.writeString(out, val.toString());
			}
		}
	}
	
	
	public static byte[] toJsonString(Object val) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONEncoder.writeObject(baos, val);
			return baos.toByteArray();
		} catch(Throwable t) {
			Api.throv(t);
		}
		return null; //Unreach
	}
	
}
