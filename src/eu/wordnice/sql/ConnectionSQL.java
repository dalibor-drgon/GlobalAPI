/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.sql;

import java.sql.Connection;
import java.sql.Statement;

public abstract class ConnectionSQL implements SQL {

	public Connection con;
	public Statement stm;

	protected ConnectionSQL() {
	}

	protected ConnectionSQL(Connection con) {
		this.con = con;
		this.stm = null;
		try {
			this.stm = con.createStatement();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected ConnectionSQL(Connection con, Statement stm) {
		this.con = con;
		this.stm = stm;
	}

	@Override
	public ResSet getQuery(String query) {
		this.checkConnection();
		try {
			return new ResultResSet(this.stm.executeQuery(query));
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public boolean getCommand(String cmd) {
		this.checkConnection();
		try {
			this.stm.executeUpdate(cmd);
			return true;
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	public boolean close() {
		try {
			this.stm.close();
			this.con.close();
			return true;
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	public boolean isClosed() {
		if (this.stm == null || this.con == null) {
			return true;
		}
		boolean stmc = true;
		boolean conc = true;
		try {
			stmc = this.stm.isClosed();
		} catch (Throwable t) {
		}
		if (stmc == false) {
			try {
				conc = this.con.isClosed();
			} catch (Throwable t) {
			}
		}
		return (stmc == true || conc == true);
	}

	public void checkConnection() {
		/*
		 * boolean stmc = true; boolean conc = true; try { stmc =
		 * this.stm.isClosed(); } catch(Throwable t) {} try { conc =
		 * this.con.isClosed(); } catch(Throwable t) {}
		 */

		if (this.isClosed()) {
			boolean con = this.connect();
			if (con == false) {
				new Exception("Cant reconnect! CON == FALSE!")
						.printStackTrace();
			}
		}
	}

	@Override
	public abstract boolean connect();

}
