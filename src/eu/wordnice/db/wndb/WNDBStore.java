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

package eu.wordnice.db.wndb;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.Api;
import eu.wordnice.api.InstanceMan;
import eu.wordnice.db.ColType;

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
	public abstract void exc(Throwable t);
	
	
	public Map<String, WNDB> getDBs() {
		return InstanceMan.getValues(this, this.getClass(), WNDB.class);
	}
	
	public void loadDBs() {
		Iterator<Entry<String, WNDB>> dbs = this.getDBs().entrySet().iterator();
		while(dbs.hasNext()) {
			Entry<String, WNDB> ent = dbs.next();
			WNDB db = ent.getValue();
			String name = ent.getKey();
			if(db == null) {
				this.out("Loading database " + name + "!");
			} else {
				this.out("Loading (overwriting runtime data) database " + name + "!");
			}
			try {
				if(this.loadDB(name)) {
					this.out("Database " + name +" loaded!");
				} else {
					this.err("BUG: Types or names for " + name + " database are null! "
							+ "Probably they do not exist, we are sorry for that bug.");
				}
			} catch(Throwable t) {
				this.err("We are really sorry, but we cannot load database " + name +"... Details:");
				this.exc(t);
			}
		}
	}
	
	public void loadEmptyDBs() {
		Iterator<Entry<String, WNDB>> dbs = this.getDBs().entrySet().iterator();
		while(dbs.hasNext()) {
			Entry<String, WNDB> ent = dbs.next();
			//WNDB db = ent.getValue();
			String name = ent.getKey();
			this.out("Creating simulated config " + name + "!");
			try {
				if(loadEmptyDB(name)) {
					this.out("Simulated config for " + name +" created!");
				} else {
					this.err("BUG: Types or names for " + name + " database are null! "
							+ "Probably they do not exist, we are sorry for that bug.");
				}
			} catch(Throwable t) {
				this.err("We are really sorry, but we even cannot simulate database " + name +"... Details:");
				this.exc(t);
			}
		}
	}
	
	public boolean loadEmptyDB(String name) {
		ColType[] set_types = (ColType[]) InstanceMan.getValue(
				this, this.getClass(), name + "_types");
		String[] set_names = (String[]) InstanceMan.getValue(this, this.getClass(), name + "_names");
		
		if(set_types == null || set_names == null) {
			return false;
		}
		
		WNDB db = WNDB.createEmptyWNDB(set_names, set_types);
		
		if(!InstanceMan.setValue(this, this.getClass(), name, db)) {
			if(set_types == null || set_names == null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean loadDB(String name) {
		ColType[] set_types = (ColType[]) InstanceMan.getValue(
				this, this.getClass(), name + "_types");
		String[] set_names = (String[]) InstanceMan.getValue(this, this.getClass(), name + "_names");
		
		if(set_types == null || set_names == null) {
			return false;
		}
		
		WNDB db = null;
		File file = new File(this.dir, name + ".wndb");
		if(file.exists()) {
			try {
				db = new WNDB(file);
			} catch(Throwable t) {
				db = null;
				File movedto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
				this.err("Cannot read database " + name + "; database renamed to '" 
						+ Api.getRealPath(movedto) + "' and we are going to create new one! "
						+ "Database parse error:");
				this.exc(t);
				
				try {
					file.renameTo(movedto);
				} catch(Throwable t2) {
					this.err("We are really sorry... Cannot rename the database... Details: ");
					this.exc(t2);
					
					this.err("Due this unexpected error, all settings will be saved to RAM "
							+ "and after restart will be lost! Please, fix this problem.");
					db = WNDB.createEmptyWNDB(set_names, set_types);
				}
			}
		}
		if(db == null) {
			try {
				db = WNDB.createWNDB(file, set_names, set_types);
			} catch (Throwable t) {
				this.err("We cannot even create the database " + name + "... Details: ");
				this.exc(t);
				
				this.err("Due this unexpected error, all settings for database " + name +" will be "
						+ "saved to RAM and after restart will be lost! Please, fix this problem.");
				db = WNDB.createEmptyWNDB(set_names, set_types);
			}
		}
		if(!InstanceMan.setValue(this, this.getClass(), name, db)) {
			if(set_types == null || set_names == null) {
				return false;
			}
		}
		return true;
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
						+ "We cannot save the database " + name +"...  Details: ");
				this.exc(t);
				return true;
			}
		} else {
			File file = new File(this.dir, name + ".wndb");
			this.out("Saving (unsaved yet) database " + name + "...");
			if(file.exists()) {
				try {
					File moveto = Api.getFreeName(Api.getRealPath(file) + ".invalid");
					this.out("Save unsaved db: old " + name +" database renamed to '" + Api.getRealPath(moveto) 
							+ "' and we are going to create new one!");
					file.renameTo(moveto);
				} catch(Throwable t) {
					this.err("This is embarrassing... "
							+ "We cannot rename the database " + name +"... Details: ");
					this.exc(t);
					return true;
				}
			}
			try {
				db.file = file;
				db.save();
			} catch(Throwable t) {
				db.file = null;
				this.err("This is embarrassing... "
						+ "We cannot save the database " + name +"...  Details: ");
				this.exc(t);
				return true;
			}
			InstanceMan.setValue(this, name, db);
			this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
		}
		return true;
	}
	
	
	public void saveUnsavedDBs() {
		Iterator<Entry<String, WNDB>> dbs = this.getDBs().entrySet().iterator();
		while(dbs.hasNext()) {
			Entry<String, WNDB> ent = dbs.next();
			WNDB db = ent.getValue();
			String name = ent.getKey();
			if(db != null) {
				if(db.file == null) {
					File file = new File(this.dir, name + ".wndb");
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
							this.exc(t);
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
						this.exc(t);
						continue;
					}
					InstanceMan.setValue(this, name, db);
					this.out("Database " + name + " successfuly saved to " + Api.getRealPath(file) + "!");
				}
			}
		}
	}
	
	public void saveDBs() {
		Iterator<Entry<String, WNDB>> dbs = this.getDBs().entrySet().iterator();
		while(dbs.hasNext()) {
			Entry<String, WNDB> ent = dbs.next();
			WNDB db = ent.getValue();
			String name = ent.getKey();
			if(db != null) {
				if(db.file == null) {
					File file = new File(this.dir, name + ".wndb");
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
							this.exc(t);
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
						this.exc(t);
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
						this.exc(t);
						continue;
					}
					InstanceMan.setValue(this, name, db);
					this.out("Database " + name + " successfuly saved to " + Api.getRealPath(db.file) + "!");
				}
			}
		}
	}
}
