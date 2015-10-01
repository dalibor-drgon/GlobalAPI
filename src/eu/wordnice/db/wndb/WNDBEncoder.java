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

package eu.wordnice.db.wndb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import eu.wordnice.db.ColType;
import eu.wordnice.db.serialize.BadTypeException;
import eu.wordnice.db.serialize.SerializeException;
import eu.wordnice.streams.Output;
import eu.wordnice.streams.OutputAdv;

public class WNDBEncoder {

	public static final long PREFIX_2_4_9 = 0xDEADCAFEBEEFBABEL;
	public static final long PREFIX = 0xDEEDCAFEBEEFBABEL;
	
	public static void writeFileData(File f, String[] names, ColType[] types, Iterable<Object[]> vals, long nextId) throws SerializeException, IOException {
		if(vals == null || f == null) {
			throw new NullPointerException("File or values are null!");
		}
		Output out = OutputAdv.forFile(f);
		WNDBEncoder.writeOutputStreamData(out, names, types, vals, nextId);
		out.close();
	}
	
	public static void writeOutputStreamData(Output out, String[] names, ColType[] types, Iterable<Object[]> vals, long nextId) throws SerializeException, IOException {
		if(out == null || vals == null || 
				names == null || types == null ||
						names.length == 0 || names.length != types.length) {
			throw new NullPointerException("File or values are null, or the lengths "
					+ "of names and types do not match!");
		}
		out.writeLong(WNDBEncoder.PREFIX);
		int sz = names.length;
		out.writeInt(sz);
		out.writeLong(nextId);
		
		for(int b = 0; b < sz; b++) {
			out.writeUTF(names[b]);
			out.writeByte(types[b].b);
		}
		
		Iterator<Object[]> it = vals.iterator();
		int i = 0;
		while(it.hasNext()) {
			Object[] cur = it.next();
			out.writeBoolean(true);
			int i2 = 0;
			for(; i2 < sz; i2++) {
				WNDBEncoder.writeObject(out, cur[i2], types[i2], i, i2);
			}
			i++;
		}
		out.writeBoolean(false);
	}
	
	/**
	 * @param ri Row number (index) - used for exception
	 * @param vi Column index - used for exception
	 */
	public static void writeObject(Output out, Object obj, Byte type, int ri, int vi) throws SerializeException, IOException {
		WNDBEncoder.writeObject(out, obj, ColType.getByByte(type), ri, vi);
	}

	public static void writeObject(Output out, Object obj, ColType typ, int ri, int vi) throws SerializeException, IOException {
		if(obj != null && !ColType.isAssignable(typ, obj)) {
			throw new BadTypeException("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - is not assignable for type " + typ.name());
		}
		switch(typ) {
			case BOOLEAN:
				if(obj == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean((Boolean) obj);
				}
				return;
				
			case BYTE:
				if(obj == null) {
					out.writeByte((byte) 0);
				} else {
					out.writeByte((Byte) obj);
				}
				return;
				
			case SHORT:
				if(obj == null) {
					out.writeShort((short) 0);
				} else {
					out.writeShort((Short) obj);
				}
				return;
				
			case INT:
				if(obj == null) {
					out.writeInt((int) 0);
				} else {
					out.writeInt((Integer) obj);
				}
				return;
				
			case ID:
			case LONG:
				if(obj == null) {
					out.writeLong((long) 0);
				} else {
					out.writeLong((Long) obj);
				}
				return;
				
			case FLOAT:
				if(obj == null) {
					out.writeFloat((float) 0);
				} else {
					out.writeFloat((Float) obj);
				}
				return;
				
			case DOUBLE:
				if(obj == null) {
					out.writeDouble((double) 0);
				} else {
					out.writeDouble((Double) obj);
				}
				return;
				
			case STRING:
				if(obj == null) {
					out.writeUTF((String) null);
				} else {
					out.writeUTF((String) obj.toString());
				}
				return;
				
			case BYTES:
				if(obj == null) {
					out.writeBytes((byte[]) null);
				} else {
					out.writeBytes((byte[]) obj);
				}
				return;
				
			case ARRAY:
				if(obj == null) {
					out.writeColl(null);
				} else if(obj.getClass().isArray()) {
					out.writeCollArray((Object[]) obj);
				} else {
					out.writeColl((Collection<?>) obj);
				}
				return;
				
			case MAP:
				if(obj == null) {
					out.writeMap(null);
				} else {
					out.writeMap((Map<?,?>) obj);
				}
				return;
		}
		throw new BadTypeException("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - unsupported type " + typ.name());
	}
	
}
