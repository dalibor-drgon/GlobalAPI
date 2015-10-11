package eu.wordnice.javaagent;

import java.lang.instrument.Instrumentation;

public class JavaAgent {
	
	/**
	 * MANIFEST.MF
	 * 
	 * 	...
	 * 	Premain-Class: eu.wordnice.javaagent.JavaAgent
	 * 	Can-Redefine-Classes: true
	 */
	
	private static volatile Instrumentation instr;
	
	public static void premain(String args, Instrumentation inst) {
		System.out.println("[DEBUG] eu.wordnice.javaagent.JavaAgent: Premain called!"
				+ "\n\t- Arguments: " + args
				+ "\n\t- Instrumentation: " + inst);
		if(inst != null) {
			instr = inst;
		}
	}
	
	public static Instrumentation get() {
		return instr;
	}
}
