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

package eu.wordnice.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import eu.wordnice.api.serialize.WNSerializer;
import eu.wordnice.db.DBType;
import eu.wordnice.db.wndb.WNDBEncoder;

public class OStream extends OutputStream {

	public OutputStream out;

	public OStream() {}

	public OStream(OutputStream out) {
		this.out = out;
	}

	/*** SpecialWrite ***/

	public void writeBytes(byte[] bytes) throws IOException {
		if(bytes == null) {
			this.writeInt(-1);
			return;
		}
		this.writeInt(bytes.length);
		this.write(bytes);
	}

	public void writeString(String s) throws IOException {
		if(s == null) {
			this.writeInt(-1);
			return;
		}
		this.writeBytes(s.getBytes());
	}

	public void writeDouble(double value) throws IOException {
		this.writeLong(Double.doubleToLongBits(value));
	}

	public void writeFloat(float value) throws IOException {
		this.writeLong(Float.floatToIntBits(value));
	}

	public void writeLong(long value) throws IOException {
		this.write(new byte[] { (byte) value, (byte) (value >> 8),
				(byte) (value >> 16), (byte) (value >> 24),
				(byte) (value >> 32), (byte) (value >> 40),
				(byte) (value >> 48), (byte) (value >> 56)});
	}

	public void writeInt(int value) throws IOException {
		this.write(new byte[] { (byte) value, (byte) (value >> 8),
				(byte) (value >> 16), (byte) (value >> 24)});
	}

	public void writeShort(short value) throws IOException {
		this.write(new byte[] { (byte) value, (byte) (value >> 8)});
	}

	public void writeByte(byte value) throws IOException {
		this.write(value);
	}
	
	public void writeBoolean(boolean value) throws IOException {
		this.writeByte((byte) ((value == true) ? 1 : 0));
	}
	
	public void writeSet(Iterable<?> set) throws Exception {
		if(set == null) {
			this.writeInt(-1);
			return;
		}
		WNSerializer.coll2stream(this, set);
	}
	
	public void writeMap(Map<?,?> map) throws Exception {
		if(map == null) {
			this.writeInt(-1);
			return;
		}
		WNSerializer.map2stream(this, map);
	}
	
	public void writeObject(Object obj) throws Exception {
		this.writeObject(obj, -1, -1);
	}
	
	public void writeObject(Object obj, int ri, int vi) throws Exception {
		if(obj == null) {
			this.writeByte(DBType.BYTES.b);
			this.writeInt(-1);
		} else {
			DBType typ = DBType.getByObject(obj);
			this.writeByte(typ.b);
			WNDBEncoder.writeObject(this, obj, typ, ri, vi);
		}
	}
	

	/*** Override ***/

	@Override
	public void write(int obyte) throws IOException {
		this.out.write(obyte);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		this.out.write(bytes);
	}

	@Override
	public void write(byte[] bytes, int off, int l) throws IOException {
		this.out.write(bytes, off, l);
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}

}
