package eu.wordnice.api;

public class SystemLogg implements Logg {

	public String prefix = null;
	public boolean displayDebug = false;
	
	public SystemLogg() {}
	
	public SystemLogg(String prefix) {
		this.prefix = prefix + " ";
	}
	
	@Override
	public void debug(String str) {
		if(!this.displayDebug) {
			return;
		}
		if(this.prefix != null) {
			System.out.println("[DEBUG] " + this.prefix + str);
		} else {
			System.out.println("[DEBUG] " + str);
		}
	}

	@Override
	public void info(String str) {
		if(this.prefix != null) {
			System.out.println("[INFO] " + this.prefix + str);
		} else {
			System.out.println("[INFO] " + str);
		}
	}

	@Override
	public void warn(String str) {
		if(this.prefix != null) {
			System.out.println("[WARN] " + this.prefix + str);
		} else {
			System.out.println("[WARN] " + str);
		}
	}

	@Override
	public void severe(String str) {
		if(this.prefix != null) {
			System.err.println("[SEVERE] " + this.prefix + str);
		} else {
			System.err.println("[SEVERE] " + str);
		}
	}

	@Override
	public void severe(Throwable t) {
		if(this.prefix != null) {
			System.err.print("[SEVERE] " + this.prefix + ": ");
		} else {
			System.err.print("[SEVERE] <throwable>: ");
		}
		t.printStackTrace(System.err);
	}

	@Override
	public void severe(String str, Throwable t) {
		if(this.prefix != null) {
			System.err.print("[SEVERE] " + this.prefix + str + ": ");
		} else {
			System.err.print("[SEVERE] " + str + ": ");
		}
		t.printStackTrace(System.err);
	}

}
