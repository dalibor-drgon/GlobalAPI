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

package wordnice.http.websocket;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import wordnice.api.Nice;
import wordnice.seq.ByteArraySequence;
import wordnice.streams.ArrayOutputStream;
import wordnice.streams.IUtils;

/**
 * WebSocket internal methods & utilities
 * Made to be compatible with RFC 6455
 */
public class WebSockets {
	
	/**
	 * RFC 6455:
	 *  0x0 denotes a continuation frame
	 *  0x1 denotes a text frame
	 *  0x2 denotes a binary frame
	 *  0x3-7 are reserved for further non-control frames
	 *  0x8 denotes a connection close
	 *  0x9 denotes a ping
	 *  0xA denotes a pong
	 *  0xB-F are reserved for further control frames
      **/
	
	//Opcodes as string
	protected static final String[] OpcodeNames = new String[] {
		"Continuation", "Text", "Binary", null, 
		null, 	null, 	null, 	null, //future non-control frames
		
		"Close", "Ping", "Pong", null,
		null, 	null, 	null, 	null //future control frames
	};
	
	//Opcodes
	public static final int
		OContinue = 0x0,
		OText = 0x1,
		OBin = 0x2,
		
		OClose = 0x8,
		OPing = 0x9,
		OPong = 0xA;
	
	//First byte flags
	public static final int
		FOpcode = 0xF,
		FRSV3 = 0x10,
		FRSV2 = 0x20,
		FRSV1 = 0x40,
		FFin = 0x80;
	
	//Second byte (length start) mask
	public static final int
		LMask = 0x80;
	
	public static final boolean hasFlag(int value, int flag) {
		return (value & flag) == flag;
	}
	
	public static final byte getOpcode(int value) {
		return (byte) (value & FOpcode);
	}
	
	public static final byte getRSV(int value) {
		return (byte) ((value>>4) & 0x7);
	}
	
	public static String getOpcodeName(int value) {
		return OpcodeNames[value & FOpcode];
	}

	public static final int lengthLength(int len) {
		if(len < 126) return 1;
		if(len < 0x10000) return 3;
		return 9;
	}
	
	public static byte[] createCompleteUnmaskedFrame(byte[] rawData) throws IOException {
		if(rawData == null) throw new IllegalArgumentException("Input bytes == null");
		return createUnmaskedFrame(rawData, 0, rawData.length, 129);
	}
	
	public static byte[] createCompleteUnmaskedFrame(byte[] rawData, int off, int len) throws IOException {
		return createUnmaskedFrame(rawData, off, len, 129);
	}
	
	public static byte[] createUnmaskedFrame(byte[] rawData, int flags) throws IOException {
		if(rawData == null) throw new IllegalArgumentException("Input bytes == null");
		return createUnmaskedFrame(rawData, 0, rawData.length, flags);
	}
	
	public static byte[] createUnmaskedFrame(byte[] rawData, int off, int len, int flags) throws IOException {
		Nice.checkBounds(rawData, off, len);
		return createUnmaskedFrame(new byte[len+lengthLength(len)+1], rawData, off, len, flags);
	}
	
	protected static byte[] createUnmaskedFrame(byte[] output, byte[] rawData, int off, int len, int flags) throws IOException {
	    output[0] = (byte) (flags & 0xFF);
	    int offset = 0;
	    if(len < 126){
	        output[1] = (byte) len;
	        offset = 2;
	    } else if(len < 65536){
	    	output[1] = (byte) 126;
	        output[2] = (byte) ((len >> 8) & 0xFF);
	        output[3] = (byte) (len & 0xFF); 
	        offset = 4;
	    } else{
	    	output[1] = (byte) 127;
	        output[2] = (byte) ((len >>> 56) & 0xFF);
	        output[3] = (byte) ((len >> 48) & 0xFF);
	        output[4] = (byte) ((len >> 40) & 0xFF);
	        output[5] = (byte) ((len >> 32) & 0xFF);
	        output[6] = (byte) ((len >> 24) & 0xFF);
	        output[7] = (byte) ((len >> 16) & 0xFF);
	        output[8] = (byte) ((len >> 8) & 0xFF);
	        output[9] = (byte) (len & 0xFF);
	        offset = 10;
	    }
	    int i = off;
	    int end = off + len;
	    for(; i < end; i++, offset++)
	        output[offset] = rawData[i];
	    return output;
	}
         
	protected static long readLong(InputStream in) throws IOException {
		long b1 = in.read();
		long b2 = in.read();
		long b3 = in.read();
		long b4 = in.read();
		long b5 = in.read();
		long b6 = in.read();
		long b7 = in.read();
		long b8 = in.read();
		if(b8 == -1) throw new EOFException();
		return (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
	}
	
	public static byte[] readMessage(InputStream in, int limit, boolean forceMask, byte[] mask) 
			throws IOException, WebSocketException {
		int len = in.read();
		if(len == -1) throw new EOFException();
		boolean hasMask = (len & 0x80) == 0x80;
		len = len & 0x7F;
		long longlen = len;
		if(forceMask && !hasMask) {
			throw new WebSocketException("Mask missing but required!");
		}
		
		if(len == 126) {
			int b1 = in.read();
			int b2 = in.read();
			if(b2 == -1) throw new EOFException();
			longlen = (b1 << 8) | b2;
		} else if(len == 127) {
			longlen = readLong(in);
			if(longlen < 0) throw new WebSocketException("Negative 64bit size!");
		}
		if((longlen > limit && limit != 0)
			|| longlen > Nice.MaxArrayLength) {
			throw new WebSocketException("Limit reached");
			//in.skip(longlen + ((hasMask) ? 4:0));
			//return null;
		}
		len = (int) longlen;
		if(hasMask) {
			if(mask == null) mask = new byte[4];
			IUtils.readFully(in, mask, 0, 4);
		}
		byte[] output = new byte[len];
		IUtils.readFully(in, output);
		
		if(hasMask) {
			int i = 0;
			for(; i < output.length; i++) {
				output[i] = (byte) ((output[i] ^ mask[i&3]) & 0xFF);
			}
		}
		return output;
    }

	public static ArrayOutputStream readMessage(InputStream in, ArrayOutputStream out, int limit, boolean forceMask, byte[] mask)
			throws IOException, WebSocketException {
		if(out == null) out = Nice.createArrayOutput();
		int len = in.read();
		if(len == -1) throw new EOFException();
		boolean hasMask = (len & 0x80) == 0x80;
		len = len & 0x7F;
		long longlen = len;
		if(forceMask && !hasMask) {
			throw new WebSocketException("Mask missing but required!");
		}
		
		if(len == 126) {
			int b1 = in.read();
			int b2 = in.read();
			if(b2 == -1) throw new EOFException();
			longlen = (b1 << 8) | b2;
		} else if(len == 127) {
			longlen = readLong(in);
			if(longlen < 0) throw new WebSocketException("Negative 64bit size!");
		}
		if(((longlen > limit || (longlen + out.size()) > limit) && limit != 0)
				|| longlen > Nice.MaxArrayLength) {
			throw new WebSocketException("Limit reached");
			//in.skip(longlen + ((hasMask) ? 4:0));
			//return null;
		}
		len = (int) longlen;
		if(hasMask) {
			if(mask == null) mask = new byte[4];
			IUtils.readFully(in, mask, 0, 4);
		}
		//TODO
		byte[] output = new byte[len];
		IUtils.readFully(in, output);
		
		if(hasMask) {
			int i = 0;
			for(; i < output.length; i++) {
				output[i] = (byte) ((output[i] ^ mask[i&3]) & 0xFF);
			}
		}
		out.write(output);
		return out;
	}
	
	public static void sendControlFrame(OutputStream out, int flag)
			throws IOException {
		sendFrame(out, flag, null, null, 0, 0);
	}
	
	public static void sendControlFrame(OutputStream out, int flag, byte[] mask, byte[] content)
			throws IOException {
		sendControlFrame(out, flag, mask, content, 0, (content == null) ? 0 : content.length);
	}
	
	public static void sendControlFrame(OutputStream out, int flag, byte[] mask, byte[] content, int off, int conLen)
			throws IOException {
		if(conLen > 125) throw new IllegalArgumentException("Control message with length over 125!");
		sendFrame(out, flag, mask, content, off, conLen);
	}
	
	public static void sendFrame(OutputStream out, int flag, byte[] mask, byte[] content)
			throws IOException {
		sendFrame(out, flag, mask, content, 0, (content == null) ? 0 : content.length);
	}
	
	public static void sendFrame(OutputStream out, int flags, byte[] mask, byte[] content, int off, int conLen)
			throws IOException {
		Nice.checkBoundsNull(content, off, conLen);
		if(conLen == 0) {
			content = null;
			mask = null;
		} else if(content != null && mask != null && mask.length < 4) {
			throw new IllegalArgumentException("Mask length too short");
		}
		byte[] output = new byte[lengthLength(conLen)+1];
		output[0] = (byte) (flags & 0xFF);
		int maskFlag = (mask == null) ? 0 : LMask;
	    if(conLen < 126){
	        output[1] = (byte) (conLen | maskFlag);
	    } else if(conLen < 65536){
	    	output[1] = (byte) (126 | maskFlag);
	        output[2] = (byte) ((conLen >> 8) & 0xFF);
	        output[3] = (byte) (conLen & 0xFF); 
	    } else{
	    	output[1] = (byte) (127 | maskFlag);
	        output[2] = (byte) ((conLen >>> 56) & 0xFF);
	        output[3] = (byte) ((conLen >> 48) & 0xFF);
	        output[4] = (byte) ((conLen >> 40) & 0xFF);
	        output[5] = (byte) ((conLen >> 32) & 0xFF);
	        output[6] = (byte) ((conLen >> 24) & 0xFF);
	        output[7] = (byte) ((conLen >> 16) & 0xFF);
	        output[8] = (byte) ((conLen >> 8) & 0xFF);
	        output[9] = (byte) (conLen & 0xFF);
	    }
	    out.write(output);
		if(content != null) {
			if(mask != null) {
				int i = 0;
				for(; i < conLen; i++) {
					content[i+off] = (byte) ((content[i] ^ mask[i&3]) & 0xFF);
				}
				out.write(mask, 0, 4);
			}
			out.write(content);
		}
		out.flush();
	}

	public static byte[] readControlMessage(InputStream in, boolean forceMask, byte[] mask) 
			throws IOException, WebSocketException {
		int len = in.read();
		if(len == -1) throw new EOFException();
		boolean hasMask = (len & LMask) == LMask;
		len = len & 0x7F;
		if(forceMask && !hasMask) {
			throw new WebSocketException("Mask missing but required!");
		}
		
		if(len > 125)
			throw new WebSocketException("Control message with length over 125!");
		if(hasMask) {
			if(mask == null) mask = new byte[4];
			IUtils.readFully(in, mask, 0, 4);
		}
		byte[] output = new byte[len];
		IUtils.readFully(in, output);
		
		if(hasMask) {
			int i = 0;
			for(; i < output.length; i++) {
				output[i] = (byte) ((output[i] ^ mask[i&3]) & 0xFF);
			}
		}
		return output;
    }
	
	public static byte[] createUnmaskedFrame(Iterable<ByteArraySequence> seq, int flags) throws IOException {
		if(seq == null) throw new IllegalArgumentException("Iterable<Sequence> == null");
		int len = 0;
		Iterator<ByteArraySequence> it = seq.iterator();
		while(it.hasNext()) {
			len += it.next().length();
		}
		return createUnmaskedFrame(new byte[len+lengthLength(len)+1], seq, len, flags);
	}
	
	protected static byte[] createUnmaskedFrame(byte[] output, Iterable<ByteArraySequence> seq, int len, int flags) throws IOException {
	    output[0] = (byte) (flags & 0xFF);
	    int offset = 0;
	    if(len < 126){
	        output[1] = (byte) len;
	        offset = 2;
	    } else if(len < 65536){
	    	output[1] = (byte) 126;
	        output[2] = (byte) ((len >> 8) & 0xFF);
	        output[3] = (byte) (len & 0xFF); 
	        offset = 4;
	    } else{
	    	output[1] = (byte) 127;
	        output[2] = (byte) ((len >>> 56) & 0xFF);
	        output[3] = (byte) ((len >> 48) & 0xFF);
	        output[4] = (byte) ((len >> 40) & 0xFF);
	        output[5] = (byte) ((len >> 32) & 0xFF);
	        output[6] = (byte) ((len >> 24) & 0xFF);
	        output[7] = (byte) ((len >> 16) & 0xFF);
	        output[8] = (byte) ((len >> 8) & 0xFF);
	        output[9] = (byte) (len & 0xFF);
	        offset = 10;
	    }
	    Iterator<ByteArraySequence> it = seq.iterator();
		while(it.hasNext()) {
			ByteArraySequence cur = it.next();
			int i = cur.offset();
			byte[] rawData = cur.array();
			int end = i = cur.length();
			for(; i < end; i++)
		        output[offset++] = rawData[i];
		}
	    return output;
	}
	
}
