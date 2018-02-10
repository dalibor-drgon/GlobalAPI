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

package wordnice.config;

public class ConfigurationProperty {
	
	public static long processNumber(SimplePropertyGetter getter, String name, long value) {
		if(getter == null) throw new IllegalArgumentException("Getter == null");
		//long value = getter.getValueInt(name);
		
		
		
		String prop = getter.getValueString(name+"Clamp");
		if(prop != null && !prop.isEmpty()) {
			int i = prop.indexOf(',');
			if(prop.lastIndexOf(',', i+1) == -1) {
				try {
					String minValueStr = prop.substring(0, (i == -1) ? prop.length() : i).trim();
					if(!minValueStr.isEmpty()) {
						long minValue = Long.parseLong(minValueStr);
						value = Math.max(value, minValue);
					}
					if(i != -1) {
						String maxValueStr = prop.substring(i+1).trim();
						if(!maxValueStr.isEmpty()) {
							long maxValue = Long.parseLong(maxValueStr);
							value = Math.min(value, maxValue);
						}
					}
				} catch(NumberFormatException nfe) {}
			}
		}
		return value;
	}
	
}
