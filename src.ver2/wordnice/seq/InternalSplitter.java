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

package wordnice.seq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import wordnice.api.Nice;
import wordnice.api.Nice.BadFormat;
import wordnice.api.Nice.MutableEntry;
import wordnice.api.Nice.FastRuntimeException;
import wordnice.api.Nice.Masked;
import wordnice.codings.URLCoder;
import wordnice.utils.NiceStrings;


public class InternalSplitter {
	
	/*public static ColSplitter split(String rowDelimiter) {
		return new ColSplitter(rowDelimiter);
	}
	
	public static MapSplitter splitMap(String rowDelimiter, String keyValSeparator) {
		return new MapSplitter(rowDelimiter, keyValSeparator);
	}
	
	public static class ColSplitter {
		
		ColSplitter(String row) {
			if(row == null) throw new IllegalArgumentException("Row delimiter cannot be null!");
			this.rowDel = row;
		}
		
		ColSplitter(char row) {
			this.rowDel = String.valueOf(row);
		}
		
		ColSplitter(ColSplitter cs) {
			this.rowDel = cs.rowDelimiter();
			this.onHandle = cs.onHandle();
			this.onSplit = cs.onSplit();
		}
		
		protected String rowDel;
		protected Consumer<String> onHandle;
		protected Function<String,String> onSplit;
		protected boolean dieOnNullValue;
		protected boolean dieOnNullKey;
		protected boolean ignoreNullEntry = true;
		protected boolean ignoreNullKey;
		
		public ColSplitter rowDelimiter(String row) {
			if(row == null) throw new IllegalArgumentException("Row delimiter cannot be null!");
			//if(this.rowDelimiter().equals(row)) {
			//	return this;
			//}
			ColSplitter cs = new ColSplitter(this);
			cs.rowDel = row;
			return cs;
		}
		
		public ColSplitter rowDelimiter(char row) {
			return this.rowDelimiter(String.valueOf(row));
		}
		
		public String rowDelimiter() {
			return rowDel;
		}
		
		public MapSplitter keyValueSeparator(String value) {
			if(value == null) throw new IllegalArgumentException("KeyValue separator == null!");
			return new MapSplitter(this.rowDel, value);
		}
		
		public MapSplitter keyValueSeparator(char value) {
			return new MapSplitter(this.rowDel, String.valueOf(value));
		}

		public Consumer<String> onHandle() {
			return onHandle;
		}

		public ColSplitter onHandle(Consumer<String> onHandle) {
			this.onHandle = onHandle;
			return this;
		}

		public Function<String, String> onSplit() {
			return onSplit;
		}

		public ColSplitter onSplit(Function<String, String> onSplit) {
			this.onSplit = onSplit;
			return this;
		}
		
		public int forEach(String input) {
			return forEach(input, onHandle());
		}
		
		public int forEach(String input, Consumer<String> onHandle) {
			if(onHandle == null) throw new IllegalArgumentException("Handler == null");
			return handleColl(onHandle, input, this.rowDel, 0);
		}
		
		public List<String> split(String input) {
			return split(input, onSplit());
		}
		
		public List<String> split(String input, Function<String,String> onSplit) {
			if(onSplit == null) throw new IllegalArgumentException("OnSplit == null");
			return splitColl(onSplit, input, this.rowDel);
		}
		
	}
	
	public static class MapSplitter {
		
		MapSplitter(String row) {
			this.setRowDelimiter(row);
		}
		
		MapSplitter(char row) {
			this.setRowDelimiter(row);
		}
		
		MapSplitter(String row, String value) {
			this.setRowDelimiter(row);
		}
		
		protected String rowDel;
		protected String valueDel;
		
		public MapSplitter setRowDelimiter(String row) {
			if(row == null) throw new IllegalArgumentException("Row delimiter cannot be null!");
			this.rowDel = row;
			return this;
		}
		
		public MapSplitter setRowDelimiter(char row) {
			this.rowDel = String.valueOf(row);
			return this;
		}
		
		public String getRowDelimiter() {
			return rowDel;
		}
		
		public MapSplitter setKeyValueSeparator(String value) {
			if(value == null) throw new IllegalArgumentException("KeyValue separator == null!");
			this.valueDel = value;
			return this;
		}
		
		public MapSplitter setKeyValueSeparator(char value) {
			this.valueDel = String.valueOf(value);
			return this;
		}
		
		public String getKeyValueSeparator() {
			return valueDel;
		}
		
		public boolean hasKeyValueSeparator() {
			return this.valueDel != null;
		}
		
		public static void handle(String input) {
			//handleInternal
		}
		
	}*/
	
	/*
	 * Exception thrown when entry without value is parsed
	 */
	protected static FoundNullValueException _foundNullValueException = new FoundNullValueException();
	
	protected static FoundNullValueException foundNullValueException() {
		if(Nice.debug()) return new FoundNullValueException();
		return _foundNullValueException;
	}
	
	public static class FoundNullValueException
	extends FastRuntimeException {
		private static final long serialVersionUID = 1L;
	}

	
	public static interface MutableEntryConsumer<K,V> 
	extends Consumer<MutableEntry<K,V>> { }
	
	public static final MutableEntryConsumer<String,String> TRIMMER = 
			new MutableEntryConsumer<String,String>() {

				@Override
				public void accept(MutableEntry<String,String> entry) {
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
	
	public static final MutableEntryConsumer<String,String> URLDECODER = 
			new MutableEntryConsumer<String,String>() {

				@Override
				public void accept(MutableEntry<String,String> entry) {
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
			Function<String,String> handler, 
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
			Function<String,String> handler,
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
				val = handler.apply(val);
			}
			coll.add(val);
			
			start = rowIndex + row.length();
		}
	}
	
	public static int handleColl(
			Consumer<String> handler,
			String s, String row, int start) {
		if (handler == null) {
			return 0;
		}
		int rowIndex;
		boolean can = true;
		int entries = 0;
		while(can) {
			rowIndex = s.indexOf(row, start);
			if (rowIndex < 0) {
				if (s.length() == start) {
					break;
				} else {
					rowIndex = s.length();
					can = false;
				}
			} else if(rowIndex == start) {
				start += row.length();
				continue;
			}
			entries++;
			handler.accept(s.substring(start, rowIndex));
			start = rowIndex + row.length();
		}
		return entries;
	}
	
	public static void handleCollInside(Consumer<String> handler,
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
			handler.accept(key);
			return;
		}
		while(start != -1 && end != -1) {
			if(start == 0) {
				handler.accept(key.substring(1, end));
			} else {
				if(lastEnd == -1) {
					handler.accept(key.substring(0, start));
				}
				if(start+1 == end) {
					handler.accept("");
				} else {
					handler.accept(key.substring(start+1,end));
				}
			}
			lastEnd = end;
			start = end+1;
			if(start == key.length()) {
				return;
			}
			if(key.charAt(start) != startChar) { //Eg. "Opps" not inside [] -> Request[Post]Oops
				throw Nice.badFormat("ArgsDecoder.handleArray: Unexpected character")
					.setOffsetAndLength(start, 1);
			}
			end = key.indexOf(endChar, start);
			if(end == -1) { //Last key not terminated with ] -> Request[Post][Password
				throw Nice.badFormat("ArgsDecoder.handleArray: Name not terminated until EOF")
					.setOffsetAndEnd(start, key.length());
			}
			int i = NiceStrings.indexOf(key, startChar, start+1, end);
			if(i != -1) { //Found [ inside [ -> [blah[anotherName ...
				throw Nice.badFormat("ArgsDecoder.handleArray: Found another name starting in name")
					.setOffsetAndLength(i, 1);
			}
			
		}
	};
	
	
	/*#********************************************************************
	 * STRINGS
	 **********************************************************************/
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, String, String, String, int, boolean)
	 */
	public static Map<String,String> splitMap(
			String str, String val, String row)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<String,String>createMap(), null, str, val, row, 0, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, String, String, String, int, boolean)
	 */
	public static Map<String,String> splitMap(
			String str, String val, String row, int start)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<String,String>createMap(), null, str, val, row, start, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, String, String, String, int, boolean)
	 */
	public static Map<String,String> splitMap(MutableEntryConsumer<String,String> handler,
			String str, String val, String row)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<String,String>createMap(), handler, str, val, row, 0, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, String, String, String, int, boolean)
	 */
	public static Map<String,String> splitMap(MutableEntryConsumer<String,String> handler,
			String str, String val, String row, int start)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<String,String>createMap(), handler, str, val, row, start, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, String, String, String, int, boolean)
	 */
	public static Map<String,String> splitMap(Map<String,String> output,
			MutableEntryConsumer<String,String> handler,
			String str, String val, String row)
				throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(output, handler, str, val, row, 0, true);
	}
	
	/**
	 * Low-level Splitting (handling) of string
	 * @param output Output map
	 * @param handler Handler : Can be null
	 * @param str String to split (handle)
	 * @param val Delimited of name and value
	 * @param row Delimiter of rows
	 * @param start Where to start in string : Default 0
	 * @param allowNullValue Allow keys without values? : Default true
	 * @return First argument - output map
	 * @throws FoundNullValueException If allowNullValue argument is false and 
	 * 			key without value was found
	 * @throws IllegalArgumentException If any of argument (except handler) are null
	 * @throws IndexOutOfBoundsException (start < 0 || start > str.length())
	 */
	public static Map<String,String> splitMap(Map<String,String> output,
			MutableEntryConsumer<String,String> handler,
			String str, String val, String row, int start, boolean allowNullValue)
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		if(output == null) throw new IllegalArgumentException("Output map == null");
		if(str == null) throw new IllegalArgumentException("String == null");
		if(val == null) throw new IllegalArgumentException("Name-Value delimiter == null");
		if(row == null) throw new IllegalArgumentException("Row delimiter == null");
		if(start == str.length()) return output;
		if(start < 0 || start > str.length()) throw new ArrayIndexOutOfBoundsException(start);
		
		MutableEntry<String,String> entry = null;
		if(handler != null) {
			entry = new MutableEntry<String,String>();
		}
		int nextVi = -1;
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
				i = str.length();
			}
			int vi = nextVi;
			nextVi = -1;
			if(vi == -1) vi = str.indexOf(val, start);
			if(vi+val.length() == i && start == vi) { //delimiters like: <ROW><VAL><ROW>
				start = i + row.length();
				continue;
			}
			
			String key = null, keyval = null;
			if(vi < 0) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
			} else if(vi > i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
				nextVi = vi;
			} else if(vi+val.length() == i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, vi);
			} else {
				key = str.substring(start, vi);
				keyval = str.substring(vi+val.length(), i);
			}
			start = i + row.length();
			
			if(handler == null) {
				output.put(key, keyval);
			} else {
				entry.setKey(key);
				entry.setValue(keyval);
				handler.accept(entry);
				output.put(entry.getKey(), entry.getValue());
			}
			if(i == str.length()) break;
		}
		return output;
	}
	
	/**
	 * @see #handleMap(BiConsumer, String, String, String, int, boolean)
	 */
	public static int handleMap(
			BiConsumer<String,String> handler,
			String str, String val, String row) 
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		return handleMap(handler, str, val, row, 0, true);
	}
	
	/**
	 * Low-level Splitting (handling) of string
	 * @param handler Handler
	 * @param str String to split (handle)
	 * @param val Delimited of name and value
	 * @param row Delimiter of rows
	 * @param start Where to start in string
	 * @param allowNullValue Allow keys without values?
	 * @return Number of entries handled (splitted) = number of calls to handler
	 * @throws FoundNullValueException If allowNullValue argument is false and 
	 * 			key without value was found
	 * @throws IllegalArgumentException If any of argument are null 
	 * @throws IndexOutOfBoundsException (start < 0 || start > str.length())
	 */
	public static int handleMap(
			BiConsumer<String,String> handler,
			String str, String val, String row, int start, 
			boolean allowNullValue) 
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		if(handler == null) throw new IllegalArgumentException("Handler == null");
		if(str == null) throw new IllegalArgumentException("String == null");
		if(val == null) throw new IllegalArgumentException("Name-Value delimiter == null");
		if(row == null) throw new IllegalArgumentException("Row delimiter == null");
		if(start == str.length()) return 0;
		if(start < 0 || start > str.length()) throw new ArrayIndexOutOfBoundsException(start);
		int entries = 0, nextVi = -1;
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
				i = str.length();
			}
			int vi = nextVi;
			nextVi = -1;
			if(vi == -1) vi = str.indexOf(val, start);
			if(vi+val.length() == i && start == vi) { //delimiters like: <ROW><VAL><ROW>
				start = i + row.length();
				continue;
			}
			
			String key = null, keyval = null;
			if(vi < 0) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
			} else if(vi > i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
				nextVi = vi;
			} else if(vi+val.length() == i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, vi);
			} else {
				key = str.substring(start, vi);
				keyval = str.substring(vi+val.length(), i);
			}
			start = i + row.length();
			
			handler.accept(key, keyval);
			entries++;
			if(i == str.length()) break;
		}
		return entries;
	}
	
	
	/*#********************************************************************
	 * BYTE SEQUENCES
	 **********************************************************************/
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(
			ByteSequence str, ByteSequence val, ByteSequence row)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<ByteSequence,ByteSequence>createMap(), null, str, val, row, 0, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(
			ByteSequence str, ByteSequence val, ByteSequence row, int start)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<ByteSequence,ByteSequence>createMap(), null, str, val, row, start, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(MutableEntryConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<ByteSequence,ByteSequence>createMap(), handler, str, val, row, 0, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(MutableEntryConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row, int start)
					throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(Nice.<ByteSequence,ByteSequence>createMap(), handler, str, val, row, start, true);
	}
	
	/**
	 * @see #splitMap(Map, MutableEntryConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(Map<ByteSequence,ByteSequence> output,
			MutableEntryConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row)
				throws IllegalArgumentException, IndexOutOfBoundsException {
		return splitMap(output, handler, str, val, row, 0, true);
	}
	
	/**
	 * Low-level Splitting (handling) of string
	 * @param output Output map
	 * @param handler Handler : Can be null
	 * @param str ByteSequence to split (handle)
	 * @param val Delimited of name and value
	 * @param row Delimiter of rows
	 * @param start Where to start in string : Default 0
	 * @param allowNullValue Allow keys without values? : Default true
	 * @return First argument - output map
	 * @throws FoundNullValueException If allowNullValue argument is false and 
	 * 			key without value was found
	 * @throws IllegalArgumentException If any of argument (except handler) are null
	 * @throws IndexOutOfBoundsException (start < 0 || start > str.length())
	 */
	public static Map<ByteSequence,ByteSequence> splitMap(Map<ByteSequence,ByteSequence> output,
			MutableEntryConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row, int start, boolean allowNullValue)
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		if(output == null) throw new IllegalArgumentException("Output map == null");
		if(str == null) throw new IllegalArgumentException("ByteSequence == null");
		if(val == null) throw new IllegalArgumentException("Name-Value delimiter == null");
		if(row == null) throw new IllegalArgumentException("Row delimiter == null");
		if(start == str.length()) return output;
		if(start < 0 || start > str.length()) throw new ArrayIndexOutOfBoundsException(start);
		
		MutableEntry<ByteSequence,ByteSequence> entry = null;
		if(handler != null) {
			entry = new MutableEntry<ByteSequence,ByteSequence>();
		}
		int nextVi = -1;
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
				i = str.length();
			}
			int vi = nextVi;
			nextVi = -1;
			if(vi == -1) vi = str.indexOf(val, start);
			if(vi+val.length() == i && start == vi) { //delimiters like: <ROW><VAL><ROW>
				start = i + row.length();
				continue;
			}
			
			ByteSequence key = null, keyval = null;
			if(vi < 0) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
			} else if(vi > i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
				nextVi = vi;
			} else if(vi+val.length() == i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, vi);
			} else {
				key = str.substring(start, vi);
				keyval = str.substring(vi+val.length(), i);
			}
			start = i + row.length();
			
			if(handler == null) {
				output.put(key, keyval);
			} else {
				entry.setKey(key);
				entry.setValue(keyval);
				handler.accept(entry);
				output.put(entry.getKey(), entry.getValue());
			}
			if(i == str.length()) break;
		}
		return output;
	}
	
	/**
	 * @see #handleMap(BiConsumer, ByteSequence, ByteSequence, ByteSequence, int, boolean)
	 */
	public static int handleMap(
			BiConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row) 
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		return handleMap(handler, str, val, row, 0, true);
	}
	
	/**
	 * Low-level Splitting (handling) of string
	 * @param handler Handler
	 * @param str ByteSequence to split (handle)
	 * @param val Delimited of name and value
	 * @param row Delimiter of rows
	 * @param start Where to start in string
	 * @param allowNullValue Allow keys without values?
	 * @return Number of entries handled (splitted) = number of calls to handler
	 * @throws FoundNullValueException If allowNullValue argument is false and 
	 * 			key without value was found
	 * @throws IllegalArgumentException If any of argument are null 
	 * @throws IndexOutOfBoundsException (start < 0 || start > str.length())
	 */
	public static int handleMap(
			BiConsumer<ByteSequence,ByteSequence> handler,
			ByteSequence str, ByteSequence val, ByteSequence row, int start, 
			boolean allowNullValue) 
					throws FoundNullValueException,
					IllegalArgumentException, IndexOutOfBoundsException {
		if(handler == null) throw new IllegalArgumentException("Handler == null");
		if(str == null) throw new IllegalArgumentException("ByteSequence == null");
		if(val == null) throw new IllegalArgumentException("Name-Value delimiter == null");
		if(row == null) throw new IllegalArgumentException("Row delimiter == null");
		if(start == str.length()) return 0;
		if(start < 0 || start > str.length()) throw new ArrayIndexOutOfBoundsException(start);
		int entries = 0, nextVi = -1;
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
				i = str.length();
			}
			int vi = nextVi;
			nextVi = -1;
			if(vi == -1) vi = str.indexOf(val, start);
			if(vi+val.length() == i && start == vi) { //delimiters like: <ROW><VAL><ROW>
				start = i + row.length();
				continue;
			}
			
			ByteSequence key = null, keyval = null;
			if(vi < 0) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
			} else if(vi > i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, i);
				nextVi = vi;
			} else if(vi+val.length() == i) {
				if(!allowNullValue) throw foundNullValueException();
				key = str.substring(start, vi);
			} else {
				key = str.substring(start, vi);
				keyval = str.substring(vi+val.length(), i);
			}
			start = i + row.length();
			
			handler.accept(key, keyval);
			entries++;
			if(i == str.length()) break;
		}
		return entries;
	}

}