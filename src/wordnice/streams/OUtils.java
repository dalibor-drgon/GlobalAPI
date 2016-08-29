/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of out software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and out permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import wordnice.api.Api;
import wordnice.db.ColType;
import wordnice.db.serialize.CollSerializer;
import wordnice.db.serialize.SerializeException;
import wordnice.db.wndb.WNDBEncoder;
import wordnice.utils.NiceStringUtils;

public class OUtils {
	
	public static void writeBoolean(OutputStream out, boolean v) throws IOException {
		out.write(v ? 1 : 0);
	}
	
	public static void writeByte(OutputStream out, int v) throws IOException {
		out.write(v);
	}
	
	public static void writeShort(OutputStream out, int v) throws IOException {
		out.write((v >>> 0) & 0xFF);
		out.write((v >>> 8) & 0xFF);
	}
	
	public static void writeChar(OutputStream out, int v) throws IOException {
		out.write((v >>> 0) & 0xFF);
		out.write((v >>> 8) & 0xFF);
	}
	
	public static void writeInt(OutputStream out, int v) throws IOException {
		out.write((v >>>  0) & 0xFF);
		out.write((v >>>  8) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 24) & 0xFF);
	}
	
	public static void writeLong(OutputStream out, long v) throws IOException {
		out.write((int) ((v >>  0) & 0xFF));
		out.write((int) ((v >>  8) & 0xFF));
		out.write((int) ((v >> 16) & 0xFF));
		out.write((int) ((v >> 24) & 0xFF));
		out.write((int) ((v >> 32) & 0xFF));
		out.write((int) ((v >> 40) & 0xFF));
		out.write((int) ((v >> 48) & 0xFF));
		out.write((int) ((v >> 56) & 0xFF));
	}

	public static void writeFloat(OutputStream out, float v) throws IOException {
		writeInt(out, Float.floatToIntBits(v));
	}
	
	public static void writeDouble(OutputStream out, double v) throws IOException {
		writeLong(out, Double.doubleToLongBits(v));
	}
	
	public static void serializeBytes(OutputStream out, byte[] bytes) throws IOException {
		if(bytes == null) {
			writeInt(out, -1);
			return;
		}
		int len = bytes.length;
		writeInt(out, len);
		out.write(bytes);
	}
	
	public static void serializeBytes(OutputStream out, byte[] bytes, int off, int len) throws IOException {
		if(bytes == null || len < 0) {
			writeInt(out, -1);
			return;
		}
		writeInt(out, len);
		out.write(bytes, off, len);
	}
	
	public static void writeASCIIBytes(OutputStream out, CharSequence str) throws IOException {
		writeASCIIBytes(out, str, 0, str.length());
	}
	
	public static void writeASCIIBytes(OutputStream out, CharSequence str, int off, int len) throws IOException {
		int n = str.length();
		byte[] buff = new byte[Math.min(8192, n)];
		int strI = 0;
		while(strI < n) {
			int curWrite = Math.min(buff.length, n);
			for(int i = 0; i < curWrite;) {
				buff[i++] = (byte) str.charAt(strI++);
			}
			out.write(buff, 0, curWrite);
		}
	}
	
	public static void serializeUTF(OutputStream out, CharSequence str) throws IOException {
		if(str == null) {
			writeInt(out, -1);
			return;
		}
		int len = str.length();
		writeInt(out, len);
		writeChars(out, str, 0, len);
	}
	
	public static void serializeUTF(OutputStream out, CharSequence str, int off, int len) throws IOException {
		if(str == null || len < 0) {
			writeInt(out, -1);
			return;
		}
		writeInt(out, len);
		writeChars(out, str, off, len);
	}
	
	public static void serializeUTF(OutputStream out, char[] chars) throws IOException {
		if(chars == null) {
			writeInt(out, -1);
			return;
		}
		int len = chars.length;
		writeInt(out, len);
		writeChars(out, chars, 0, len);
	}
	
	public static void serializeUTF(OutputStream out, char[] chars, int off, int clen) throws IOException {
		if(chars == null || clen < 0) {
			writeInt(out, -1);
			return;
		}
		writeInt(out, clen);
		writeChars(out, chars, off, clen);
	}
	
	public static void writeChars(OutputStream out, char[] chars) throws IOException {
		writeChars(out, chars, 0, chars.length);
	}
	
	public static void writeChars(OutputStream out, char[] chars, int off, int clen) throws IOException {
		int len = clen * 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		byte[] buff = new byte[Math.min(8192, len)];
		clen += off;
		while(off < clen) {
			int curChars = Math.min((clen - off) * 2, buff.length);
			for(int i = 0; i < curChars; off++) {
				char cur = chars[off];
				buff[i++] = (byte) (cur >> 0);
				buff[i++] = (byte) (cur >> 8);
			}
			out.write(buff, 0, curChars);
		}
	}
	
	public static void writeChars(OutputStream out, CharSequence str) throws IOException {
		writeChars(out, str, 0, str.length());
	}
	
	public static void writeChars(OutputStream out, CharSequence str, int off, int clen) throws IOException {
		int len = clen * 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		byte[] buff = new byte[Math.min(8192, len)];
		clen += off;
		while(off < clen) {
			int curChars = Math.min((clen - off) * 2, buff.length);
			for(int i = 0; i < curChars; off++) {
				char cur = str.charAt(off);
				buff[i++] = (byte) (cur & 0xFF);
				buff[i++] = (byte) (cur >> 8);
			}
			out.write(buff, 0, curChars);
		}
	}
	
	
	public static void serializeColl(OutputStream out, Collection<?> set) throws SerializeException, IOException {
		if(set == null) {
			writeInt(out, -1);
			return;
		}
		serializeColl(out, set.iterator(), set.size());
	}
	
	public static void serializeColl(OutputStream out, Iterator<?> it, int size) throws SerializeException, IOException {
		CollSerializer.collToStream(out, it, size);
	}
	
	public static void serializeMap(OutputStream out, Map<? extends Object, ? extends Object> map) throws SerializeException, IOException {
		if(map == null) {
			writeInt(out, -1);
			return;
		}
		CollSerializer.mapToStream(out, map);
	}
	
	public static void serializeCollArray(OutputStream out, Object[] arr) throws SerializeException, IOException {
		if(arr == null) {
			writeInt(out, -1);
			return;
		}
		CollSerializer.arrayToStream(out, arr, 0, arr.length);
	}
	
	public static void serializeCollArray(OutputStream out, Object[] arr, int off, int len) throws SerializeException, IOException {
		if(arr == null || len < 0) {
			writeInt(out, -1);
			return;
		}
		CollSerializer.arrayToStream(out, arr, off, len);
	}
	
	public static void serializeKnownObject(OutputStream out, Object obj) throws SerializeException, IOException {
		serializeKnownObject(out, obj, -1, -1);
	}
	
	public static void serializeKnownObject(OutputStream out, Object obj, int ri, int vi) throws SerializeException, IOException {
		if(obj == null) {
			writeByte(out, ColType.BYTES.b);
			writeInt(out, -1);
		} else {
			ColType typ = ColType.getByObject(obj);
			writeByte(out, typ.b);
			WNDBEncoder.serializeKnownObject(out, obj, typ, ri, vi);
		}
	}
	
	public static void append(OutputStream out, CharSequence csq) throws IOException {
		write(out, csq, 0, csq.length());
	}
	
	public static void append(OutputStream out, CharSequence csq, int start, int end) throws IOException {
		write(out, csq, start, end-start);
	}
	
	public static void write(OutputStream out, char[] cbuf) throws IOException {
		write(out, cbuf, 0, cbuf.length);
	}
	public static void write(OutputStream out, char[] cbuf, int off, int len) throws IOException {
		Api.checkBounds(cbuf, off, len);
		NiceStringUtils.toBytes(out,cbuf, off, len);
	}
	
	public static void write(OutputStream out, Object obj) throws IOException {
		if(obj instanceof byte[]) {
			out.write((byte[]) obj);
			return;
		} else if(obj instanceof char[]) {
			write(out, (char[]) obj);
			return;
		} else if(obj instanceof CharSequence) {
			write(out, (CharSequence) obj);
			return;
		}
		write(out, (obj == null) ? "null" : obj.toString());
	}
	
	public static void write(OutputStream out, char c) throws IOException {
		write(out, new char[] {c});
	}
	
	public static void write(OutputStream out, CharSequence str) throws IOException {
		write(out, str, 0, str.length());
	}
	
	public static void write(OutputStream out, CharSequence str, int off, int len) throws IOException {
		Api.checkBounds(str, off, len);
		NiceStringUtils.toBytes(out, str, off, len);
	}
	
	public static void writeln(OutputStream out) throws IOException {
		out.write(Api.LINE_SEPARATOR_BYTES);
	}
	
	public static void writeln(OutputStream out, Object obj) throws IOException {
		write(out, obj);
		writeln(out);
	}
	
	public static void writeln(OutputStream out, char c) throws IOException {
		write(out, c);
		writeln(out);
	}
	public static void writeln(OutputStream out, CharSequence str) throws IOException {
		write(out, str);
		writeln(out);
	}
	public static void writeln(OutputStream out, CharSequence str, int off, int len) throws IOException {
		write(out, str, off, len);
		writeln(out);
	}
	
}
