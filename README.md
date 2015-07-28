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

```

First output:

```

```

Second output:

```

```