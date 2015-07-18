package eu.wordnice.sql.wndb;

import java.io.File;

import eu.wordnice.api.Api;
import eu.wordnice.api.InstanceMan;
import eu.wordnice.api.Map;
import eu.wordnice.api.Set;

public abstract class WNDBStore {
	
	/*** INSTANCE FIELDS ***/
	public File dir = null;
	
	
	/*** CONSTRUCTOR ***/
	public WNDBStore() {
		//For hackers
	}
	
	public WNDBStore(File dir) {
		this.dir = dir;
	}
	
	
	/*** UTITILITIES ***/
	public abstract void err(String str);
	public abstract void out(String str);
	
	
	public Map<String, WNDB> getDBs() {
		return InstanceMan.getValues(this, WNDB.class);
	}
	
	public void loadDBs() {
		Map<String, WNDB> dbs = this.getDBs();
		int i = 0;
		String name = null;
		WNDB db = null;
		for(; i < dbs.size(); i++) {
			name = dbs.getNameI(i);
			db = dbs.getI(i);
			if(db == null) {
				this.out("Loading database " + name + "!");
			} else {
				this.out("Loading (overwriting) database " + name + "!");
			}
			try {
				this.loadDB(name);
			} catch(Throwable t) {
				this.err("We are really sorry, but we cannot load database... Details:");
				t.printStackTrace();
			}
		}
	}
	
	public void loadEmptyDBs() {
		Map<String, WNDB> dbs = this.getDBs();
		int i = 0;
		String name = null;
		this.out("Due to some fatal errors occured, we are simulating configs...");
		for(; i < dbs.size(); i++) {
			name = dbs.getNameI(i);
			this.out("Creating simulated config " + name + "!");
			try {
				loadEmptyDB(name);
			} catch(Throwable t) {
				this.err("This is really embarrassing... "
						+ "We cannot simulate this database...  Details: ");
				t.printStackTrace();
				continue;
			}
			this.out("Simulated config created!");
		}
	}
	
	public void loadEmptyDB(String name) throws Exception {
		@SuppressWarnings("unchecked")
		Set<WNDBVarTypes> set_types = (Set<WNDBVarTypes>) InstanceMan.getValue(
				this, this.getClass(), name + "_types");
		@SuppressWarnings("unchecked")
		Set<String> set_names = (Set<String>) InstanceMan.getValue(this, this.getClass(), name + "_names");
		
		if(set_types == null || set_names == null) {
			throw new NullPointerException("Types or names for " + name + " database are null!"
					+ " Probably they do not exist, we are sorry for that bug.");
		}
		
		WNDB db = new WNDB();
		db.values = new Set<Set<Object>>();
		db.names = set_names;
		db.types = set_types;
		
		if(!InstanceMan.setValue(this, this.getClass(), name, db)) {
			if(set_types == null || set_names == null) {
				throw new NullPointerException("We cannot set database value, it probably does not exists...");
			}
		}
	}
	
	public void loadDB(String name) throws Exception {
		@SuppressWarnings("unchecked")
		Set<WNDBVarTypes> set_types = (Set<WNDBVarTypes>) InstanceMan.getValue(
				this, this.getClass(), name + "_types");
		@SuppressWarnings("unchecked")
		Set<String> set_names = (Set<String>) InstanceMan.getValue(this, this.getClass(), name + "_names");
		
		if(set_types == null || set_names == null) {
			throw new NullPointerException("Types or names for " + name + " database are null! "
					+ "Probably they do not exist, we are sorry for that bug.");
		}
		
		WNDB db = null;
		File file = new File(this.dir, name + ".wndb");
		if(file.exists()) {
			try {
				db = new WNDB(file);
				db.checkSet();
			} catch(Throwable t) {
				db = null;
				File movedto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
				this.err("Cannot read database " + name + "; database renamed to '" 
						+ Api.getRealPath(movedto) + "' and we are going to create new one! "
						+ "Database parse error:");
				t.printStackTrace();
				
				try {
					file.renameTo(movedto);
				} catch(Throwable t2) {
					this.err("We are really sorry... Cannot rename the database... Details: ");
					t2.printStackTrace();
					
					this.err("Due this unexpected error, all settings will be saved to RAM "
							+ "and after restart will be lost! Please, fix this problem.");
					db = new WNDB();
					db.values = new Set<Set<Object>>();
					db.names = set_names;
					db.types = set_types;
				}
			}
		}
		if(db == null) {
			try {
				db = WNDB.createWBDB(file, set_names, set_types);
				db.checkSet();
			} catch (Throwable t) {
				this.err("We cannot create the database " + name + "... Details: ");
				t.printStackTrace();
				
				this.err("Due this unexpected error, all settings will be saved to RAM "
						+ "and after restart will be lost! Please, fix this problem.");
				db = new WNDB();
				db.values = new Set<Set<Object>>();
				db.names = set_names;
				db.types = set_types;
			}
		}
		if(!InstanceMan.setValue(this, this.getClass(), name, db)) {
			if(set_types == null || set_names == null) {
				throw new NullPointerException("We cannot set database value, it probably does not exists...");
			}
		}
	}
	
	public boolean saveDB(String name) {
		WNDB db = (WNDB) InstanceMan.getValue(this, this.getClass(), name);
		if(db == null) {
			return false;
		}
		if(db.file != null) {
			this.out("Saving database " + name + "...");
			try {
				db.save();
			} catch(Throwable t) {
				this.err("This is embarrassing... "
						+ "We cannot save the database...  Details: ");
				t.printStackTrace();
				return true;
			}
		} else {
			File file = new File(this.dir, name + ".wndb");
			this.out("Saving (unsaved yet) database " + name + "...");
			if(file.exists()) {
				try {
					File moveto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
					this.out("Save unsaved db: old database renamed to '" + Api.getRealPath(moveto) 
							+ "' and we are going to create new one!");
					file.renameTo(moveto);
				} catch(Throwable t) {
					this.err("This is embarrassing... "
							+ "We cannot rename the database again... Details: ");
					t.printStackTrace();
					return true;
				}
			}
			try {
				db.file = file;
				db.save();
			} catch(Throwable t) {
				db.file = null;
				this.err("This is embarrassing... "
						+ "We cannot save the database...  Details: ");
				t.printStackTrace();
				return true;
			}
			InstanceMan.setValue(this, name, db);
			this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
		}
		return true;
	}
	
	
	public void saveUnsavedDBs() {
		Map<String, WNDB> dbs = this.getDBs();
		File file = null;
		int i = 0;
		String name = null;
		WNDB db = null;
		for(; i < dbs.size(); i++) {
			name = dbs.getNameI(i);
			db = dbs.getI(i);
			if(db != null) {
				if(db.file == null) {
					file = new File(this.dir, name + ".wndb");
					this.out("Saving (unsaved yet) database " + name + "...");
					if(file.exists()) {
						try {
							File moveto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
							this.out("Save unsaved dbs: old database renamed to '" + Api.getRealPath(moveto) 
									+ "' and we are going to create new one!");
							file.renameTo(moveto);
						} catch(Throwable t) {
							this.err("This is embarrassing... "
									+ "We cannot rename the database again... Continue to next db... Details: ");
							t.printStackTrace();
							continue;
						}
					}
					try {
						db.file = file;
						db.save();
					} catch(Throwable t) {
						db.file = null;
						this.err("This is embarrassing... "
								+ "We cannot save the database... Continue to next db... Details: ");
						t.printStackTrace();
						continue;
					}
					InstanceMan.setValue(this, name, db);
					this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
				}
			}
		}
	}
	
	public void saveDBs() {
		Map<String, WNDB> dbs = this.getDBs();
		File file = null;
		int i = 0;
		String name = null;
		WNDB db = null;
		for(; i < dbs.size(); i++) {
			name = dbs.getNameI(i);
			db = dbs.getI(i);
			if(db != null) {
				if(db.file == null) {
					file = new File(this.dir, name + ".wndb");
					this.out("Saving (unsaved yet) database " + name + "...");
					if(file.exists()) {
						try {
							File moveto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
							this.out("Save unsaved dbs: old database renamed to '" + Api.getRealPath(moveto) 
									+ "' and we are going to create new one!");
							file.renameTo(moveto);
						} catch(Throwable t) {
							this.err("This is embarrassing... "
									+ "We cannot rename the database again... Continue to next db... Details: ");
							t.printStackTrace();
							continue;
						}
					}
					try {
						db.file = file;
						db.save();
					} catch(Throwable t) {
						db.file = null;
						this.err("This is embarrassing... "
								+ "We cannot save the database... Continue to next db... Details: ");
						t.printStackTrace();
						continue;
					}
					InstanceMan.setValue(this, name, db);
					this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
				} else {
					this.out("Saving database " + name + "...");
					try {
						db.save();
					} catch(Throwable t) {
						this.err("This is embarrassing... "
								+ "We cannot save the database... Continue to next db... Details: ");
						t.printStackTrace();
						continue;
					}
					InstanceMan.setValue(this, name, db);
					this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
				}
			}
		}
	}
}
