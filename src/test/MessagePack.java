/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
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

package test;

import java.util.Collection;
import java.util.Map;

import wordnice.api.Nice;

public class MessagePack {
	
	public static final int
		UNPACK_LIST_AS_ARRAY 	= 0x0001,
		UNPACK_MAP_AS_ARRAY 	= 0x0002,
		UNPACK_MAP_AS_LIST		= 0x0004;
		
	
	/*
	public static String test = 
		  "de 00 10 a7 63 6f 6d 70 61 63 74 c3 a6 73 63 68 65 6d 61 00 a8 63 6f 6d 70 61 63 74 32 c3 a7 73 63 68 65 6d 61 33 00 a8 63 6f 6d 70 34 61 63 74 c3 a7 73 63 68 34 65 6d 61 00 a9 63 6f 6d 70 61 34 63 74 32 c3 a8 73 63 68 65 6d 34 61 33 00 a8 63 6f 6d 32 70 61 63 74 c3 a7 73 63 68 33 65 6d 61 00 a9 63 6f 34 6d 70 61 63 74 32 c3 a8 73 63 35 68 65 6d 61 33 00 a9 63 6f 6d 36 70 34 61 63 74 c3 a8 73 63 68 34 37 65 6d 61 00 aa 63 6f 6d 39 70 61 34 63 74 32 c3 a9 73 63 68 39 65 6d 34 61 33 00";
	//test = "82 d9 20 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30 31 32 c3 a6 73 63 68 65 6d 61 00";
	
	public static byte[] parseHex(String str) {
		byte[] bytes = new byte[(str.length()+1)/3];
		for(int i = 0; i < bytes.length; i++) {
			char c1 = str.charAt(i*3);
			char c2 = str.charAt(i*3+1);
			bytes[i] = (byte)Integer.parseInt(str.substring(i*3,  i*3+2), 16);
		}
		return bytes;
	}
	
	public static void test(byte[] bytes) {
		System.out.println();
		System.out.println("------------ " + Arrays.toString(bytes) + " -----------");
		System.out.println("======== 64:");
		System.out.println(readS64(bytes, 0, 8));
		try { System.out.println(readU64(bytes, 0, 8)); } catch(Throwable t) {t.printStackTrace();}
		System.out.println("======== 32:");
		System.out.println(readS32(bytes, 0, 8));
		System.out.println(readU32(bytes, 0, 8));
		System.out.println("======== 16:");
		System.out.println(readS16(bytes, 0, 8));
		System.out.println(readU16(bytes, 0, 8));
		System.out.println("========  8:");
		System.out.println(readS8(bytes, 0, 8));
		System.out.println(readU8(bytes, 0, 8));
		System.out.println();
	}
	
	public static void main(String... strings) {
		byte[] arr = new byte[]{ (byte) 0xfe };
		arr = parseHex(test);
		System.out.println(Arrays.toString(arr));
		System.out.println(unpack(arr));
		
		test(new byte[]{0, -1, -1, -1, -1, -1, -1, -1});
		test(new byte[]{0x7f, -1, -1, -1, -1, -1, -1, -1});
		test(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1});
		
		byte[] bytes2 = {01, 00};
		System.out.println(readS16(bytes2, 0, 2));
		System.out.println(readU16(bytes2, 0, 2));
	}*/
	
	public static Object unpack(byte[] arr) {
		Value val = new Value(0);
		unpackRaw(val, arr, 0, arr.length);
		return val.value;
	}
	
	public static Object unpack(byte[] arr, int off, int len) {
		Value val = new Value(off);
		unpackRaw(val, arr, off, len);
		return val.value;
	}
	
	public static final class Value {
		public Object value;
		public int offset;
		
		public Value(int offset) {
			this.offset = offset;
		}
		
		public void set(Object value, int offset) {
			this.value = value;
			this.offset = offset;
		}
		
		public void inc(Object value, int length) {
			this.value = value;
			this.offset += length;
		}
	}

	@Deprecated
	public static final void unpackRaw(Value val, byte[] arr, int off, int end) {
		if(off >= end) {
			throw new IllegalArgumentException("Zero length, Nothing to deserialize");
		}
		int ch = arr[off] & 0xFF;
		switch(ch) {
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
        case 0x08: case 0x09: case 0x0a: case 0x0b: case 0x0c: case 0x0d: case 0x0e: case 0x0f:
        case 0x10: case 0x11: case 0x12: case 0x13: case 0x14: case 0x15: case 0x16: case 0x17:
        case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f:
        case 0x20: case 0x21: case 0x22: case 0x23: case 0x24: case 0x25: case 0x26: case 0x27:
        case 0x28: case 0x29: case 0x2a: case 0x2b: case 0x2c: case 0x2d: case 0x2e: case 0x2f:
        case 0x30: case 0x31: case 0x32: case 0x33: case 0x34: case 0x35: case 0x36: case 0x37:
        case 0x38: case 0x39: case 0x3a: case 0x3b: case 0x3c: case 0x3d: case 0x3e: case 0x3f:
        case 0x40: case 0x41: case 0x42: case 0x43: case 0x44: case 0x45: case 0x46: case 0x47:
        case 0x48: case 0x49: case 0x4a: case 0x4b: case 0x4c: case 0x4d: case 0x4e: case 0x4f:
        case 0x50: case 0x51: case 0x52: case 0x53: case 0x54: case 0x55: case 0x56: case 0x57:
        case 0x58: case 0x59: case 0x5a: case 0x5b: case 0x5c: case 0x5d: case 0x5e: case 0x5f:
        case 0x60: case 0x61: case 0x62: case 0x63: case 0x64: case 0x65: case 0x66: case 0x67:
        case 0x68: case 0x69: case 0x6a: case 0x6b: case 0x6c: case 0x6d: case 0x6e: case 0x6f:
        case 0x70: case 0x71: case 0x72: case 0x73: case 0x74: case 0x75: case 0x76: case 0x77:
        case 0x78: case 0x79: case 0x7a: case 0x7b: case 0x7c: case 0x7d: case 0x7e: case 0x7f:
        	val.inc(new Integer(ch), 1); return; //positive fixnum
        	
        case 0x80: case 0x81: case 0x82: case 0x83: case 0x84: case 0x85: case 0x86: case 0x87:
        case 0x88: case 0x89: case 0x8a: case 0x8b: case 0x8c: case 0x8d: case 0x8e: case 0x8f:
        	unpackMap(val, arr, off+1, end, ch-0x80); return; //fixmap
        	
        case 0x90: case 0x91: case 0x92: case 0x93: case 0x94: case 0x95: case 0x96: case 0x97:
        case 0x98: case 0x99: case 0x9a: case 0x9b: case 0x9c: case 0x9d: case 0x9e: case 0x9f:
        	unpackArray(val, arr, off+1, end, ch-0x90); return; // fixarray
        	
        case 0xa0: case 0xa1: case 0xa2: case 0xa3: case 0xa4: case 0xa5: case 0xa6: case 0xa7:
        case 0xa8: case 0xa9: case 0xaa: case 0xab: case 0xac: case 0xad: case 0xae: case 0xaf:
        case 0xb0: case 0xb1: case 0xb2: case 0xb3: case 0xb4: case 0xb5: case 0xb6: case 0xb7:
        case 0xb8: case 0xb9: case 0xba: case 0xbb: case 0xbc: case 0xbd: case 0xbe: case 0xbf:
        	unpackString(val, arr, off+1, end, ch-0xa0); return; // fixstr

        case 0xc0:  val.set(null, off+1); return;  // nil
        case 0xc2:  val.set(false, off+1); return;  // false
        case 0xc3:  val.set(true, off+1); return;  // true
        
        case 0xc4:  unpackBytes(val, arr, off+2, end, readU8(arr, off+1, end)); // bin 8
        case 0xc5:  unpackBytes(val, arr, off+3, end, readU16(arr, off+1, end)); // bin 16
        case 0xc6:  unpackBytes(val, arr, off+5, end, readU32L(arr, off+1, end)); // bin 32
        	
        case 0xc7:  //getDDItem1(CWP_ITEM_EXT, ext.length, uint8_t);              // ext 8
                    //cw_unpack_assert_space(1);
                    //unpack_context->item.type = *(int8_t*)p;
                    //cw_unpack_assert_blob(ext);
        case 0xc8:  //getDDItem2(CWP_ITEM_EXT, ext.length, uint16_t);             // ext 16
                    //cw_unpack_assert_space(1);
                    //unpack_context->item.type = *(int8_t*)p;
                    //cw_unpack_assert_blob(ext);
        case 0xc9:  //getDDItem4(CWP_ITEM_EXT, ext.length, uint32_t);             // ext 32
                    //cw_unpack_assert_space(1);
                    //unpack_context->item.type = *(int8_t*)p;
                    //cw_unpack_assert_blob(ext);
        	
        case 0xca:  val.inc(Float.intBitsToFloat(readS32(arr, off+1, end)), 5); return; // float
        case 0xcb:  val.inc(Double.longBitsToDouble(readS64(arr, off+1, end)), 9); return; // double
        	 
        case 0xcc:  val.inc(readU8(arr, off+1, end), 2); return; // unsigned int  8
        case 0xcd:  val.inc(readU16(arr, off+1, end), 3); return; // unsigned int  16
        case 0xce:  val.inc(readU32(arr, off+1, end), 5); return; // unsigned int  32
        case 0xcf:  val.inc(readU64(arr, off+1, end), 9); return; // unsigned int  64
        
        case 0xd0:  val.inc(readS8(arr, off+1, end), 2); return; // signed int  8
        case 0xd1:  val.inc(readS16(arr, off+1, end), 3); return; // signed int  16
        case 0xd2:  val.inc(readS32(arr, off+1, end), 5); return; // signed int  32
        case 0xd3:  val.inc(readS64(arr, off+1, end), 9); return; // signed int  64
        	
        case 0xd4:  //getDDItemFix(1);                                            // fixext 1
        case 0xd5:  //getDDItemFix(2);                                            // fixext 2
        case 0xd6:  //getDDItemFix(4);                                            // fixext 4
        case 0xd7:  //getDDItemFix(8);                                            // fixext 8
        case 0xd8:  //getDDItemFix(16);                                           // fixext 16
        
        case 0xd9:  unpackString(val, arr, off+2, end, readU8(arr, off+1, end)); return; // str 8
        case 0xda:  unpackString(val, arr, off+3, end, readU16(arr, off+1, end)); return; // str 16
        case 0xdb:  unpackString(val, arr, off+5, end, readU32L(arr, off+1, end)); return; // str 32
        
        case 0xdc:  unpackArray(val, arr, off+3, end, readU16(arr, off+1, end)); return;  // array 16
        case 0xdd:  unpackArray(val, arr, off+5, end, readU32L(arr, off+1, end)); return;  // array 32
        
        case 0xde:  unpackMap(val, arr, off+3, end, readU16(arr, off+1, end)); return;  // map 16
        case 0xdf:  unpackMap(val, arr, off+5, end, readU32L(arr, off+1, end)); return;  // map 32
        
        case 0xe0: case 0xe1: case 0xe2: case 0xe3: case 0xe4: case 0xe5: case 0xe6: case 0xe7:
        case 0xe8: case 0xe9: case 0xea: case 0xeb: case 0xec: case 0xed: case 0xee: case 0xef:
        case 0xf0: case 0xf1: case 0xf2: case 0xf3: case 0xf4: case 0xf5: case 0xf6: case 0xf7:
        case 0xf8: case 0xf9: case 0xfa: case 0xfb: case 0xfc: case 0xfd: case 0xfe: case 0xff:
        	val.inc(new Integer((byte) ch), 1); return;    // negative fixnum
        default:
                    //unreachable
		}
		throw new IllegalArgumentException("Unexpected to be here");
	}

	private static void unpackArray(Value val, byte[] arr, int off, int end, int sz) {
		Collection<Object> output = Nice.createList(sz);
		while(sz-- != 0) {
			unpackRaw(val, arr, off, end);
			off = val.offset;
			Object value = val.value;
			
			output.add(value);
		}
		val.set(output, off);
		return;
	}

	private static void unpackMap(Value val, byte[] arr, int off, int end, int sz) {
		Map<Object,Object> output = Nice.createMap(sz);
		while(sz-- != 0) {
			unpackRaw(val, arr, off, end);
			off = val.offset;
			Object name = val.value;
			
			unpackRaw(val, arr, off, end);
			off = val.offset;
			Object value = val.value;
			
			output.put(name, value);
		}
		val.set(output, off);
		return;
	}
	
	private static void unpackString(Value val, byte[] arr, int off, int end, int sz) {
		//System.out.println(arr.length + " " + off + " " + end + " " + sz);
		int nevoff = off + sz;
		if(nevoff > end) {
			throw new IllegalArgumentException("String is bigger than input.");
		}
		val.set(new String(arr, off, sz), nevoff);
		return;
	}
	
	private static void unpackBytes(Value val, byte[] arr, int off, int end, int sz) {
		int nevoff = off + sz;
		if(nevoff > end) {
			throw new IllegalArgumentException("String is bigger than input.");
		}
		byte[] output = new byte[sz];
		System.arraycopy(arr, off, output, 0, sz);
		val.set(output, nevoff);
		return;
	}
	
	private static int readS8(byte[] arr, int off, int end) {
		checkSpace(off, end, 1);
		return arr[off+0];
	}
	
	private static int readS16(byte[] arr, int off, int end) {
		checkSpace(off, end, 2);
		return (short) ((arr[off+0]&0xFF) << 8) | ((arr[off+1]&0xFF) << 0) << 16 >>> 16;
	}
	
	//Big Endian
	private static int readS32(byte[] arr, int off, int end) {
		checkSpace(off, end, 4);
		return (((int) arr[off+0]&0xFF) << 24) | (((int) arr[off+1]&0xFF) << 16) 
			| (((int) arr[off+2]&0xFF) << 8) | (((int) arr[off+3]&0xFF) << 0);
	}
	
	private static long readS64(byte[] arr, int off, int end) {
		checkSpace(off, end, 8);
		return (((long) arr[off+0]&0xFFL) << 56) | (((long) arr[off+1]&0xFFL) << 48) 
			| (((long) arr[off+2]&0xFFL) << 40) | (((long) arr[off+3]&0xFFL) << 32)
			| (((long) arr[off+4]&0xFFL) << 24) | (((long) arr[off+5]&0xFFL) << 16) 
			| (((long) arr[off+6]&0xFFL) << 8) | (((long) arr[off+7]&0xFFL) << 0);
	}
	
	private static int readU8(byte[] arr, int off, int end) {
		checkSpace(off, end, 1);
		return arr[off] & 0xFF;
	}
	
	private static int readU16(byte[] arr, int off, int end) {
		checkSpace(off, end, 2);
		return (((int) arr[off+0]&0xFF) << 8) | (((int) arr[off+1]&0xFF) << 0);
	}
	
	//Big Endian
	private static long readU32(byte[] arr, int off, int end) {
		checkSpace(off, end, 4);
		return (((long) arr[off+0]&0xFFL) << 24) | (((long) arr[off+1]&0xFFL) << 16) 
			| (((long) arr[off+2]&0xFFL) << 8) | (((long) arr[off+3]&0xFFL) << 0);
	}
	
	private static int readU32L(byte[] arr, int off, int end) {
		checkSpace(off, end, 4);
		int val = (((int) arr[off+0]&0xFF) << 24) | (((int) arr[off+1]&0xFF) << 16) 
				| (((int) arr[off+2]&0xFF) << 8) | (((int) arr[off+3]&0xFF) << 0);
		if(val < 0) {
			throw new IllegalArgumentException("Integer overflow");
		}
		return val;
	}
	
	private static long readU64(byte[] arr, int off, int end) {
		checkSpace(off, end, 8);
		long val = (((long) arr[off+0]&0xFFL) << 56) | (((long) arr[off+1]&0xFFL) << 48) 
				| (((long) arr[off+2]&0xFFL) << 40) | (((long) arr[off+3]&0xFFL) << 32)
				| (((long) arr[off+4]&0xFFL) << 24) | (((long) arr[off+5]&0xFFL) << 16) 
				| (((long) arr[off+6]&0xFFL) << 8) | (((long) arr[off+7]&0xFFL) << 0);
		if(val < 0) throw new IllegalArgumentException("Integer overflow");
		return val;
	}
	
	private static void checkSpace(int off, int end, int space) {
		if(off + space > end) {
			throw new IllegalArgumentException("No space. Required to read " + space + " bytes.");
		}
	}
	
}
