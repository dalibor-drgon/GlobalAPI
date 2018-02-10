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

package wordnice.db.wndb;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.db.ColType;
import wordnice.db.serialize.BadTypeException;
import wordnice.db.serialize.SerializeException;
import wordnice.streams.OUtils;

public class WNDBEncoder {

	public static final long PREFIX_2_4_9 = 0xDEADCAFEBEEFBABEL;
	public static final long PREFIX = 0xDEEDCAFEBEEFBABEL;
	
	public static void writeFileData(File f, String[] names, ColType[] types, Iterable<Object[]> vals, long nextId) throws SerializeException, IOException {
		if(vals == null || f == null) {
			throw Nice.illegal("File or values are null!");
		}
		try(OutputStream out = Nice.output(f)) {
			WNDBEncoder.writeOutputStreamData(out, names, types, vals, nextId);
		}
	}
	
	public static void writeOutputStreamData(OutputStream out, String[] names, ColType[] types, Iterable<Object[]> vals, long nextId) throws SerializeException, IOException {
		if(out == null || vals == null || 
				names == null || types == null ||
						names.length == 0 || names.length != types.length) {
			throw Nice.illegal("File or values are null, or the lengths "
					+ "of names and types do not match!");
		}
		OUtils.writeLong(out, WNDBEncoder.PREFIX);
		int sz = names.length;
		OUtils.writeInt(out, sz);
		OUtils.writeLong(out,nextId);
		
		for(int b = 0; b < sz; b++) {
			OUtils.serializeUTF(out, names[b]);
			OUtils.writeByte(out, types[b].b);
		}
		
		Iterator<Object[]> it = vals.iterator();
		int i = 0;
		while(it.hasNext()) {
			Object[] cur = it.next();
			OUtils.writeBoolean(out, true);
			int i2 = 0;
			for(; i2 < sz; i2++) {
				WNDBEncoder.serializeKnownObject(out, cur[i2], types[i2], i, i2);
			}
			i++;
		}
		OUtils.writeBoolean(out, false);
	}
	
	/**
	 * @param ri Row number (index) - used for exception
	 * @param vi Column index - used for exception
	 */
	public static void serializeKnownObject(OutputStream out, Object obj, Byte type, int ri, int vi) throws SerializeException, IOException {
		WNDBEncoder.serializeKnownObject(out, obj, ColType.getByByte(type), ri, vi);
	}

	public static void serializeKnownObject(OutputStream out, Object obj, ColType typ, int ri, int vi) throws SerializeException, IOException {
		if(obj != null && !ColType.isAssignable(typ, obj)) {
			throw new BadTypeException("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - is not assignable for type " + typ.name());
		}
		switch(typ) {
			case BOOLEAN:
				if(obj == null) {
					OUtils.writeBoolean(out, false);
				} else {
					OUtils.writeBoolean(out, (Boolean) obj);
				}
				return;
				
			case BYTE:
				if(obj == null) {
					OUtils.writeByte(out, (byte) 0);
				} else {
					OUtils.writeByte(out, (Byte) obj);
				}
				return;
				
			case SHORT:
				if(obj == null) {
					OUtils.writeShort(out, (short) 0);
				} else {
					OUtils.writeShort(out, (Short) obj);
				}
				return;
				
			case INT:
				if(obj == null) {
					OUtils.writeInt(out, (int) 0);
				} else {
					OUtils.writeInt(out, (Integer) obj);
				}
				return;
				
			case ID:
			case LONG:
				if(obj == null) {
					OUtils.writeLong(out, (long) 0);
				} else {
					OUtils.writeLong(out, (Long) obj);
				}
				return;
				
			case FLOAT:
				if(obj == null) {
					OUtils.writeFloat(out, (float) 0);
				} else {
					OUtils.writeFloat(out, (Float) obj);
				}
				return;
				
			case DOUBLE:
				if(obj == null) {
					OUtils.writeDouble(out, (double) 0);
				} else {
					OUtils.writeDouble(out, (Double) obj);
				}
				return;
				
			case STRING:
				if(obj == null) {
					OUtils.serializeUTF(out, (String) null);
				} else {
					OUtils.serializeUTF(out, (String) obj.toString());
				}
				return;
				
			case BYTES:
				if(obj == null) {
					OUtils.serializeBytes(out, (byte[]) null);
				} else {
					OUtils.serializeBytes(out, (byte[]) obj);
				}
				return;
				
			case ARRAY:
				if(obj == null) {
					OUtils.serializeColl(out, null);
				} else if(obj.getClass().isArray()) {
					OUtils.serializeCollArray(out, (Object[]) obj);
				} else {
					OUtils.serializeColl(out, (Collection<?>) obj);
				}
				return;
				
			case MAP:
				if(obj == null) {
					OUtils.serializeMap(out, null);
				} else {
					OUtils.serializeMap(out, (Map<?,?>) obj);
				}
				return;
		}
		throw new BadTypeException("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - unsupported type " + typ.name());
	}
	
}
