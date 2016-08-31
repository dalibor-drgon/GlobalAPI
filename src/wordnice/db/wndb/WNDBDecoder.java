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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import wordnice.api.Nice;
import wordnice.db.ColType;
import wordnice.db.serialize.BadPrefixException;
import wordnice.db.serialize.BadTypeException;
import wordnice.db.serialize.SerializeException;
import wordnice.streams.IUtils;

public class WNDBDecoder { 
	
	public interface DecoderHandler {
		void decoderRequest(String[] names, ColType[] types, 
				List<Object[]> data, long nextId);
	}
	
	public static void readFileRawData(DecoderHandler hd, File f) 
			throws SerializeException, IOException {
		try(InputStream in = Nice.input(f)) {
			WNDBDecoder.readInputStreamRawData(hd, in);
		}
	}
	
	public static void readInputStreamRawData(DecoderHandler hd, InputStream in) 
			throws SerializeException, IOException {
		WNDBDecoder.readInputStreamRawData(hd, in, IUtils.readLong(in));
	}
	
	public static void readInputStreamRawData(DecoderHandler hd,InputStream in, long prefix) 
			throws SerializeException, IOException {
		if(prefix != WNDBEncoder.PREFIX && prefix != WNDBEncoder.PREFIX_2_4_9) {
			throw new BadPrefixException("Not WNDB format!");
		}

		long nextId = 1;
		int bt = IUtils.readInt(in);
		if(bt < 1) {
			throw new SerializeException("Invalid returned number of heads: " + bt);
		}
		if(prefix != WNDBEncoder.PREFIX_2_4_9) {
			nextId = IUtils.readLong(in);
		}

		String[] names = new String[bt];
		ColType[] types = new ColType[bt];
		int i = 0;
		for(i = 0; i < bt; i++) {
			names[i] = IUtils.deserializeUTF(in);
			types[i] = ColType.getByByte(IUtils.readByte(in));
		}
			
		int b = 0;
		i = 0;
		List<Object[]> data = new ArrayList<Object[]>();
		while(IUtils.readByte(in) == 1) {
			Object[] cur = new Object[bt];
			for(b = 0; b < bt; b++) {
				cur[b] = WNDBDecoder.deserializeKnownObject(in, types[b], i, b);
			}
			data.add(cur);
			i++;
		}
		
		if(prefix == WNDBEncoder.PREFIX_2_4_9) {
			nextId = i + 1;
		}
		hd.decoderRequest(names, types, data, nextId);
	}
	
	public static Object deserializeKnownObject(InputStream in, int type, int ri, int vi) 
			throws SerializeException, IOException {
		return WNDBDecoder.deserializeKnownObject(in, ColType.getByByte(type), ri, vi);
	}

	public static Object deserializeKnownObject(InputStream in, ColType typ, int ri, int vi) 
			throws SerializeException, IOException {
		switch(typ) {
			case BOOLEAN:
				return IUtils.readBoolean(in);
			case BYTE:
				return IUtils.readByte(in);
			case SHORT:
				return IUtils.readShort(in);
			case INT:
				return IUtils.readInt(in);
			case ID:
			case LONG:
				return IUtils.readLong(in);
			case FLOAT:
				return IUtils.readFloat(in);
			case DOUBLE:
				return IUtils.readDouble(in);
			case STRING:
				return IUtils.deserializeUTF(in);
			case BYTES:
				return IUtils.deserializeBytes(in);
			case ARRAY:
				return IUtils.deserializeColl(in);
			case MAP:
				return IUtils.deserializeMap(in);
		}
		throw new BadTypeException("Cannot read object at " + ri + ":" + vi + " - unsupported type " + typ);
	}
}
