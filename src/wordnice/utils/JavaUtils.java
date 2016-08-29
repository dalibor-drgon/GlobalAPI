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

package wordnice.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaUtils {

	public static File getJRE() {
		return new File(System.getProperty("java.home"));
	}

	public static String getCommandOutput(String... args) {
		String output = null; // the string to return

		Process process = null;
		BufferedReader reader = null;
		InputStreamReader streamReader = null;
		InputStream stream = null;
		InputStream err = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectErrorStream(false);
			process = pb.start();

			stream = process.getInputStream();
			err = process.getErrorStream();
			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);

			byte[] blah = new byte[1024];
			String currentLine = null; // store current line of OutputStream from the
										// cmd
			StringBuilder commandOutput = new StringBuilder(); // build up the
																// OutputStream from
																// cmd

			while (err.read(blah) > 0);
			while ((currentLine = reader.readLine()) != null) {
				commandOutput.append(currentLine);
			}
			while (err.read(blah) > 0);

			if (process.waitFor() == 0) {
				output = commandOutput.toString();
			}
		} catch (Exception e) {
			if (e instanceof IOException && !(e instanceof EOFException)) {
				output = null;
			}
		}
		try {
			stream.close();
		} catch (Exception e) {
		}
		try {
			streamReader.close();
		} catch (Exception e) {
		}
		try {
			streamReader.close();
		} catch (Exception e) {
		}

		return output;
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		return (os != null && (os.contains("win") || os.contains("Win")));
	}

	public static File getJDK() {
		File lastChance = null;

		String ver = "jdk" + (System.getProperty("java.version").toLowerCase());

		String jdk = System.getenv("JAVA_HOME");
		if (jdk != null && !jdk.isEmpty() && ver != null) {
			lastChance = new File(jdk);
			if (lastChance.exists() && lastChance.getName().toLowerCase().contains(ver)) {
				return lastChance;
			}
		}

		String os = System.getProperty("os.name");
		boolean isWin = (os != null && (os.contains("win") || os.contains("Win")));
		if (!isWin) {
			String response = getCommandOutput("whereis", "javac");
			if (response != null && !response.isEmpty()) {
				int pathStartIndex = response.indexOf('/');
				if (pathStartIndex != -1) {
					String[] paths = response.substring(pathStartIndex, response.length()).split(" ");
					for (int i = 0, n = paths.length; i < n; i++) {
						String path = paths[i];
						if (!path.endsWith("javac")) {
							continue;
						}
						lastChance = FilesAPI.readFinalLink(new File(path)).getParentFile().getParentFile();
						if (lastChance.exists()) {
							return lastChance;
						}
					}
				}
			}
			if (lastChance == null) {
				lastChance = getJRE().getParentFile();
			}
		} else {
			String path = getCommandOutput("where.exe", "javac");
			if (path != null && !path.isEmpty()) {
				lastChance = new File(path).getParentFile().getParentFile();
				if (lastChance.exists() && lastChance.getName().toLowerCase().contains(ver)) {
					return lastChance;
				}
			}

			if (ver == null) {
				return lastChance;
			}
			path = System.getenv("PATH");
			if (path == null) {
				path = System.getenv("Path");
			}

			if (path != null) {
				String[] paths = path.split(File.pathSeparator);
				for (int i = 0, n = paths.length; i < n; i++) {
					String p = paths[i];
					if (p.toLowerCase().contains(ver)) {
						File f = new File(p);
						if (f.exists()) {
							return f;
						} else if (lastChance == null) {
							lastChance = f;
						}
					}
				}
			}

			File[] roots = File.listRoots();
			if (roots == null || roots.length == 0) {
				return null;
			}
			File jp = new File(roots[0], "Program Files/Java/");
			if (!jp.exists() || !jp.isDirectory()) {
				jp = new File(roots[0], "Program Files (x86)/Java/");
				if (!jp.exists() || !jp.isDirectory()) {
					return null;
				}
			}
			File[] jvs = jp.listFiles();
			if (jvs == null || jvs.length == 0) {
				return null;
			}
			if (jvs.length == 1) {
				return jvs[0];
			}
			for (int i = 0, n = jvs.length; i < n; i++) {
				File f = jvs[i];
				String nm = f.getName().toLowerCase();
				if (nm.contains(ver)) {
					return f;
				} else if (nm.contains("jdk")) {
					if (lastChance == null || !lastChance.exists()) {
						lastChance = f;
					}
				}
			}
		}
		if (lastChance == null) {
			if (jdk != null && !jdk.isEmpty()) {
				lastChance = new File(jdk);
			}
		}
		return lastChance;
	}

}
