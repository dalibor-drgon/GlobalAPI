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

package wordnice.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import wordnice.api.Api;
import wordnice.api.Api.BadFormat;
import wordnice.api.Api.DataEntry;
import wordnice.api.Api.Masked;
import wordnice.codings.URLCoder;


public class ArgsDecoder {
	
	public static interface MapHandler<K,V> {
		public void handle(K key, V value) throws Masked;
	}
	
	public static interface AtomicMapHandler<K,V> {
		public void handle(DataEntry<K,V> entry) throws Masked;
	}
	
	public static interface CollHandler<V> {
		public void handle(V value) throws Masked;
	}
	
	public static interface AtomicCollHandler<V> {
		public V handle(V value) throws Masked;
	}
	
	public static final AtomicMapHandler<String,String> TRIMMER = 
			new AtomicMapHandler<String,String>() {

				@Override
				public void handle(DataEntry<String,String> entry) {
					String key = entry.getKey();
					String val = entry.getValue();
					if(key != null) {
						entry.setKey(key.trim());
					}
					if(val != null) {
						entry.setValue(val.trim());
					}
				}
		
	};
	
	public static final AtomicMapHandler<String,String> URLDECODER = 
			new AtomicMapHandler<String,String>() {

				@Override
				public void handle(DataEntry<String,String> entry) {
					String key = entry.getKey();
					String val = entry.getValue();
					if(key != null) {
						entry.setKey(URLCoder.decode(key));
					}
					if(val != null) {
						entry.setKey(URLCoder.decode(val));
					}
				}
	};
	
	public static List<String> splitColl(String s, String row) {
		List<String> list = new ArrayList<String>();
		splitColl(list, null, s, row);
		return list;
	}

	public static List<String> splitColl(
			AtomicCollHandler<String> handler, 
			String s, String row) {
		List<String> list = new ArrayList<String>();
		splitColl(list, handler, s, row);
		return list;
	}

	public static void splitColl(
			Collection<String> coll,
			String s, String row) {
		splitColl(coll, null, s, row);
	}

	public static void splitColl(
			Collection<String> coll,
			AtomicCollHandler<String> handler,
			String s, String row) {
		
		int rowIndex;
		int start = 0;
		boolean can = true;
		while(can) {
			rowIndex = s.indexOf(row, start);
			if (rowIndex < 0) {
				if (s.length() == start) {
					return;
				} else {
					rowIndex = s.length();
					can = false;
				}
			} else if(rowIndex == start) {
				start += row.length();
				continue;
			}
			String val = s.substring(start, rowIndex);
			if(handler != null) {
				val = handler.handle(val);
			}
			coll.add(val);
			
			start = rowIndex + row.length();
		}
	}
	
	public static void handleColl(
			CollHandler<String> handler,
			String s, String row) {
		if (handler == null) {
			return;
		}
		int rowIndex;
		boolean can = true;
		int start = 0;
		while(can) {
			rowIndex = s.indexOf(row, start);
			if (rowIndex < 0) {
				if (s.length() == start) {
					return;
				} else {
					rowIndex = s.length();
					can = false;
				}
			} else if(rowIndex == start) {
				start += row.length();
				continue;
			}
			handler.handle(s.substring(start, rowIndex));
			start = rowIndex + row.length();
		}
	}
	
	public static void handleCollInside(CollHandler<String> handler,
			String key, char startChar, char endChar) 
			throws BadFormat, Masked {
		if(handler == null) {
			return;
		}
		int lastEnd = -1;
		int start = key.indexOf(startChar, 0);
		int end = -1;
		if(start != -1) {
			end = key.indexOf(endChar, start);
		}
		if(start == -1 || end == -1) {
			handler.handle(key);
			return;
		}
		while(start != -1 && end != -1) {
			if(start == 0) {
				handler.handle(key.substring(1, end));
			} else {
				if(lastEnd == -1) {
					handler.handle(key.substring(0, start));
				}
				if(start+1 == end) {
					handler.handle("");
				} else {
					handler.handle(key.substring(start+1,end));
				}
			}
			lastEnd = end;
			start = end+1;
			if(start == key.length()) {
				return;
			}
			if(key.charAt(start) != startChar) { //Eg. "Opps" not inside [] -> Request[Post]Oops
				throw Api.badFormat("ArgsDecoder.handleArray: Unexpected character")
					.setOffsetAndLength(start, 1);
			}
			end = key.indexOf(endChar, start);
			if(end == -1) { //Last key not terminated with ] -> Request[Post][Password
				throw Api.badFormat("ArgsDecoder.handleArray: Name not terminated until EOF")
					.setOffsetAndEnd(start, key.length());
			}
			int i = NiceStringUtils.indexOf(key, startChar, start+1, end);
			if(i != -1) { //Found [ inside [ -> [blah[anotherName ...
				throw Api.badFormat("ArgsDecoder.handleArray: Found another name starting in name")
					.setOffsetAndLength(i, 1);
			}
			
		}
	};
	
	
	/**
	 * BYTESEQUENCE
	 */
	
	public static Map<ByteSequence,ByteSequence> handleMap(
			ByteSequence str, ByteSequence val, ByteSequence row) throws Masked {
		return handleMap(Api.<ByteSequence,ByteSequence>createMap(), null, str, val, row, 0);
	}
	
	public static Map<ByteSequence,ByteSequence> handleMap(
			ByteSequence str, ByteSequence val, ByteSequence row, int start) throws Masked {
		return handleMap(Api.<ByteSequence,ByteSequence>createMap(), null, str, val, row, start);
	}
	
	public static Map<ByteSequence,ByteSequence> handleMap(AtomicMapHandler<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row) throws Masked {
		return handleMap(Api.<ByteSequence,ByteSequence>createMap(), handler, str, val, row, 0);
	}
	
	public static Map<ByteSequence,ByteSequence> handleMap(AtomicMapHandler<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row, int start) throws Masked {
		return handleMap(Api.<ByteSequence,ByteSequence>createMap(), handler, str, val, row, start);
	}
	
	public static Map<ByteSequence,ByteSequence> handleMap(Map<ByteSequence,ByteSequence> output,
			AtomicMapHandler<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row) throws Masked {
		return handleMap(output, handler, str, val, row, 0);
	}
	
	public static Map<ByteSequence,ByteSequence> handleMap(Map<ByteSequence,ByteSequence> output,
			AtomicMapHandler<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row, int start) throws Masked {
		DataEntry<ByteSequence,ByteSequence> entry = null;
		if(handler != null) {
			entry = new DataEntry<ByteSequence,ByteSequence>();
		}
		while(true) {
			int i = str.indexOf(row, start);
			if(i == start) {
				start += row.length();
				continue;
			}
			if(i < 0) {
				if(start == str.length()) {
					break;
				}
			}
			int vi = str.indexOf(val, start);
			ByteSequence key = null, keyval = null;
			if(i < 0) {
				if(vi < 0) {
					key = str.substring(start);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length());
				}
			} else {
				if(vi < 0) {
					key = str.substring(start, i);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length(), i);
				}
				start = i + row.length();
			}
			if(handler == null) {
				output.put(key, keyval);
			} else {
				entry.setKey(key);
				entry.setValue(keyval);
				handler.handle(entry);
				output.put(entry.getKey(), entry.getValue());
			}
			if(i < 0) break;
		}
		return output;
	}
	
	
	
	/**
	 * STRINGS
	 */
	
	public static Map<String,String> handleMap(
			String str, String val, String row) throws Masked {
		return handleMap(Api.<String,String>createMap(), null, str, val, row, 0);
	}
	
	public static Map<String,String> handleMap(
			String str, String val, String row, int start) throws Masked {
		return handleMap(Api.<String,String>createMap(), null, str, val, row, start);
	}
	
	public static Map<String,String> handleMap(AtomicMapHandler<String,String> handler,
			String str, String val, String row) throws Masked {
		return handleMap(Api.<String,String>createMap(), handler, str, val, row, 0);
	}
	
	public static Map<String,String> handleMap(AtomicMapHandler<String,String> handler,
			String str, String val, String row, int start) throws Masked {
		return handleMap(Api.<String,String>createMap(), handler, str, val, row, start);
	}
	
	public static Map<String,String> handleMap(Map<String,String> output,
			AtomicMapHandler<String,String> handler,
			String str, String val, String row) throws Masked {
		return handleMap(output, handler, str, val, row, 0);
	}
	
	public static Map<String,String> handleMap(Map<String,String> output,
			AtomicMapHandler<String,String> handler,
			String str, String val, String row, int start) throws Masked {
		DataEntry<String,String> entry = null;
		if(handler != null) {
			entry = new DataEntry<String,String>();
		}
		while(true) {
			int i = str.indexOf(row, start);
			if(i == start) {
				start += row.length();
				continue;
			}
			if(i < 0) {
				if(start == str.length()) {
					break;
				}
			}
			int vi = str.indexOf(val, start);
			String key = null, keyval = null;
			if(i < 0) {
				if(vi < 0) {
					key = str.substring(start);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length());
				}
			} else {
				if(vi < 0) {
					key = str.substring(start, i);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length(), i);
				}
				start = i + row.length();
			}
			if(handler == null) {
				output.put(key, keyval);
			} else {
				entry.setKey(key);
				entry.setValue(keyval);
				handler.handle(entry);
				output.put(entry.getKey(), entry.getValue());
			}
			if(i < 0) break;
		}
		return output;
	}
	
	public static void handleMap(MapHandler<String,String> handler, 
			String str, String val, String row) throws Masked {
		handleMap(handler, str, val, row, 0);
	}
	
	public static void handleMap(MapHandler<String,String> handler, 
			String str, String val, String row, int start) throws Masked {
		while(true) {
			int i = str.indexOf(row, start);
			if(i == start) {
				start += row.length();
				continue;
			}
			if(i < 0) {
				if(start == str.length()) {
					break;
				}
			}
			int vi = str.indexOf(val, start);
			String key = null, keyval = null;
			if(i < 0) {
				if(vi < 0) {
					key = str.substring(start);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length());
				}
			} else {
				if(vi < 0) {
					key = str.substring(start, i);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length(), i);
				}
				start = i + row.length();
			}
			handler.handle(key, keyval);
			if(i < 0) break;
		}
	}
	
	public static void handleMap(MapHandler<ByteSequence,ByteSequence> handler, 
			ByteSequence str, ByteSequence val, ByteSequence row) throws Masked {
		handleMap(handler, str, val, row, 0);
	}
	
	public static void handleMap(MapHandler<ByteSequence,ByteSequence> handler, 
			ByteSequence str, ByteSequence val, ByteSequence row, int start) throws Masked {
		while(true) {
			int i = str.indexOf(row, start);
			if(i == start) {
				start += row.length();
				continue;
			}
			if(i < 0) {
				if(start == str.length()) {
					break;
				}
			}
			int vi = str.indexOf(val, start);
			ByteSequence key = null, keyval = null;
			if(i < 0) {
				if(vi < 0) {
					key = str.substring(start);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length());
				}
			} else {
				if(vi < 0) {
					key = str.substring(start, i);
				} else {
					key = str.substring(start, vi);
					keyval = str.substring(vi+val.length(), i);
				}
				start = i + row.length();
			}
			handler.handle(key, keyval);
			if(i < 0) break;
		}
	}
	
	
	/*public static void main(String...strings) {
		DataEntryHandler<String,String> outputHandler = 
				new DataEntryHandler<String,String>() {
	
			@Override
			public boolean handle(DataEntry<String, String> entry) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
				return true;
			}
			
		};
		
		AtomicCollHandler<String> outputSingleHandler = 
				new AtomicCollHandler<String>() {
	
			@Override
			public void handle(Value<String> entry) {
				System.out.println("Entry[]= " + entry.getValue());
			}
			
		};
		
		String str = "abc&abc=ASD&asdb[2]=23=32&&Hey1=1&Hey2=2&Hey1=3&Hey2&Hey4=2&Hey4&Hey5&Hey=Nope&";
		handleString(str, "=", "&", outputHandler);
		System.out.println(decodeString(str, "=", "&"));
		
		handleSplitString(str, "&", outputSingleHandler);
		System.out.println(splitString(str, "&"));
		
		String key1 = "Testing[KeyOne][][KeyTwo][a]";
		String key2 = "This[Example2][Example2][Example2][]";
		parseArrayKey(key1, '[', ']', outputSingleHandler);
		parseArrayKey(key2, '[', ']', outputSingleHandler);
	}*/

}