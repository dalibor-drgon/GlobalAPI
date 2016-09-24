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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import gnu.trove.set.hash.THashSet;
import sun.misc.Unsafe;
import wordnice.api.Nice;
import wordnice.javaagent.JavaAgent;
import wordnice.streams.ArrayOutputStream;
import wordnice.streams.IUtils;
import wordnice.threads.TimeoutThread;
import wordnice.threads.TimeoutThread.Result;

public class JavaUtils {

	public static class RuntimeThrowable extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		public RuntimeThrowable(Throwable t) {
			initCauseOfThis(t);
		}
		
		@Override
		public void setStackTrace(StackTraceElement[] st) {
			this.getCause().setStackTrace(st);
		}
		
		@Override
		public StackTraceElement[] getStackTrace() {
			return this.getCause().getStackTrace();
		}
		
		protected Throwable initCauseOfThis(Throwable cuz) {
			return super.initCause(cuz);
		}
		
		@Override
		public Throwable initCause(Throwable cuz) {
			return this.getCause().initCause(cuz);
		}
		
		@Override
		public Throwable getCause() {
			return this.getCause();
		}
		
		@Override
		public Throwable fillInStackTrace() {
			return this.getCause();
		}
		
		public void throwIt() throws Throwable {
			throw this.getCause();
		}
		
		@Override
		public boolean equals(Object obj) {
			return this.getCause().equals(obj);
		}
		
	}
	
	protected static Unsafe unsafe;
	protected static Method getThreads;
	
	/*** C & UNSAFE ***/
	
	public static Instrumentation getInstrumentation() {
		return JavaAgent.get();
	}
	
	public static Unsafe getUnsafe() {
		if(unsafe == null) {
			Class<?> clz = Unsafe.class;
			Field[] fields = clz.getDeclaredFields();
			for(int i = 0, n = fields.length; i < n; i++) {
				Field f = fields[i];
				if(!f.getType().equals(Unsafe.class)) {
					continue;
				}
				try {
					f.setAccessible(true);
					Unsafe unf = (Unsafe) f.get(null);
					if(unf != null) {
						return (unsafe = unf);
					}
				} catch(Throwable t) {}
			}
		}
		return unsafe;
	}
	
	public static boolean is64() {
		return (Unsafe.ADDRESS_SIZE == 8);
	}
	
	public static void throv(Throwable t) {
		Unsafe unf = getUnsafe();
		if(unf != null) {
			unf.throwException(t);
		}
		throw new RuntimeException(t);
	}
	
	
	/*** MEMORY ***/
	
	public static void memcpy(Object to, Object from, int size) {
		System.arraycopy(from, 0, to, 0, size);
	}
	
	public static void memcpy(Object to, int posto, Object from, int posfrom, int size) {
		System.arraycopy(from, posfrom, to, posto, size);
	}
	
	public static void memcpy(long to, long from, long sz) {
		getUnsafe().copyMemory(from, to, sz);
	}
	
	public static void memset(long to, byte val, long sz) {
		getUnsafe().setMemory(to, sz, val);
	}
	
	
	public static long malloc(long sz) {
		return getUnsafe().allocateMemory(sz);
	}
	
	public static long zalloc(long sz) {
		return getUnsafe().allocateMemory(sz);
	}
	
	public static long realloc(long ptr, long nevsz) {
		return getUnsafe().reallocateMemory(ptr, nevsz);
	}
	
	public static void free(long ptr) {
		getUnsafe().freeMemory(ptr);
	}
	
	public static long getPointer(Object obj) {
		Object arr[] = new Object[] { obj };
		long base_offset = getUnsafe().arrayBaseOffset(Object[].class);
		if(is64()) {
			return getUnsafe().getLong(arr, base_offset);
		} else {
			return getUnsafe().getInt(arr, base_offset);
		}
	}
	
	/*** CLASS LOADERS ***/
	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
	public static Set<Class<?>> filterClasses(Collection<Class<?>> in, Class<?> ext) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClasses(out, in, ext);
		return out;
	}
	
	public static void filterClasses(Collection<Class<?>> ret, Collection<Class<?>> in, Class<?> ext) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> clz = it.next();
			if(ext.isAssignableFrom(clz)) {
				ret.add(clz);
			}
		}
	}
	
	public static boolean instanceOf(Object o, Class<?> c) {
		if(o == null || c == null) {
			return false;
		}
		return c.isAssignableFrom(o.getClass());
	}
	
	public static boolean instanceOf(Class<?> o, Class<?> c) {
		if(o == null || c == null) {
			return false;
		}
		return c.isAssignableFrom(o);
	}
	
	public static Class<?> getClass(String name) throws Throwable {
		return (Class<?>) Class.forName(name);
	}
	
	public static Set<String> filterClassesString(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterClassesString(out, in, pref);
		return out;
	}
	
	public static void filterClassesString(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Class<?>> filterClasses(Collection<Class<?>> in, String pref) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClasses(out, in, pref);
		return out;
	}
	
	public static void filterClasses(Collection<Class<?>> out, Collection<Class<?>> in, String pref) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> cur = it.next();
			if(cur.getName().startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<String> filterClassesStringEquals(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterClassesStringEquals(out, in, pref);
		return out;
	}
	
	public static void filterClassesStringEquals(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.equals(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Class<?>> filterClassesEquals(Collection<Class<?>> in, String pref) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClassesEquals(out, in, pref);
		return out;
	}
	
	public static void filterClassesEquals(Collection<Class<?>> out, Collection<Class<?>> in, String pref) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> cur = it.next();
			if(cur.getName().equals(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<String> filterPackagesString(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterPackagesString(out, in, pref);
		return out;
	}
	
	public static void filterPackagesString(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Package> filterPackages(Collection<Package> in, String pref) {
		Set<Package> out = new THashSet<Package>();
		filterPackages(out, in, pref);
		return out;
	}
	
	public static void filterPackages(Collection<Package> out, Collection<Package> in, String pref) {
		Iterator<Package> it = in.iterator();
		while(it.hasNext()) {
			Package cur = it.next();
			if(cur.getName().startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static void loadClasses(Collection<Class<?>> out, Collection<String> in) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(getClass(it.next()));
		}
	}
	
	public static void loadClasses(Collection<Class<?>> out, Collection<String> in, ClassLoader cl) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(cl.loadClass(it.next()));
		}
	}
	
	public static void loadPackages(Collection<Package> out, Collection<String> in) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(Package.getPackage(it.next()));
		}
	}
	
	public static File getClassesLocation(Class<?> cls) throws URISyntaxException {
		return new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
	}
	
	public static File getClassesLocation(ClassLoader cl) throws URISyntaxException {
		return new File(cl.getResource("").toURI());
	}
	
	protected static void getClassesFolder(Collection<String> set, File fd, String pref) {
		String[] ls = fd.list();
		int i = 0;
		for(; i < ls.length; i++) {
			String curstr = ls[i];
			File cur = new File(fd, curstr);
			if(cur.isDirectory()) {
				getClassesFolder(set, cur, (pref + curstr + "."));
			} else {
				String clsn = pref + curstr;
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.add(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	protected static void getClassesFolder(Consumer<String> handler, File fd, String pref) {
		String[] ls = fd.list();
		int i = 0;
		for(; i < ls.length; i++) {
			String curstr = ls[i];
			File cur = new File(fd, curstr);
			if(cur.isDirectory()) {
				getClassesFolder(handler, cur, (pref + curstr + "."));
			} else {
				String clsn = pref + curstr;
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					handler.accept(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	public static Set<String> getClassesNear(Class<?> cls) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, getClassesLocation(cls));
		return out;
	}
	
	public static Set<String> getClasses(ClassLoader cl) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, getClassesLocation(cl));
		return out;
	}
	
	public static Set<String> getClasses(File fd) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, fd);
		return out;
	}
	
	public static void getClasses(Collection<String> set, File fd) throws Exception {
		if(fd.isDirectory()) {
			getClassesFolder(set, fd, "");
			return;
		}
		getClassesZip(set, new ZipInputStream(Nice.input(fd)));
	}
	
	public static void getClasses(Consumer<String> handler, File fd) throws Exception {
		if(fd.isDirectory()) {
			getClassesFolder(handler, fd, "");
			return;
		}
		getClassesZip(handler, new ZipInputStream(Nice.input(fd)));
	}
	
	public static Set<String> getClassesZip(ZipInputStream zip) throws Exception {
		Set<String> set = Nice.createSet();
		getClassesZip(set, zip);
		return set;
	}
	
	public static void getClassesZip(Collection<String> set, ZipInputStream zip) throws Exception {
		ZipEntry ent = null;
		while((ent = zip.getNextEntry()) != null) {
			if(!ent.isDirectory()) {
				String clsn = ent.getName();
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.add(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
			zip.closeEntry();
		}
		zip.close();
	}
	
	public static void getClassesZip(Consumer<String> handler, ZipInputStream zip) throws Exception {
		ZipEntry ent = null;
		while((ent = zip.getNextEntry()) != null) {
			if(!ent.isDirectory()) {
				String clsn = ent.getName();
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					handler.accept(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
			zip.closeEntry();
		}
		zip.close();
	}
	
	public static Thread[] getThreads() {
		try {
			getThreads = Thread.class.getDeclaredMethod("getThreads");
			getThreads.setAccessible(true);
			return (Thread[]) getThreads.invoke(null);
		} catch(Throwable t) {}
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		return threadSet.toArray(new Thread[threadSet.size()]);
	}
	
	public static Class<?> getClassSafe(String cls) {
		try {
			return Class.forName(cls);
		} catch (Throwable t) { }
		return null;
	}
	
	public static Class<?> getClassSafe(String cls, ClassLoader cl) {
		try {
			return Class.forName(cls, true, cl);
		} catch (Throwable t) {}
		return null;
	}
	
	public static interface OnFinishConsumer {
		void accept(CommandData cd);
	}
	
	public static class CommandData {
		protected boolean readError = false;
		protected boolean readOutput = true;
		protected boolean redirectError = false;
		protected int exitStatus = -1;
		protected OutputStream output = null;
		protected OutputStream error = null;
		protected Throwable throwable = null;
		protected int timeout = 0;
		/** Called on finish */
		protected OnFinishConsumer onFinish;
		/** Run in another thread? Ignored if onFinish == null */
		protected boolean multithreaded = false;
		/** Used when timedout > 0 */
		protected Result<Boolean> result = null;
		
		public Throwable getThrowable() {
			return this.throwable;
		}
		
		public CommandData setThrowable(Throwable t) {
			this.throwable = t;
			return this;
		}
		
		public boolean isReadError() {
			return readError;
		}
		public CommandData setReadError(boolean readError) {
			this.readError = readError;
			return this;
		}
		public boolean isReadOutput() {
			return readOutput;
		}
		public CommandData setReadOutput(boolean readOutput) {
			this.readOutput = readOutput;
			return this;
		}
		public boolean isRedirectError() {
			return redirectError;
		}
		public CommandData setRedirectError(boolean joinError) {
			this.redirectError = joinError;
			return this;
		}
		public int getExitStatus() {
			return exitStatus;
		}
		public CommandData setExitStatus(int exitStatus) {
			this.exitStatus = exitStatus;
			return this;
		}
		public boolean isExitOK() {
			return this.exitStatus == 0;
		}
		public OutputStream getOutput() {
			return output;
		}
		public CommandData setOutput(OutputStream output) {
			this.output = output;
			return this;
		}
		public OutputStream getError() {
			return error;
		}
		public CommandData setError(OutputStream error) {
			this.error = error;
			return this;
		}
		public int getTimeout() {
			return this.timeout;
		}
		public CommandData setTimeout(int tim) {
			this.timeout = (tim >= 0) ? tim : 0;
			return this;
		}
		
		public String getErrorString() {
			return (error == null) ? "" : error.toString();
		}
		
		public String getOutputString() {
			return (output == null) ? "" : output.toString();
		}
		
		public void writeOutput(byte[] buff, int off, int len) throws IOException {
			if(!this.readOutput || len == 0) return;
			if(this.output == null) this.output = Nice.createArrayOutput();
			this.output.write(buff, off, len);
		}
		
		public void writeOutput(InputStream in) throws IOException {
			if(!this.readOutput) {
				IUtils.readFullyNowhere(in);
				return;
			}
			if(this.output == null) this.output = Nice.createArrayOutput();
			if(this.output instanceof ArrayOutputStream) {
				((ArrayOutputStream) this.output).write(in);
			} else {
				IOUtils.copy(in, this.output);
			}
		}
		
		public void writeError(byte[] buff, int off, int len) throws IOException {
			if(!this.readError || len == 0) return;
			if(this.error == null) this.error = Nice.createArrayOutput();
			this.error.write(buff, off, len);
		}
		
		public void writeError(InputStream in) throws IOException {
			if(!this.readError) {
				IUtils.readFullyNowhere(in);
				return;
			}
			if(this.error == null) this.error = Nice.createArrayOutput();
			if(this.error instanceof ArrayOutputStream) {
				((ArrayOutputStream) this.error).write(in);
			} else {
				IOUtils.copy(in, this.error);
			}
		}
		
		public OnFinishConsumer getOnFinish() {
			return onFinish;
		}

		public CommandData setOnFinish(OnFinishConsumer onFinish) {
			this.onFinish = onFinish;
			return this;
		}

		public boolean isMultithreaded() {
			return multithreaded;
		}

		public CommandData setMultithreaded(boolean multithreaded) {
			this.multithreaded = multithreaded;
			return this;
		}
		
		public Result<Boolean> getTimeoutResult() {
			return this.result;
		}
		
		public CommandData setTimeoutResult(Result<Boolean> result) {
			this.result = result;
			return this;
		}
		
	};

	public static File getJRE() {
		return new File(System.getProperty("java.home"));
	}

	public static String executeAndGetOutput(String... args) {
		try {
			CommandData cd = executeDefault(args);
			if(cd.isExitOK()) return cd.getOutputString();
		} catch(Exception e) {}
		return null;
	}

	public static CommandData executeDefault(String... args) 
			throws IOException, InterruptedException {
		CommandData cd = new CommandData();
		execute(cd, args);
		return cd;
	}
	
	
	public static void execute(final CommandData out, final String... args) 
			throws IOException, InterruptedException {
		if(out.getTimeout() > 0) {
			executeSpecial(out, args);
			return;
		} else if(out.isMultithreaded()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						executeBlock(out, args);
					} catch (Exception e) {
						out.setThrowable(e);
					}
				}
			}).start();
		} else {
			executeBlock(out, args);
		}
	}
	
	protected static void executeBlock(CommandData out, String... args) 
			throws IOException, InterruptedException {
		if(out.getTimeout() > 0) {
			executeSpecial(out, args);
			return;
		}
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(out.isRedirectError());
		Process proc = pb.start();
		try(InputStream in = proc.getInputStream();
			InputStream err = proc.getErrorStream()) {
			out.writeOutput(in);
			out.writeError(err);
			proc.waitFor();
			out.setExitStatus(proc.exitValue());
			if(out.getOnFinish() != null)
				out.getOnFinish().accept(out);
		}
	}
	
	protected static void executeSpecial(final CommandData out, final String... args) 
			throws IOException, InterruptedException {
		Runnable rn = new Runnable() {
			@Override
			public void run() {
				TimeoutThread<Boolean> tt = new TimeoutThread<Boolean>(
					new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						executeBlock(out, args);
						return true;
					}
				}, out.getTimeout());
				Result<Boolean> res = new Result<Boolean>();
				res.setResult(false);
				tt.runSafe(res);
				out.setTimeoutResult(res);
			}
		};
		if(out.isMultithreaded()) {
			new Thread(rn).start();
		} else {
			rn.run();
		}
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
			String response = executeAndGetOutput("whereis", "javac");
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
			String path = executeAndGetOutput("where.exe", "javac");
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
