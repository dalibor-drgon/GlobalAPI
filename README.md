 GlobalApi
===========

Java library for easy reading & writing primite types, fast data serialization, timeout threads, mysql & sqlite interface.

Usable as Bukkit plugin, with small changes compatible with plain Java. Not well documented, but try it on your own.

Under MIT license.



## Examples



### Serialize Set & read back

Serialize Set with another Set, Map and random primitives to 131 bytes! And parse it back.

```java
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import eu.wordnice.api.OStream;
import eu.wordnice.api.IStream;
import eu.wordnice.api.Map;
import eu.wordnice.api.Set;

public class SerializeExample {
	
	public static void main(String... args) throws Exception {
		// Write
		ByteArrayOutputStream ot = new ByteArrayOutputStream();
		OStream os = new OStream(ot);
		os.writeSet(new Set<Object>("Hello!", 123456, true, true,
				new Map<String, Integer>(new String[]{"Zero","One","Two","Three","Four"}, new Integer[] {0,1,2,3,4}),
				false, "YES", new Set<Object>(404,"NotFound")));
		os.close();

		// Read
		IStream is = new IStream(new ByteArrayInputStream(ot.toByteArray()));
		Set<Object> set = is.readSet();
		System.out.println("Set: " + set);
		is.close();
	}
}
```

Output:
```
Set: [Hello!,123456,true,true,{Zero:0,One:1,Two:2,Three:3,Four:4},false,YES,[404,NotFound]]
```



### Timeouted threads (runnables)

Useful for I/O and multithreading.

```java
package eu.wordnice.test;

import eu.wordnice.api.threads.Runa;
import eu.wordnice.api.threads.TimeoutThread;

public class TimeoutedHelloWorld {
	
	public static void main(String... blah) throws Exception {
		TimeoutThread.run(new Runa<Object>() {
			@Override
			public Object call() throws Exception{
				while(true) {
					System.out.println("Hello, world!");
					Thread.sleep(200);
				}
			}
		}, 500L);
	}
	
}
```

Will output:

```
Hello, world!
Hello, world!
Hello, world!
Exception in thread "main" java.util.concurrent.TimeoutException
	at java.util.concurrent.FutureTask.get(FutureTask.java:201)
	at eu.wordnice.api.threads.TimeoutThread.run(TimeoutThread.java:94)
	at eu.wordnice.test.TimeoutedHelloWorld.main(TimeoutedHelloWorld.java:9)
```




### Simple HTTP server

Simple HTTP server at localhost:8192. Write headers and "Hello!" with date.

```java
import java.util.Date;

import eu.wordnice.api.Handler;
import eu.wordnice.sockets.HIO;
import eu.wordnice.sockets.HIOServer;

public class HIOExample {
	
	public static void main(String... args) throws Exception {
		HIOServer server = new HIOServer("localhost", 8192);
		System.out.println("Server running at " + server.server.getInetAddress().toString() 
				+ ", port " + server.port);

		/*
		 public void onAccept(Handler.TwoVoidHandler<Thread, HIO> request_handler, 
			boolean create_new_thread, boolean read_post, long timeout_per_read, 
			Handler.ThreeVoidHandler<Thread, Throwable, HIO> error_handler);
		 */

		server.onAccept(new Handler.TwoVoidHandler<Thread, HIO>() {
			@Override
			public void handle(Thread thrd, HIO hio) {
				try {
					System.out.println("Accepted: " + hio.METHOD + " " + hio.REQTYPE);
					System.out.println(" - Path: " + hio.PATH);
					System.out.println(" - Args: " + hio.GET);
					System.out.println(" - Head: " + hio.HEAD);
					
					// HTTP Head
					hio.out.write(new String(hio.REQTYPE + " 200 OK\r\n" +
							"Content-Type: text/plain; charset=utf-8\r\n\r\n").getBytes());
					
					// Content
					hio.out.write(new String("Hello!\n\n" + new Date().toString()).getBytes());
					
					// Close
					hio.close();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}, false, false, 3000, null);
	}
}
```

Sample browser output:
```
Hello!

Mon Jul 20 13:33:04 CEST 2015

```

Sample console output:
```
Accepted: GET HTTP/1.1
 - Path: /
 - Args: null
 - Head: {host:localhost:8192,connection:keep-alive,cache-control:max-age=0,accept:text/html,application/xhtml xml,application/xml;q=0.9,image/webp,*/*;q=0.8,user-agent:Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36,accept-encoding:gzip, deflate, sdch,accept-language:sk-SK,sk;q=0.8,cs;q=0.6,en-US;q=0.4,en;q=0.2}
Accepted: GET HTTP/1.1
 - Path: /favicon.ico
 - Args: null
 - Head: null
```



### Simple HTTPS server

Simple HTTPS server. It's too same with few args more when creating `HIOServer` instance.

First, if already not got, you must generate your own keystore file, i.e. self-signed certificate - your browser will warn you about untrusted certificate on page enter. Just skip that warning and continue, connection will be still encrypted. If you want "trusted" certificate, you must register it (for non-commercial use it might be free).

```
$ keytool -genkey -keysize 2048 -keyalg RSA -keypass $KEYPASS -storepass $STOREPASS -keystore $OUTPUT

What is your first and last name?
  [Unknown]:  mail.ru
What is the name of your organizational unit?
  [Unknown]:  IT
What is the name of your organization?
  [Unknown]:  LLC Mail.ru
What is the name of your City or Locality?
  [Unknown]:  Moscow
What is the name of your State or Province?
  [Unknown]:  RUSSIAN FEDERATION
What is the two-letter country code for this unit?
  [Unknown]:  RU
Is CN=mail.ru, OU=IT, O=LLC Mail.ru, L=Moscow, ST=RUSSIAN FEDERATION, C=RU correct?
  [no]:  yes

```

or

```
$ keytool -genkey -keysize 2048 -keyalg RSA -keypass $KEYPASS -storepass $STOREPASS -keystore $OUTPUT

What is your first and last name?
  [Unknown]:  www.example.com
What is the name of your organizational unit?
  [Unknown]:  Software Development
What is the name of your organization?
  [Unknown]:  Example, Inc.
What is the name of your City or Locality?
  [Unknown]:  Talkeetna
What is the name of your State or Province?
  [Unknown]:  AK
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=www.example.com, OU=Software Development, O=Example, Inc., L=Talkeetna, ST=AK, C=US correct?
  [no]:  yes

```

Or, if your country is not divided into states, you can just copy "Location" to "State or Province" field:

```
$ keytool -genkey -keysize 2048 -keyalg RSA -keypass $KEYPASS -storepass $STOREPASS -keystore $OUTPUT

What is your first and last name?
  [Unknown]:  git.priklad.eu
What is the name of your organizational unit?
  [Unknown]:  Services
What is the name of your organization?
  [Unknown]:  Priklad s.r.o.
What is the name of your City or Locality?
  [Unknown]:  Spacince 1
What is the name of your State or Province?
  [Unknown]:  Spacince 1
What is the two-letter country code for this unit?
  [Unknown]:  SK
Is CN=git.priklad.eu, OU=Services, O=Priklad s.r.o., L=Spacince 1, ST=Spacince 1, C=SK correct?
  [no]:  y

```

Let's say we generated password with KP="s1mul4t0r" and SP="goodnight" with output file ".keystore", we will costruct `HIOServer` with folowing arguments:

```
		HIOServer server = new HIOServer("localhost", 8192, "s1mul4t0r", "goodnight", ".keystore");
```

To get working example, just copy code from `Simple HTTP server` and replace constructor. Connect to `https://localhost:8192` (note the `https://`).






### WNDB

`WNDB` class is for easy tables serialization & deserialization, low-level inserting, querying and updating. For even easily work, there was created `WNDBStore` abstract class, and with its help there can be created very short and clean databases store class. Example, class with 2 databases is below. Run it twice, if everything is OK and no errors are displayed, you should get Outputs as under code.


```java
package eu.wordnice.test;

import java.io.File;

import eu.wordnice.api.Api;
import eu.wordnice.api.Set;
import eu.wordnice.sql.wndb.WNDB;
import eu.wordnice.sql.wndb.WNDBStore;
import eu.wordnice.sql.wndb.WNDBVarTypes;

public class WNDBStoreExample extends WNDBStore {
	
	/*** DATABASES & DATAS ***/
	
	public WNDB users;
 	Set<WNDBVarTypes> users_types = new Set<WNDBVarTypes>(
 			WNDBVarTypes.STRING, WNDBVarTypes.INT
 	);
 	Set<String> users_names = new Set<String>(
 			"name", "pass"
 	);
 	
 	
 	public WNDB posts;
 	Set<WNDBVarTypes> posts_types = new Set<WNDBVarTypes>(
 			WNDBVarTypes.STRING, WNDBVarTypes.STRING, WNDBVarTypes.STRING
 	);
 	Set<String> posts_names = new Set<String>(
 			"name", "title", "text"
 	);
	
	
	/*** OVERRIDE ***/
 	
 	public WNDBStoreExample(File dir) {
 		super(dir);
 		this.timeout = 5000;
 		if(dir.exists() == false) {
 			try {
 				dir.mkdirs();
 			} catch(Throwable t) {
 				this.err("Error while creating database folder, details:");
 				this.exc(t);
 				this.err("Cancelling");
 				
 				//this.loadEmptyDBs();
 				return;
 			}
 			this.loadDBs();
 		} else {
 			this.loadDBs();
 		}
 	}
	
	@Override
	public void err(String msg) {
		System.out.println(msg);
	}

	@Override
	public void out(String msg) {
		System.out.println(msg);
	}

	@Override
	public void exc(Throwable t) {
		t.printStackTrace();
	}
	
	
	
	/*** MAIN ***/
	
	public static void main(String... args) throws Exception {
		WNDBStoreExample ws = new WNDBStoreExample(new File("./dbs/"));
		
		if(ws.posts.size() == 0 || ws.users.size() == 0) {
			System.out.println("Databases do not exist, inserting some data...");
			
			String curusr = Api.genString(10);
			int curusr_pass = Api.genString(12).hashCode();
			System.out.println("Inserting user1: " + curusr + " with password hash " + curusr_pass);
			if(!ws.users.insert(curusr, curusr_pass)) { System.out.println("Cannot insert new user..."); }
			
			curusr = Api.genString(10);
			curusr_pass = Api.genString(12).hashCode();
			System.out.println("Inserting user2: " + curusr + " with password hash " + curusr_pass);
			if(!ws.users.insert(curusr, curusr_pass)) { System.out.println("Cannot insert new user..."); }
			
			String title = "How to decode " + Api.genString(12);
			String text = "Blah blah blah, I really cannot say you how to do it, because it just random string.";
			System.out.println("Inserting post1: from " + curusr + " with title " + title);
			if(!ws.posts.insert(curusr, title, text)) { System.out.println("Cannot insert new post..."); }
			
			ws.saveDBs();
			System.out.println("Databases should be saved. Run this program again to load and print data...");
		} else {
			System.out.println("Displaying saved users:");
			int n = ws.users.size();
			int i = 0;
			Set<Object> vals = null;
			for(i = 0; i < n; i++) {
				vals = ws.users.getEntry(i);
				System.out.println("\t- " + ((String) vals.get(0)) + " with password hash " + ((Integer) vals.get(1)));
			}
			System.out.println("");
			
			
			System.out.println("Displaying saved posts:");
			n = ws.posts.size();
			for(i = 0; i < n; i++) {
				vals = ws.posts.getEntry(i);
				System.out.println("\t- " + ((String) vals.get(1)) + " from user " + ((String) vals.get(0)));
				System.out.println("\t\t" + ((String) vals.get(2)));
			}
			System.out.println("");
		}
	}
}
```
