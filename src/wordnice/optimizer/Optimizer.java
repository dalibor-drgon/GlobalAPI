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

package wordnice.optimizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javassist.ClassPool;
import javassist.CtClass;
import sun.misc.Unsafe;
import wordnice.api.InitNiceAPI;
import wordnice.api.Nice;
import wordnice.coll.ImmArray;
import wordnice.utils.JavaHooker;
import wordnice.utils.JavaUtils;

public class Optimizer {
	
	protected static Collection<String> bootstrapped = null;
	protected static Collection<Bootstrap.EntryFile> pendingBootstrap = null;
	protected static Collection<String> optimized = null;
	protected static Collection<Optimizable> pendingOptimizations = null;
	
	protected static final String propOptimized = "wordnice.optimizer.Optimizer.optimized";
	protected static final String propBootstrapped = "wordnice.optimizer.Optimizer.bootstrapped";
	protected static final String propPendingOptimizations = "wordnice.optimizer.Optimizer.pendingOptimizes";
	protected static final String propPendingBootstrap = "wordnice.optimizer.Optimizer.pendingBootstrap";
	
	protected static Timer timer = new Timer();
	protected static TimerTask currentTask = null;
	protected static Logger log = Logger.getLogger("WordniceOptimizer");
	
	protected static int waitTime = 5000; //5sec
	
	public static synchronized void forceOptimizations() {
		forceOptimizations(getPendingOptimizations());
		getPendingOptimizations().clear();
	}
	
	public static synchronized void forceOptimizations(Collection<Optimizable> opts) {
		if(isOptimizableEmpty(opts)) {
			log.info("Optimizations were forced, but there is nothing "
					+ "to optimize. Aborting...");
		} else {
			log.info("Forcing optimizations...");
			optimize(opts);
		}
	}
	
	public static synchronized void forceBootstrap() {
		forceBootstrap(getPendingBootstrap());
		getPendingBootstrap().clear();
	}
	
	public static synchronized void forceBootstrap(Collection<Bootstrap.EntryFile> boot) {
		Collection<String> outBoot = getBootstrapped();
		if(isBootstrapEmpty(boot)) {
			log.info("Bootstrap forced, but nothing to add. Aborting...");
		} else {
			log.info("Bootstrap forced... Adding " + boot.size() + " files to bootstrap...");
			try {
				bootstrap(outBoot, boot);
				log.info("Bootstrap done... Finished.");
			} catch(Throwable t) {
				log.log(Level.SEVERE, "Bootstrap operation failed with error...", t);
				Nice.checkUnsafeError(t);
			}
		}
	}
	
	
	protected static synchronized void bootstrap() {
		Collection<Bootstrap.EntryFile> boot = getPendingBootstrap();
		Collection<String> outBoot = getBootstrapped();
		removeBootstrapped(boot);
		if(!isBootstrapEmpty(boot)) {
			log.info("Adding " + boot.size() + " files to bootstrap...");
			try {
				bootstrap(outBoot, boot);
				log.info("Bootstrap done... Finished.");
				boot.clear();
			} catch(Throwable t) {
				log.log(Level.SEVERE, "Bootstrap operation failed with error...", t);
				boot.clear();
				Nice.checkUnsafeError(t);
			}
		}
	}
	
	protected static synchronized void optimize() {
		optimize(getPendingOptimizations());
		getPendingOptimizations().clear();
	}
	
	@SuppressWarnings("deprecation")
	protected static synchronized void optimize(Collection<Optimizable> opts) {
		final Collection<String> outOpts = getOptimized();
		if(isOptimizableEmpty(opts)) {
			return;
		}
		
		log.info("Preparing to optimizations...");
		
		Unsafe unf = JavaUtils.getUnsafe();
		Instrumentation ins = JavaUtils.getInstrumentation();
		
		Thread[] threads = JavaUtils.getThreads();
		boolean[] unlock = new boolean[threads.length];
		
		log.info("Suspending threads...");
		for(int i = 0, n = threads.length; i < n; i++) {
			Thread t = threads[i];
			if(!Thread.currentThread().equals(t) 
					&& !t.getThreadGroup().getName().equals("system")) {
				try {
					t.suspend();
					unlock[i] = true;
				} catch(Throwable ign) {
					Nice.checkUnsafeError(ign);
				}
			}
		}
		
		log.info("Doing optimalizations...");
		Iterator<Optimizable> it = opts.iterator();
		Optimizable opt = null;
		while(it.hasNext())
			if(!wasOptimized(opt = it.next()))
				opt.beforeOptimize();
		it = opts.iterator();
		ClassPool cp = new ClassPool(true);
		
		List<ClassDefinition> cdlist = Nice.createList();
		final List<Optimizable> afterOptimize = Nice.createList();
		Optimizable.OptimizedChecker checker = new Optimizable.OptimizedChecker() {
			@Override
			public boolean has(String name) {
				if(name == null) return false;
				if(outOpts.contains(name)) return true;
				for(Optimizable a : afterOptimize)
					if(a.getName().equals(name)) return true;
				return false;
			}
		};
		
		while(it.hasNext()) {
			opt = it.next();
			String nam = opt.getName();
			if(wasOptimized(opt)) continue;
			if(!opt.canOptimize(checker)) {
				log.info("Cannot optimize because of depencies " + nam + ", ignoring...");
				continue;
			}
			log.info("Optimizing " + nam + "...");
			try {
				opt.optimize(cp, cdlist);
				afterOptimize.add(opt);
			} catch(Throwable th) {
				log.log(Level.SEVERE, "Error while optimizing " + nam + ":", th);
				Nice.checkUnsafeError(th);
			}
		}
		
		ClassDefinition[] cd = cdlist.toArray(new ClassDefinition[cdlist.size()]);
		
		log.info("Locking class objects...");
		for(int i = 0, n = cd.length; i < n; i++) {
			unf.monitorEnter(cd[i].getDefinitionClass());
		}
		
		log.info("Redefining " + cd.length + " classes...");
		boolean status = true;
		try {
			ins.redefineClasses(cd);
			log.info("Sucessfully redefined! Unlocking classes...");
			status = true;
		} catch(Throwable t) {
			status = false;
			log.log(Level.SEVERE, "Error while redefining ALL THE CLASSES! "
					+ "NO optimizations were done! Aborting...", t);
			log.info("Unlocking classes...");
			Nice.checkUnsafeError(t);
		}
		
		for(int i = 0, n = cd.length; i < n; i++) {
			unf.monitorExit(cd[i].getDefinitionClass());
		}
		
		log.info("Calling afterOptimize()...");
		it = afterOptimize.iterator();
		while(it.hasNext()) {
			opt = it.next();
			try {
				opt.afterOptimize(cp);
				outOpts.add(opt.getName());
			} catch(Throwable t) {
				log.log(Level.SEVERE, "Error occured on "  + opt.getName() + ".afterOptimize(). Continue..." , t);
				Nice.checkUnsafeError(t);
			}
		}
		
		log.info("Resuming threads!");
		for(int i = 0, n = threads.length; i < n; i++) {
			if(unlock[i]) {
				try {
					threads[i].resume();
				} catch(Throwable ign) {
					Nice.checkUnsafeError(ign);
				}
			}
		}
				
		log.info("Optimalization process done! "
				+ ((status) ? "Success!"
						: "Errors occured!"));
	}
	
	private static synchronized void bootstrap(Collection<String> outBoot, Collection<Bootstrap.EntryFile> boot) 
			throws IOException {
		Instrumentation ins = JavaUtils.getInstrumentation();
		
		File temp = File.createTempFile("mainapi_boostrap_", ".temp.jar");
		temp.deleteOnExit();
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(temp));
		
		List<String> lst = Nice.createList();
		byte[] buff = new byte[Nice.bufferSize];
		boolean hadManifest = false;
		Iterator<Bootstrap.EntryFile> it = boot.iterator();
		while(it.hasNext()) {
			Bootstrap.EntryFile bf = it.next();
			String nam = bf.getName();
			if(nam == null || bf == null || wasBootstrapped(bf)) {
				continue;
			}
			if(nam.equals(JarFile.MANIFEST_NAME)) {
				hadManifest = true;
			} else {
				lst.add(bf.getUniqueName());
			}
			jar.putNextEntry(new ZipEntry(nam));
			bf.writeTo(jar, buff);
			jar.closeEntry();
		}
		
		if(!hadManifest) {
			jar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
			jar.write("Manifest-Version: 1.0\r\n".getBytes());
			jar.closeEntry();
		}
		
		jar.finish();
		jar.close();
		
		ins.appendToBootstrapClassLoaderSearch(new JarFile(temp));
		ins.appendToSystemClassLoaderSearch(new JarFile(temp));
		
		outBoot.addAll(lst);
	}
	
	public static synchronized boolean wasBootstrapped(Bootstrap.EntryFile bf) {
		return getBootstrapped().contains(bf.getUniqueName());
	}
	
	public static synchronized boolean wasBootstrapped(String key) {
		return getBootstrapped().contains(key);
	}
	
	public static synchronized boolean wasOptimized(Optimizable opt) {
		return getOptimized().contains(opt.getName());
	}
	
	public static synchronized boolean wasOptimized(String key) {
		return getOptimized().contains(key);
	}
	
	public static synchronized void removeOptimized(Collection<Optimizable> col) {
		if(col instanceof List) {
			//if possible go from backward to minimize reallocations
			ListIterator<Optimizable> it = ((List<Optimizable>) col).listIterator(col.size());
			while(it.hasPrevious())
				if(wasOptimized(it.previous()))
					it.remove();
		} else {
			Iterator<Optimizable> it = col.iterator();
			while(it.hasNext())
				if(wasOptimized(it.next()))
					it.remove();
		}
	}
	
	public static synchronized void removeBootstrapped(Collection<Bootstrap.EntryFile> col) {
		if(col instanceof List) {
			//if possible go from backward to minimize reallocations
			ListIterator<Bootstrap.EntryFile> it = ((List<Bootstrap.EntryFile>) col).listIterator(col.size());
			while(it.hasPrevious())
				if(wasBootstrapped(it.previous()))
					it.remove();
		} else {
			Iterator<Bootstrap.EntryFile> it = col.iterator();
			while(it.hasNext())
				if(wasBootstrapped(it.next()))
					it.remove();
		}
	}
	
	public static boolean isBootstrapEmpty(Collection<Bootstrap.EntryFile> col) {
		return isBootstrapEmpty(col.iterator());
	}
	
	public static boolean isBootstrapEmpty(Iterator<Bootstrap.EntryFile> it) {
		while(it.hasNext()) 
			if(!wasBootstrapped(it.next()))
				return false;
		return true;
	}
	
	public static boolean isOptimizableEmpty(Collection<Optimizable> col) {
		return isOptimizableEmpty(col.iterator());
	}
	
	public static boolean isOptimizableEmpty(Iterator<Optimizable> it) {
		while(it.hasNext()) 
			if(!wasOptimized(it.next()))
				return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	public static synchronized Collection<String> getBootstrapped() {
		if(bootstrapped == null) {
			try {
				bootstrapped = (Collection<String>) System.getProperties().get(propBootstrapped);
			} catch(Throwable t) {
				Nice.checkError(t);
			}
		}
		if(bootstrapped == null) {
			bootstrapped = new ConcurrentLinkedQueue<String>();
		}
		System.getProperties().put(propBootstrapped, bootstrapped);
		return bootstrapped;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized Collection<String> getOptimized() {
		if(optimized == null) {
			try {
				optimized = (Collection<String>) System.getProperties().get(propOptimized);
			} catch(Throwable t) {
				Nice.checkError(t);
			}
		}
		if(optimized == null) {
			optimized = new ConcurrentLinkedQueue<String>();
		}
		System.getProperties().put(propOptimized, optimized);
		return optimized;
	}
	
	@SuppressWarnings("unchecked")
	protected static synchronized Collection<Bootstrap.EntryFile> getPendingBootstrap() {
		if(pendingBootstrap == null) {
			try {
				pendingBootstrap = (Collection<Bootstrap.EntryFile>) System.getProperties().get(propPendingBootstrap);
			} catch(Throwable t) {
				Nice.checkError(t);
			}
		}
		if(pendingBootstrap == null) {
			pendingBootstrap = new ConcurrentLinkedQueue<Bootstrap.EntryFile>();
		}
		System.getProperties().put(propPendingBootstrap, pendingBootstrap);
		return pendingBootstrap;
	}
	
	@SuppressWarnings("unchecked")
	protected static synchronized Collection<Optimizable> getPendingOptimizations() {
		if(pendingOptimizations == null) {
			try {
				pendingOptimizations = (Collection<Optimizable>) System.getProperties().get(propPendingOptimizations);
			} catch(Throwable t) {
				Nice.checkError(t);
			}
		}
		if(pendingOptimizations == null) {
			pendingOptimizations = new ConcurrentLinkedQueue<Optimizable>();
		}
		System.getProperties().put(propPendingOptimizations, pendingOptimizations);
		return pendingOptimizations;
	}
	
	protected static synchronized void delayTimer() {
		if(currentTask != null) {
			currentTask.cancel();
			timer.purge();
		}
		currentTask = new TimerTask() {
			@Override
			public void run() {
				bootstrap();
				optimize();
			}};
		timer.schedule(currentTask, waitTime);
	}
	
	public static synchronized void addOptimization(Optimizable opt) {
		if(opt == null) throw new IllegalArgumentException("Optimizable == null");
		getPendingOptimizations().add(opt);
		delayTimer();
	}
	
	public static synchronized void addOptimizations(Optimizable[] opt) {
		if(opt == null) throw new IllegalArgumentException("Optimizable[] == null");
		if(opt.length == 0) return;
		getPendingOptimizations().addAll(new ImmArray<Optimizable>(opt));
		delayTimer();
	}
	
	public static synchronized void addOptimizations(Collection<Optimizable> opt) {
		if(opt == null) throw new IllegalArgumentException("Collection<Optimizable> == null");
		if(opt.size() == 0) return;
		getPendingOptimizations().addAll(opt);
		delayTimer();
	}
	
	public static Class<?> forName(String name)
			throws ClassNotFoundException {
		try {
			return Class.forName(name, false, null);
		} catch(ClassNotFoundException nfe) {
			return Class.forName(name);
		}
	}

	public static ClassDefinition createDefinition(CtClass clz) throws Throwable {
		return new ClassDefinition(
				forName(clz.getName()),
				clz.toBytecode());
	}
	
	public static ClassDefinition createDefinition(String name, byte[] bytes) throws Throwable {
		return new ClassDefinition(
				forName(name),
				bytes);
	}
	
	public static ClassDefinition createDefinition(Class<?> clz, byte[] bytes) throws Throwable {
		return new ClassDefinition(clz, bytes);
	}
	
	public static synchronized void addBootstrap(String name, String unique, URL url) {
		if(name == null || url == null || unique == null) return;
		addBootstrap0(new Bootstrap.URLStream(name, unique, url));
		delayTimer();
	}
	
	public static synchronized void addBootstrap(String name, String unique, File fl) {
		if(name == null || fl == null || unique == null) return;
		addBootstrap0(new Bootstrap.FileStream(name, unique, fl));
		delayTimer();
	}
	
	public static synchronized void addBootstrapZIP(String name, String unique, File fl, String entry) {
		if(name == null || fl == null || entry == null || unique == null) return;
		addBootstrap0(new Bootstrap.ZIPStream(name, unique, fl, entry));
		delayTimer();
	}
	
	public static synchronized void addBootstrap(Bootstrap.EntryFile fl) {
		if(fl == null) return;
		addBootstrap0(fl);
		delayTimer();
	}
	
	protected static void addBootstrap0(Bootstrap.EntryFile fl) {
		getPendingBootstrap().add(fl);
	}
	
	public static void addBootstrap(
			Collection<Bootstrap.EntryFile> out, String name, String unique, URL url) {
		if(name == null || url == null || unique == null) return;
		out.add(new Bootstrap.URLStream(name, unique, url));
	}
	
	public static void addBootstrap(
			Collection<Bootstrap.EntryFile> out, String name, String unique, File fl) {
		if(name == null || fl == null || unique == null) return;
		out.add(new Bootstrap.FileStream(name, unique, fl));
	}
	
	public static void addBootstrapZIP(
			Collection<Bootstrap.EntryFile> out, String name, String unique, File fl, String entry) {
		if(name == null || fl == null || entry == null || unique == null) return;
		out.add(new Bootstrap.ZIPStream(name, unique, fl, entry));
	}
	
	/**
	 * @param folder Folder with binary classes to add
	 */
	
	public static synchronized void addBootstrapFolder(File folder, Bootstrap.Handler accepter) {
		addBootstrapFolder(folder, null, accepter);
	}
	
	public static synchronized void addBootstrapFolder(File folder, String pref, Bootstrap.Handler accepter) {
		addBootstrapFolder(getPendingBootstrap(), folder, pref, accepter);
		delayTimer();
	}
	
	public static void addBootstrapFolder(Collection<Bootstrap.EntryFile> out, File folder, Bootstrap.Handler accepter) {
		addBootstrapFolder(out, folder, null, accepter);
	}
	
	public static void addBootstrapFolder(Collection<Bootstrap.EntryFile> out, File folder, String pref, Bootstrap.Handler accepter) {
		if(pref == null) pref = "";
		String[] files = folder.list();
		if(files == null || files.length == 0) return;
		int i = 0;
		for(; i < files.length; i++) {
			String curName = files[i];
			File cur = new File(folder, curName);
			if(cur.isDirectory()) {
				String nextDir = pref + curName + "/";
				//if(accepter != null && !accepter.handle(nextDir)) continue;
				addBootstrapFolder(out,cur, nextDir, accepter);
			} else {
				String curFullName = pref + curName;
				String uniName = accepter.canBootstrap(curFullName);
				if(uniName != null) 
					out.add(new Bootstrap.FileStream(curFullName, uniName, cur));
			}
		}
	}
	
	/**
	 * @param file Folder or jar file to add binary classes
	 */
	public static synchronized void addBootstrap(File file, Bootstrap.Handler accepter) 
			throws ZipException, IOException {
		addBootstrap(file, null, accepter);
	}
	
	public static synchronized void addBootstrap(File file, String prefix, Bootstrap.Handler accepter)
			throws ZipException, IOException {
		addBootstrap(getPendingBootstrap(), file, prefix, accepter);
		delayTimer();
	}
	
	public static void addBootstrap(Collection<Bootstrap.EntryFile> out, File file, Bootstrap.Handler accepter) 
			throws ZipException, IOException {
		addBootstrap(out, file, null, accepter);
	}
	
	public static void addBootstrap(Collection<Bootstrap.EntryFile> out, File file, String prefix, Bootstrap.Handler accepter)
			throws ZipException, IOException {
		if(prefix == null) prefix = "";
		if(file.isDirectory()) {
			addBootstrapFolder(out, file, prefix, accepter);
		} else {
			ZipFile zf = new ZipFile(file);
			try {
				Enumeration<? extends ZipEntry> it = zf.entries();
				while(it.hasMoreElements()) {
					ZipEntry ze = it.nextElement();
					String name = prefix + ze.getName();
					String uniName = accepter.canBootstrap(name);
					if(uniName != null) 
						out.add(new Bootstrap.ZIPStream(name, 
								uniName, file, ze.getName()));
				}
			} finally {
				zf.close();
			}
		}
	}
	
	public static void main(String...strings) throws Throwable {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s] %5$s%6$s%n");
		System.out.println("String from array: " + JavaHooker.string("tst".toCharArray()));
		/** INIT */
		InitNiceAPI.initAll(Logger.getLogger("NiceAPI"));
		/** INITED */
		System.out.println("String from array: " + JavaHooker.string("tst".toCharArray()));
	}
	
}
