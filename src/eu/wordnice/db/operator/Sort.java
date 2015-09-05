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

package eu.wordnice.db.operator;

public class Sort {

	/**
	 * Key / column name
	 */
	public String key;
	
	/**
	 * Sort method type
	 */
	public SType type;
	
	/**
	 * Are data binary
	 */
	public boolean bin;
	
	/**
	 * Create when requesting sorted data
	 * 
	 * @param key Key / column name {@link Sort#key}
	 * @param type Sort method type {@link Sort#type}
	 * @param bin Are data binary {@link Sort#bin}}
	 */
	public Sort(String key, SType type, boolean bin) {
		this.key = key;
		this.type = (type == null) ? SType.ASC : type;
		this.bin = bin;
	}
	
	/**
	 * Create when requesting sorted data
	 * 
	 * @param key Key / column name {@link Sort#key}
	 * @param type Sort method type {@link Sort#type}
	 */
	public Sort(String key, SType type) {
		this.key = key;
		this.type = (type == null) ? SType.ASC : type;
		this.bin = false;
	}
	
	/**
	 * Create when requesting sorted data (ascending)
	 * 
	 * @param key Key / column name. {@link Sort#key}
	 */
	public Sort(String key) {
		this.key = key;
		this.type = SType.ASC;
		this.bin = false;
	}

	
	/**
	 * @return SQL string
	 */
	public String toSQL() {
		return this.key + " " + this.type.toSQL(this.bin);
	}
	
}
