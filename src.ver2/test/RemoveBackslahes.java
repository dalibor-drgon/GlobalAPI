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

public class RemoveBackslahes {

	public static String removeDoubleBackslahes(String in) {
		//if(true) return wordnice.http.server.HttpRequest.removeDoubleBackslashes(in);
		int i = 0;
		int end = in.length();
		int start = -1;
		int first_start = 0;
		StringBuilder sb = null;
		/*while(i < end) {
			char c = in.charAt(i);
			if(c == '/') {
				i++;
			} else {
				break;
			}
		}*/
		int endOrig = end;
		while(end != i) {
			char c = in.charAt(end-1);
			if(c == '/') {
				end--;
			} else {
				break;
			}
		}
		if(i == end) {
			//TRIMMED
			return "/";
		}
		for(; i < end; i++) {
			char c = in.charAt(i);
			if(c == '/') {
				if(start == -1) start = i;
			} else if(start != -1) {
				if(start < i-1) {
					if(sb == null) sb = wordnice.api.Nice.createStringBuilder();
					if(first_start != start) sb.append(in, first_start, start);
					first_start = i;
					sb.append('/');
				}
				start = -1;
			}
		}
		if(sb == null) {
			if(end != endOrig) return in.substring(0, end);
			//OK
			return in;
		} else {
			if(start == -1 && end != endOrig)
				start = end;
			if(start != -1) {
				if(first_start != start) sb.append(in, first_start, start);
			}
			return sb.toString();
		}
	}
	
	public static void main(String...strings) {
		//Removed only /, because \ are not allowed
		System.out.println(removeDoubleBackslahes(""));
		System.out.println(removeDoubleBackslahes("/"));
		System.out.println(removeDoubleBackslahes("///"));
		System.out.println(removeDoubleBackslahes("///rest/...///"));
		System.out.println(removeDoubleBackslahes("rest/...\\\\"));
		System.out.println(removeDoubleBackslahes("/rest/.../"));
		System.out.println(removeDoubleBackslahes("///rest////.../"));
		System.out.println(removeDoubleBackslahes("///rest////...//"));
		System.out.println(removeDoubleBackslahes("///rest//TWO/THREE/FOUR//FIF//SIX83\\////.../"));
		System.out.println(removeDoubleBackslahes("/favicon.ico"));
		System.out.println(removeDoubleBackslahes("/scripts/pages/console.js"));
		System.out.println(removeDoubleBackslahes("/style/utils.css"));
		System.out.println(removeDoubleBackslahes("/scripts/pages/console.js/////////"));
		System.out.println(removeDoubleBackslahes("/style/utils.css/"));
	}
	
}
/****** Debug Output ************
 
Trimmed:
/
Trimmed:
/
Trimmed:
/
/rest/.../
OK:
rest/...\\
OK:
/rest/.../
/rest/.../
/rest/.../
/rest/TWO/THREE/FOUR/FIF/SIX83\/.../
OK:
/favicon.ico
OK:
/scripts/pages/console.js
OK:
/style/utils.css
/scripts/pages/console.js
/style/utils.css
 *
 */