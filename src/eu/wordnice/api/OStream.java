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

public class OStream extends OutputStream {

	public OutputStream out;

	public OStream() {
	}

	public OStream(OutputStream out) {
		this.out = out;
	}

	/*** SpecialWrite ***/

	public void writeBytes(byte[] bytes) throws IOException {
		this.writeInt(bytes.length);
		this.write(bytes);
	}

	public void writeString(String s) throws IOException {
		this.writeBytes(s.getBytes());
	}

	public void writeDouble(double value) throws IOException {
		this.writeLong(Double.doubleToLongBits(value));
	}

	public void writeFloat(float value) throws IOException {
		this.writeLong(Float.floatToIntBits(value));
	}

	public void writeLong(long value) throws IOException {
		/*
		this.write(new byte[] { (byte) (value >> 56), (byte) (value >> 48),
				(byte) (value >> 40), (byte) (value >> 32),
				(byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value });
		*/
		this.write(new byte[] { (byte) value, (byte) (value >> 8),
				(byte) (value >> 16), (byte) (value >> 24),
				(byte) (value >> 32), (byte) (value >> 40),
				(byte) (value >> 48), (byte) (value >> 56)});
	}
	
	public void writeuLong(long value) throws IOException {
		this.writeLong(value + Long.MIN_VALUE);
	}

	public void writeInt(int value) throws IOException {
		/*
		this.write(new byte[] { (byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value });
		*/
		this.write(new byte[] { (byte) value, (byte) (value >> 8),
				(byte) (value >> 16), (byte) (value >> 24)});
	}
	
	public void writeuInt(long value) throws IOException {
		this.writeInt((int) (value + Integer.MIN_VALUE));
	}

	public void writeShort(short value) throws IOException {
		this.write(new byte[] { (byte) value, (byte) (value >> 8)});
	}
	
	public void writeuShort(int value) throws IOException {
		this.writeShort((short) (value + Short.MIN_VALUE));
	}

	public void writeByte(byte value) throws IOException {
		this.write(value);
	}
	
	public void writeuByte(short value) throws IOException {
		this.writeByte((byte) (value + Byte.MIN_VALUE));
	}
	
	public void writeBoolean(boolean value) throws IOException {
		this.writeByte((byte) ((value == true) ? 1 : 0));
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
