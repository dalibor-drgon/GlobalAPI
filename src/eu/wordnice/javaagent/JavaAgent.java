/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.wordnice.javaagent;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.wordnice.api.Api;

/**
 * This class is based on the OpenJPA's org.apache.openjpa.enhance.InstrumentationFactory.  It essentially does
 * its best to install an instrumentation agent.  The preferred or prescribed way to install an instrumentation agent
 * is to add the agent as an argument on the command line when starting the JVM.  This class attempts to do the
 * same thing after the JVM has already started.  Unfortunately, this is the only way we know of to attach an agent to
 * the JVM except by adding a "javaagent:..." flag on the command line.
 *
 * User: Kelly Tisdell
 */
public class JavaAgent {
	
	protected static final Logger LOG = Logger.getLogger("JavaAgent");
	
	protected static final String PROPERTY = "java.lang.Instrumentation";
	protected static final String PROPERTY_LIST = "java.lang.Instrumentation.QeueeList";
	protected static final String PROPERTY_TRY = "java.lang.Instrumentation.TryAgain";
	
	protected static final String IBM_VM_CLASS = "com.ibm.tools.attach.VirtualMachine";
	protected static final String SUN_VM_CLASS = "com.sun.tools.attach.VirtualMachine";
	protected static boolean isIBM = false;
	
	public static void setTryAgain(boolean bol) {
		System.getProperties().put(PROPERTY_TRY, bol);
	}
	
	public static boolean getTryAgain() {
		Object ta = System.getProperties().get(PROPERTY_TRY);
		if(ta instanceof Boolean) {
			return (Boolean) ta;
		}
		return true;
	}
	
	
	public static void setInstrumentation(Instrumentation ins) {
		if(ins != null) {
			System.getProperties().put(PROPERTY, ins);
			ready();
		}
	}
	
	/**
	 * @deprecated Use {@link JavaAgent#get()} instead
	 */
	@Deprecated
	public static Instrumentation getRawInstrumentation() {
		Object ins = System.getProperties().get(PROPERTY);
		if(ins != null && !(ins instanceof Instrumentation)) {
			ins = null;
			System.getProperties().remove(PROPERTY);
		}
		return (Instrumentation) ins;
	}
	
	public static void ready() {
		Collection<Runnable> onInit = getRunWhenReady();
		if(onInit != null && !onInit.isEmpty()) {
			Iterator<Runnable> runs = onInit.iterator();
			while(runs.hasNext()) {
				runs.next().run();
				runs.remove();
			}
		}
	}
	
	public static void runWhenReady(Runnable run) {
		Instrumentation ins = getRawInstrumentation();
		if(ins != null) {
			run.run();
			return;
		}
		getRunWhenReady().add(run);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Runnable> getRunWhenReady() {
		Object ins = System.getProperties().get(PROPERTY_LIST);
		if(ins == null || !(ins instanceof Collection)) {
			ins = new ArrayList<Runnable>();
			System.getProperties().put(PROPERTY_LIST, ins);
		}
		return (Collection<Runnable>) ins;
	}

	
	public static void premain(String args, Instrumentation in) {
		if(in != null) {
			LOG.info("[DEBUG] eu.wordnice.javaagent.JavaAgent: PreMain called!"
					+ "\n\t- Arguments: " + args
					+ "\n\t- Instrumentation: " + in);
			setInstrumentation(in);
		}
	}
	
	public static void agentmain(String args, Instrumentation in) {
		if(in != null) {
			LOG.info("[DEBUG] eu.wordnice.javaagent.JavaAgent: AgentMain called! "
					+ "(note that agent was loaded later dynamically by itself)"
					+ "\n\t- Arguments: " + args
					+ "\n\t- Instrumentation: " + in);
			setInstrumentation(in);
		}
	}
	
	/**
	 * This method returns the Instrumentation object provided by the JVM. If the Instrumentation object is null,
	 * it does its best to add an instrumentation agent to the JVM and then the instrumentation object.
	 * @return Instrumentation
	 */
	public static synchronized Instrumentation get() {
		Instrumentation ins = getRawInstrumentation();
		if(ins != null || !getTryAgain()) {
			return ins;
		}
		
		if(System.getProperty("java.vendor").toUpperCase().contains("IBM")) {
			isIBM = true;
		}

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				if(Api.isWindows()) {
					File to = null;
					File from = null;
					try {						
						File jdk = Api.getJDK();
						File jre = Api.getJRE();
						if(jdk == null || jre == null) {
							throw new NullPointerException("Cannot get JRE or JDK location! "
									+ "Please set JAVA_HOME variable! "
									+ "JRE[" + jre + "] JDK[" + jdk + "]");
						}
						
						to = new File(jre, "bin/attach.dll");
						from = new File(jdk, "jre/bin/attach.dll");
						if(!to.exists()) {
							Api.copyFile(to, from);
						}
					} catch(Throwable t2) {
						LOG.log(Level.SEVERE, "Little error occured while checking "
								+ "and copying native libraries... "
								+ "If you see another error from JavaAgent below, please "
								+ "make sure you have got attach.dll native library "
								+ "in your JRE/bin path."
								+ ((from == null) ? "" : " (We tried copying [" 
										+ from.getAbsolutePath() + "] to [" 
										+ to.getAbsolutePath() + "])")
								+ " Continue.", t2);
					}
				} else {
					LOG.info("Just to know: If you see errors from JavaAgent "
							+ "below, make sure you have installed 'attach' "
							+ "native library instaled near [" + Api.getJDK() + "]");
				}
				
				
				/*try {
					if (!JavaAgent.class.getClassLoader().equals(
							ClassLoader.getSystemClassLoader())) {
						return null;
					}
				} catch (Throwable t) {
					return null;
				}*/
				File toolsJar = null;
				// When running on IBM, the attach api classes are packaged in vm.jar which is a part
				// of the default vm classpath.
				if (!isIBM) {
					// If we can't find the tools.jar and we're not on IBM we can't load the agent.
					toolsJar = findToolsJar();
					if (toolsJar == null) {
						return null;
					}
				}

				Class<?> vmClass = loadVMClass(toolsJar);
				if (vmClass == null) {
					return null;
				}
				String agentPath = getAgentJar();
				if (agentPath == null) {
					return null;
				}
				loadAgent(agentPath, vmClass);
				return null;
			}
		});

		setTryAgain(false);
		return getRawInstrumentation();
	}

	private static File findToolsJar() {
		File javaHomeFile = Api.getJDK();
		if(javaHomeFile == null) {
			return null;
		}

		File toolsJarFile = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
		if(!toolsJarFile.exists()) {
			// If we're on an IBM SDK, then remove /jre off of java.home and try again.
			if(javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre")) {
				javaHomeFile = javaHomeFile.getParentFile();
				toolsJarFile = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
			} else if(System.getProperty("os.name").toLowerCase().contains("mac")) {
				// If we're on a Mac, then change the search path to use ../Classes/classes.jar.
				if (javaHomeFile.getAbsolutePath().endsWith(File.separator + "Home")) {
					javaHomeFile = javaHomeFile.getParentFile();
					toolsJarFile = new File(javaHomeFile, "Classes" + File.separator + "classes.jar");
				}
			}
		}
		if(!toolsJarFile.exists()) {
			return null;
		} else {
			return toolsJarFile;
		}
	}

	private static String createAgentJar() throws IOException {
		File file =
				File.createTempFile(JavaAgent.class.getName(), ".jar");
		file.deleteOnExit();

		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
		zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(zout));

		writer.println("Agent-Class: " + JavaAgent.class.getName());
		writer.println("Can-Redefine-Classes: true");
		// IBM doesn't support retransform
		writer.println("Can-Retransform-Classes: " + Boolean.toString(!isIBM));

		writer.close();

		return file.getAbsolutePath();
	}

	private static String getAgentJar() {
		File agentJarFile = null;
		// Find the name of the File that this class was loaded from. That
		// jar *should* be the same location as our agent.
		CodeSource cs =
				JavaAgent.class.getProtectionDomain().getCodeSource();
		if (cs != null) {
			URL loc = cs.getLocation();
			if (loc != null) {
				agentJarFile = new File(loc.getFile());
			}
		}

		// Determine whether the File that this class was loaded from has this
		// class defined as the Agent-Class.
		boolean createJar = false;
		if (cs == null || agentJarFile == null
				|| agentJarFile.isDirectory()) {
			createJar = true;
		} else if (!validateAgentJarManifest(agentJarFile, JavaAgent.class.getName())) {
			// We have an agentJarFile, but this class isn't the Agent-Class.
			createJar = true;
		}

		String agentJar;
		if (createJar) {
			try {
				agentJar = createAgentJar();
			} catch (IOException ioe) {
				agentJar = null;
			}
		} else {
			agentJar = agentJarFile.getAbsolutePath();
		}

		return agentJar;
	}

	private static void loadAgent(String agentJar, Class<?> vmClass) {
		try {
			// first obtain the PID of the currently-running process
			// ### this relies on the undocumented convention of the
			// RuntimeMXBean's
			// ### name starting with the PID, but there appears to be no other
			// ### way to obtain the current process' id, which we need for
			// ### the attach process
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			String pid = runtime.getName();
			if (pid.contains("@"))
				pid = pid.substring(0, pid.indexOf("@"));

			// JDK1.6: now attach to the current VM so we can deploy a new agent
			// ### this is a Sun JVM specific feature; other JVMs may offer
			// ### this feature, but in an implementation-dependent way
			Object vm = vmClass.getMethod("attach", new Class<?>[]{String.class}).invoke(null, pid);
			vmClass.getMethod("loadAgent", new Class[]{String.class}).invoke(vm, agentJar);
			vmClass.getMethod("detach", new Class[]{}).invoke(vm);
		} catch(Throwable t) {
			if(t instanceof InvocationTargetException) {
				Throwable t2 = ((InvocationTargetException) t).getCause();
				if(t2 != null) {
					t = t2;
				}
			}
			LOG.log(Level.SEVERE, "Problem loading the agent", t);
		}
	}

	private static Class<?> loadVMClass(File toolsJar) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			String cls = SUN_VM_CLASS;
			if(isIBM) {
				cls = IBM_VM_CLASS;
			} else {
				loader = new URLClassLoader(new URL[]{toolsJar.toURI().toURL()}, loader);
			}
			return loader.loadClass(cls);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to load the virtual machine class", e);
		}
		return null;
	}

	private static boolean validateAgentJarManifest(File agentJarFile,
													String agentClassName) {
		try {
			JarFile jar = new JarFile(agentJarFile);
			Manifest manifest = jar.getManifest();
			if (manifest == null) {
				try {
					jar.close();
				} catch(Exception ign) {}
				return false;
			}
			Attributes attributes = manifest.getMainAttributes();
			String ac = attributes.getValue("Agent-Class");
			try {
				jar.close();
			} catch(Exception ign) {}
			if(ac != null && ac.equals(agentClassName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Unexpected exception occured.", e);
		}
		return false;
	}
}