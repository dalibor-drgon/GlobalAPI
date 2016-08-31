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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
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
import wordnice.api.Nice;
import wordnice.api.Nice.BHandler;
import wordnice.coll.ImmArray;
import wordnice.optimizer.builtin.OptimizeCharacter;
import wordnice.streams.IUtils;
import wordnice.utils.UnsafeAPI;

public class Optimizer {
	
	protected static Collection<String> bootstrapped = null;
	protected static Collection<BootstrapFile> pendingBootstrap = null;
	protected static Collection<String> optimized = null;
	protected static Collection<Optimizable> pendingOptimizations = null;
	
	protected static final String propOptimized = "wordnice.optimizer.Optimizer.optimized";
	protected static final String propBootstrapped = "wordnice.optimizer.Optimizer.bootstrapped";
	protected static final String propPendingOptimizations = "wordnice.optimizer.Optimizer.pendingOptimizes";
	protected static final String propPendingBootstrap = "wordnice.optimizer.Optimizer.pendingBootstrap";
	
	protected static Timer timer = new Timer();
	protected static TimerTask currentTask = null;
	protected static Logger log = Logger.getLogger("WordniceOptimizer");
	
	protected static int wait_time = 5000; //5sec
	
	public static synchronized void forceOptimizations() {
		if(getPendingBootstrap().isEmpty() && getPendingOptimizations().isEmpty()) {
			log.info("Optimizations were forced, but there is nothing "
					+ "to optimize and nothing to add to bootstrap. Aborting...");
		} else {
			log.info("Forcing optimizations...");
			optimize();
		}
	}
	
	public static synchronized void forceBootstrap() {
		Collection<String> outBoot = getBootstrapped();
		Collection<BootstrapFile> boot = getPendingBootstrap();
		if(boot.isEmpty()) {
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
	
	@SuppressWarnings("deprecation")
	protected static synchronized void optimize() {
		Collection<String> outBoot = getBootstrapped();
		Collection<BootstrapFile> boot = getPendingBootstrap();
		final Collection<String> outOpts = getOptimized();
		Collection<Optimizable> opts = getPendingOptimizations();
		if(boot.isEmpty() && opts.isEmpty()) {
			return;
		}
		Unsafe unf = UnsafeAPI.getUnsafe();
		Instrumentation ins = UnsafeAPI.getInstrumentation();
		
		Thread[] threads = UnsafeAPI.getThreads();
		boolean[] unlock = new boolean[threads.length];
		
		log.info("Preparing to optimizations...");
		
		if(!boot.isEmpty()) {
			log.info("Adding " + boot.size() + " files to bootstrap...");
			try {
				bootstrap(outBoot, boot);
				log.info("Bootstrap done...");
			} catch(Throwable t) {
				log.log(Level.SEVERE, "Bootstrap operation failed with error...", t);
				Nice.checkUnsafeError(t);
			}
		}
		if(opts.isEmpty()) {
			log.info("Optimizations aborted. Bootstrap was loaded, but there is nothing to optimize!");
			return;
		}
		
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
			Optimizable opt = it.next();
			it.remove();
			String nam = opt.getName();
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
			Optimizable opt = it.next();
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
	
	private static synchronized void bootstrap(Collection<String> outBoot, Collection<BootstrapFile> boot) 
			throws IOException {
		Instrumentation ins = UnsafeAPI.getInstrumentation();
		
		File temp = File.createTempFile("mainapi_boostrap_", ".temp.jar");
		temp.deleteOnExit();
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(temp));
		
		List<String> lst = Nice.createList();
		byte[] buff = new byte[Nice.bufferSize];
		boolean hadManifest = false;
		Iterator<BootstrapFile> it = boot.iterator();
		while(it.hasNext()) {
			BootstrapFile bf = it.next();
			it.remove();
			String nam = bf.getName();
			if(nam == null || bf == null) {
				continue;
			}
			if(nam.equals(JarFile.MANIFEST_NAME)) {
				hadManifest = true;
			} else {
				lst.add(nam);
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
	protected static synchronized Collection<BootstrapFile> getPendingBootstrap() {
		if(pendingBootstrap == null) {
			try {
				pendingBootstrap = (Collection<BootstrapFile>) System.getProperties().get(propPendingBootstrap);
			} catch(Throwable t) {
				Nice.checkError(t);
			}
		}
		if(pendingBootstrap == null) {
			pendingBootstrap = new ConcurrentLinkedQueue<BootstrapFile>();
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
	
	protected static synchronized void checkTimer() {
		
	}
	
	public static synchronized void addOptimization(Optimizable opt) {
		if(opt == null) throw new IllegalArgumentException("Optimizable == null");
		getPendingOptimizations().add(opt);
		checkTimer();
	}
	
	public static synchronized void addOptimizations(Optimizable[] opt) {
		if(opt == null) throw new IllegalArgumentException("Optimizable[] == null");
		if(opt.length == 0) return;
		getPendingOptimizations().addAll(new ImmArray<Optimizable>(opt));
		checkTimer();
	}
	
	public static synchronized void addOptimizations(Collection<Optimizable> opt) {
		if(opt == null) throw new IllegalArgumentException("Collection<Optimizable> == null");
		if(opt.size() == 0) return;
		getPendingOptimizations().addAll(opt);
		checkTimer();
	}

	public static ClassDefinition createDefinition(CtClass clz) throws Throwable {
		return new ClassDefinition(
				Class.forName(clz.getName(), false, null),
				clz.toBytecode());
	}
	
	public static ClassDefinition createDefinition(String name, byte[] bytes) throws Throwable {
		return new ClassDefinition(
				Class.forName(name, false, null),
				bytes);
	}
	
	public static void addBootstrap(String name, URL url) {
		if(name == null || url == null) return;
		addBootstrap0(new BootstrapFile.URLStream(name, url));
		checkTimer();
	}
	
	public static void addBootstrap(String name, File fl) {
		if(name == null || fl == null) return;
		addBootstrap0(new BootstrapFile.FileStream(name, fl));
		checkTimer();
	}
	
	public static void addBootstrap(BootstrapFile fl) {
		if(fl == null) return;
		addBootstrap0(fl);
		checkTimer();
	}
	
	protected static void addBootstrap0(BootstrapFile fl) {
		getPendingBootstrap().add(fl);
	}
	
	public static ClassDefinition createDefinition(Class<?> clz, byte[] bytes) throws Throwable {
		return new ClassDefinition(clz,bytes);
	}
	
	/**
	 * @param folder Folder with binary classes to add
	 */
	public static void addFolder(File folder) {
		addFolder(folder, null, null);
	}
	
	public static void addFolder(File folder, String pref) {
		addFolder(folder, pref, null);
	}
	
	public static void addFolder(File folder, BHandler<String> accepter) {
		addFolder(folder, null, null);
	}
	
	public static void addFolder(File folder, String pref, BHandler<String> accepter) {
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
				addFolder(cur, nextDir, accepter);
			} else {
				String curFullName = pref + curName;
				if(accepter != null && !accepter.handle(curFullName)) continue;
				addBootstrap0(new BootstrapFile.FileStream(curFullName, cur));
			}
		}
		checkTimer();
	}
	
	/**
	 * @param file Folder or jar file to add binary classes
	 */
	public static void addBootstrap(File file) 
			throws ZipException, IOException {
		addBootstrap(file, null, null);
	}
	
	public static void addBootstrap(File file, String prefix) 
			throws ZipException, IOException {
		addBootstrap(file, prefix, null);
	}
	
	public static void addBootstrap(File file, BHandler<String> accepter) 
			throws ZipException, IOException {
		addBootstrap(file, null, accepter);
	}
	
	public static void addBootstrap(File file, String prefix, BHandler<String> accepter)
			throws ZipException, IOException {
		if(prefix == null) prefix = "";
		if(file.isDirectory()) {
			addFolder(file, prefix, accepter);
		} else {
			ZipFile zf = new ZipFile(file);
			try {
				Enumeration<? extends ZipEntry> it = zf.entries();
				while(it.hasMoreElements()) {
					ZipEntry ze = it.nextElement();
					String name = prefix + ze.getName();
					if(accepter != null && !accepter.handle(name)) continue;
					addBootstrap0(new BootstrapFile.ZIPStream(name, 
							file, ze.getName()));
				}
			} finally {
				zf.close();
			}
		}
	}
	
	public static void main(String...strings) throws Throwable {
		Optimizer.addBootstrap(UnsafeAPI.getClassesLocation(Optimizer.class), new BHandler<String>() {

			@Override
			public boolean handle(String val) {
				return val.startsWith("java") || val.startsWith("optimized");
			}
			
		});
		Optimizer.addOptimization(new OptimizeCharacter());
		Optimizer.forceOptimizations();
		Character.toLowerCase('A');
		Character.toLowerCase((int)'A');
	}
	
}
