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

package eu.wordnice.db.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

import eu.wordnice.api.Api;

public class JDBCSQL extends ConnectionSQL {

	protected boolean single;
	public String db_url;
	public String user;
	public String pass;
	public String db_name;

	public JDBCSQL() {}

	public JDBCSQL(String db_url) {
		this.single = true;
		this.db_url = db_url;
	}

	public JDBCSQL(String db_url, String user, String pass) {
		this.single = false;
		this.db_url = db_url;
		this.user = user;
		this.pass = pass;
	}

	public JDBCSQL(String db_url, String user, String pass, boolean single) {
		this.single = single;
		this.db_url = db_url;
		this.user = user;
		this.pass = pass;
	}

	public JDBCSQL(String driver, String db_url) {
		Api.loadClassAndDebug(driver);
		this.single = true;
		this.db_url = db_url;
	}

	public JDBCSQL(String driver, String db_url, String user, String pass) {
		Api.loadClassAndDebug(driver);
		this.single = false;
		this.db_url = db_url;
		this.user = user;
		this.pass = pass;
	}

	public JDBCSQL(String driver, String db_url, String user, String pass,
			boolean single) {
		Api.loadClassAndDebug(driver);
		this.single = single;
		this.db_url = db_url;
		this.user = user;
		this.pass = pass;
	}

	@Override
	public void connect() throws SQLException {
		try {
			this.close();
		} catch(SQLException exc) {}
		if(this.single == true) {
			this.con = DriverManager.getConnection(this.getDBUrl());
		} else {
			this.con = DriverManager.getConnection(this.getDBUrl(),
					this.getUser(), this.getPass());
		}
		this.stm = this.con.createStatement();
	}

	protected String getDBUrl() {
		return this.db_url;
	}

	protected String getUser() {
		return this.user;
	}

	protected String getPass() {
		return this.pass;
	}

}
