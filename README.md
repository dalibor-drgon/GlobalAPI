 GlobalApi
===========

Java library for easy & fast data serialization, timeout threads, mysql & sqlite interface, JSON encoding, Unsafe and Reflections tools, classes and packages listing.

Usable as Bukkit and Sponge plugin, with small changes compatible with plain Java. Under MIT license.



## Examples



### Serialize Set & read back

Serialize List with Set, Map and random primitives and parse it back.

```java
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.wordnice.api.OStream;
import eu.wordnice.api.IStream;

public class SerializeExample {
	
	public static void main(String... args) throws Exception {
		// Write
		ByteArrayOutputStream ot = new ByteArrayOutputStream();
		OStream os = new OStream(ot);
		os.writeSet(createList());
		os.close();

		// Read
		IStream is = new IStream(new ByteArrayInputStream(ot.toByteArray()));
		List<Object> read = new ArrayList<Object>();
		is.readSet(read);
		System.out.println("Readed: " + read);
		is.close();
	}
	
	public static List<Object> createList() {
		Map<Integer,String> exam_map = new HashMap<Integer, String>();
		exam_map.put(1, "one");
		exam_map.put(2, "two");
		exam_map.put(3, "three");
		
		Set<String> exam_set = new HashSet<String>();
		exam_set.add("Hello");
		exam_set.add("World");
		
		List<Object> write_set = new ArrayList<Object>();
		write_set.add("Welcome");
		write_set.add(false);
		write_set.add(exam_set);
		write_set.add(-239.23);
		write_set.add(exam_map);
		write_set.add(null);
		
		return write_set;
	}
}
```

Output:
```
Readed: [Welcome, false, [World, Hello], -239.23, {1=one, 2=two, 3=three}, null]
```



### Timeouted threads (runnables)

Useful for I/O and multithreading.

```java
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
	at eu.wordnice.examples.TimeoutedHelloWorld.main(TimeoutedHelloWorld.java:9)
```



### Maps

Create immutable `Map` from one array, two arrays, or two `Iterable`s (e.g. `List`, `Set`, any `Collection`).

In following example is also created `ImmArray` from array.

```java
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.cols.ImmArray;
import eu.wordnice.api.cols.ImmMapArray;
import eu.wordnice.api.cols.ImmMapIterPair;
import eu.wordnice.api.cols.ImmMapPair;
import gnu.trove.map.hash.THashMap;

public class MapsPreview {
	
	public static void main(String[] blah) {
		/**************************
		 * Trove HashMap (mutable)
		 */
		Map<String, Integer> map = new THashMap<String, Integer>();
			map.put("one", 1);
			map.put("two", 2);
			map.put("three", 3);
		MapsPreview.debugMap(map);
		
		
		/*************************
		 * Java HashMap (mutable)
		 */
		map = new HashMap<String, Integer>();
			map.put("one", 1);
			map.put("two", 2);
			map.put("three", 3);
		MapsPreview.debugMap(map);
		
		
		/************
		 * One array
		 */
		MapsPreview.debugMap(
				new ImmMapArray<String, Integer>(new Object[] {
						"one", 1,
						"two", 2,
						"three", 3
				}, 6)
		);
		
		
		/*******
		 * Pair
		 */
		MapsPreview.debugMap(
				new ImmMapPair<String, Integer>(new Object[] {
						"one", "two", "three"
				}, new Object[] {
						1, 2, 3
				}, 3)
		);
		
		
		/******************
		 * Collection pair
		 */
		MapsPreview.debugMap(
				new ImmMapIterPair<String, Integer>(new ImmArray<String>(new Object[] {
						"one", "two", "three"
				}), new ImmArray<Integer>(new Object[] {
						1, 2, 3
				}), 3)
		);
	}
	
	
	public static void debugMap(Map<String, Integer> map) {
		System.out.println("Map: [" + map.size() + "] " + map + " / " + map.getClass());
		System.out.println("\tKeys   : [" + map.keySet().size() + "] " + map.keySet() + " / " + map.keySet().getClass());
		System.out.println("\tValues : [" + map.values().size() + "] " + map.values() + " / " + map.values().getClass());
		System.out.println("\tEntries: [" + map.entrySet().size() + "] " + map.entrySet() + " / " + map.entrySet().getClass());
		
		Iterator<Entry<String, Integer>> it = map.entrySet().iterator();
		Iterator<String> keys = map.keySet().iterator();
		Iterator<Integer> vals = map.values().iterator();
		while(it.hasNext()) {
			String pref = "";
			if(keys.hasNext()) {
				pref += keys.next() + "=";
			} else {
				pref += "?=";
			}
			if(vals.hasNext()) {
				pref += vals.next();
			} else {
				pref += "?";
			}
			System.out.println("- " + pref + ", " + it.next());
		}
		System.out.print("\n\n");
	}
	
}
```

Output:
```
Map: [3] {three=3, one=1, two=2} / class gnu.trove.map.hash.THashMap
	Keys   : [3] {three, one, two} / class gnu.trove.map.hash.THashMap$KeyView
	Values : [3] {3, 1, 2} / class gnu.trove.map.hash.THashMap$ValueView
	Entries: [3] {three=3, one=1, two=2} / class gnu.trove.map.hash.THashMap$EntryView
- three=3, three=3
- one=1, one=1
- two=2, two=2


Map: [3] {two=2, one=1, three=3} / class java.util.HashMap
	Keys   : [3] [two, one, three] / class java.util.HashMap$KeySet
	Values : [3] [2, 1, 3] / class java.util.HashMap$Values
	Entries: [3] [two=2, one=1, three=3] / class java.util.HashMap$EntrySet
- two=2, two=2
- one=1, one=1
- three=3, three=3


Map: [3] {one=1, two=2, three=3} / class eu.wordnice.api.cols.ImmMapArray
	Keys   : [3] [one, two, three] / class eu.wordnice.api.cols.ImmSkipArray
	Values : [3] [1, 2, 3] / class eu.wordnice.api.cols.ImmSkipArray
	Entries: [3] [one=1, two=2, three=3] / class eu.wordnice.api.cols.ImmEntryArray
- one=1, one=1
- two=2, two=2
- three=3, three=3


Map: [3] {one=1, two=2, three=3} / class eu.wordnice.api.cols.ImmMapPair
	Keys   : [3] [one, two, three] / class eu.wordnice.api.cols.ImmArray
	Values : [3] [1, 2, 3] / class eu.wordnice.api.cols.ImmArray
	Entries: [3] [one=1, two=2, three=3] / class eu.wordnice.api.cols.ImmMapPair$EntrySet
- one=1, one=1
- two=2, two=2
- three=3, three=3


Map: [3] {one=1, two=2, three=3} / class eu.wordnice.api.cols.ImmMapIterPair
	Keys   : [3] [one, two, three] / class eu.wordnice.api.cols.ImmIter
	Values : [3] [1, 2, 3] / class eu.wordnice.api.cols.ImmIter
	Entries: [3] [one=1, two=2, three=3] / class eu.wordnice.api.cols.ImmMapIterPair$EntrySet
- one=1, one=1
- two=2, two=2
- three=3, three=3
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

Let's say we generated self-signed certificate stored in file ".keystore" with KP="s1mul4t0r" and SP="goodnight", we will costruct `HIOServer` with folowing arguments:

```
		HIOServer server = new HIOServer("localhost", 8192, "s1mul4t0r", "goodnight", ".keystore");
```

To get working example, just copy code from `Simple HTTP server` and replace constructor. Connect to `https://localhost:8192` (note the `https://`).




### WNDB

`WNDB` class is for easy tables serialization & deserialization, low-level inserting, querying and updating. For even easily work, there was created `WNDBStore` abstract class, and with its help there can be created very short and clean databases store class. Example, class with 2 databases is below. Run it twice, if everything will be OK and no errors will be displayed, after second run you should get printed database data.

Example below is for demonstrating low-level access (for advanced access you can use `Database` class, example below this one).


```java
import java.io.File;

import eu.wordnice.api.Api;
import eu.wordnice.db.wndb.WNDB;
import eu.wordnice.db.wndb.WNDBStore;
import eu.wordnice.db.DBType;

public class WNDBStoreExample extends WNDBStore {
	
	/*** DATABASES & DATAS ***/
	
	public WNDB users;
 	DBType[] users_types = new DBType[] {
 			DBType.STRING, DBType.INT
 	};
 	String[] users_names = new String[] {
 			"name", "pass"
 	};
 	
 	
 	public WNDB posts;
 	DBType[] posts_types = new DBType[] {
 			DBType.STRING, DBType.STRING, DBType.STRING
 	};
 	String[] posts_names = new String[] {
 			"user", "title", "text"
 	};
	
	
	/*** OVERRIDE ***/
 	
 	public WNDBStoreExample(File dir) {
 		super(dir);
 		if(dir.exists() == false) {
 			try {
 				dir.mkdirs();
 			} catch(Throwable t) {
 				this.err("Error while creating database folder, details:");
 				this.exc(t);
 				this.err("Cancelling");
 				this.loadEmptyDBs();
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
			
			String curusr = new String(Api.genBytes(10));
			int curusr_pass = new String(Api.genBytes(10)).hashCode();
			System.out.println("Inserting user1: " + curusr + " with password hash " + curusr_pass);
			ws.users.insertRaw(new Object[] {curusr, curusr_pass});
			
			curusr = new String(Api.genBytes(10));
			curusr_pass = new String(Api.genBytes(10)).hashCode();
			System.out.println("Inserting user2: " + curusr + " with password hash " + curusr_pass);
			ws.users.insertRaw(new Object[] {curusr, curusr_pass});
			
			String title = "How to decode " + new String(Api.genBytes(10));
			String text = "It's just random string.";
			System.out.println("Inserting post1: from " + curusr + " with title " + title);
			ws.posts.insertRaw(new Object[] {curusr, title, text});
			
			ws.saveDBs();
			System.out.println("Databases should be saved. Run this program again to load and print data...");
		} else {
			System.out.println("Displaying saved users:");
			while(ws.users.next()) {
				System.out.println("\t- " + ws.users.getString("name") + " with password hash " + ws.users.getInt("pass"));
			}
			System.out.println("");
			
			
			System.out.println("Displaying saved posts:");
			while(ws.posts.next()) {
				System.out.println("\t- " + ws.posts.getString("title") 
						+ " from user " + ws.posts.getString("user"));
				System.out.println("\t\t" + ws.posts.getString("text"));
			}
		}
	}
}
```

First run:
```
Loading database users!
Database users loaded!
Loading database posts!
Database posts loaded!
Databases do not exist, inserting some data...
Inserting user1: VPTvzpCJpY with password hash 747074543
Inserting user2: h9t2Xc8mBe with password hash 1163470248
Inserting post1: from h9t2Xc8mBe with title How to decode ABZhEyJbL9
Saving database users...
Database users successfuly saved to .../dbs/users.wndb!
Saving database posts...
Database posts successfuly saved to .../dbs/posts.wndb!
Databases should be saved. Run this program again to load and print data...
```

Second run:
```
Loading database users!
Database users loaded!
Loading database posts!
Database posts loaded!
Displaying saved users:
	- VPTvzpCJpY with password hash 747074543
	- h9t2Xc8mBe with password hash 1163470248

Displaying saved posts:
	- How to decode ABZhEyJbL9 from user h9t2Xc8mBe
		It's just random string.
```


### Database

High-level interface for any database. *TODO, 90% Done (missing table creation)*
