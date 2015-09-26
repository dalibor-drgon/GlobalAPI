/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 22201115, Dalibor Drgoň <emptychannelmc@gmail.com>
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

package eu.wordnice.db.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.wordnice.api.Api;
import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;
import eu.wordnice.streams.InputAdv;
import eu.wordnice.streams.OutputAdv;

public class SerializeUtils {
	
	public static void write(DataWriter dw, File file) throws SerializeException, IOException {
		if(file.exists() == false) {
			Api.createFileIfNot(file);
		}
		Output os = OutputAdv.forFile(file);
		try {
			dw.write(os);
		} catch(SerializeException e) {
			try {
				os.close();
			} catch(Exception ign) {}
			throw e;
		} catch(IOException e) {
			try {
				os.close();
			} catch(Exception ign) {}
			throw e;
		}
		try {
			os.close();
		} catch(Exception ign) {}
	}
	
	public static void read(DataReader dr, File file) throws SerializeException, FileNotFoundException, IOException {
		Input is = InputAdv.forFile(file);
		try {
			dr.read(is);
		} catch(SerializeException e) {
			try {
				is.close();
			} catch(Exception ign) {}
			throw e;
		} catch(IOException e) {
			try {
				is.close();
			} catch(Exception ign) {}
			throw e;
		}
		try {
			is.close();
		} catch(Exception ign) {}
	}
	
}
