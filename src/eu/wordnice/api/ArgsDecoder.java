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

package eu.wordnice.api;

import java.util.HashMap;
import java.util.Map;

public class ArgsDecoder {

	public static Map<String, String> decodeString(String s, String valname,
			String row) {
		Map<String, String> map = new HashMap<String, String>();
		ArgsDecoder.decodeString(map, s, valname, row, null);
		return map;
	}

	public static Map<String, String> decodeString(String s, String valname,
			String row,
			Handler.OneVoidHandler<Val.TwoVal<String, String>> handler) {
		Map<String, String> map = new HashMap<String, String>();
		ArgsDecoder.decodeString(map, s, valname, row, handler);
		return map;
	}

	public static void decodeString(Map<String, String> map,
			String s, String valname, String row) {
		ArgsDecoder.decodeString(map, s, valname, row, null);
	}

	public static void decodeString(Map<String, String> map,
			String s, String valname, String row,
			Handler.OneVoidHandler<Val.TwoVal<String, String>> handler) {

		Val.TwoVal<String, String> twoval = null;
		if (handler != null) {
			twoval = new Val.TwoVal<String, String>();
		}
		int i2, i3; 
		String val, name;
		boolean can = true;
		while(can) {
			i2 = s.indexOf(row);
			if (i2 < 0) {
				if (s.length() == 0) {
					return;
				} else {
					i2 = s.length() - 1;
					can = false;
				}
			}
			i3 = s.indexOf(valname);
			if (i3 > i2) {
				if (i2 > 0) {
					name = s.substring(0, i2);
				} else {
					name = null;
				}
				val = null;
				if (handler != null) {
					twoval.one = name;
					twoval.two = val;
					handler.handle(twoval);
					name = twoval.one;
					val = twoval.two;
				}
				map.put(name, val);
			} else if(i3 > -1) {
				name = s.substring(0, i3);
				if(can) {
					val = s.substring(i3 + valname.length(), i2);
				} else {
					val = s.substring(i3 + valname.length(), i2 + 1);
				}
				if (handler != null) {
					twoval.one = name;
					twoval.two = val;
					handler.handle(twoval);
					name = twoval.one;
					val = twoval.two;
				}
				map.put(name, val);
			}
			s = s.substring(i2 + row.length(), s.length());
		}
	}

}