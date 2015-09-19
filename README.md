 MainAPI
=========

Utility Java library with

* Easy data serialization and manipulation (see packages `eu.wordnice.cols`, `eu.wordnice.streams`, `eu.wordnice.db.*`, possibly `eu.wordnice.api`)
	* Using Input and Output interfaces, which are parents of DataInput and DataOutput
	* Using InputAdv and OutputAdv abstract classes, which are parents of Input and Output interfaces and IO streams
		* Serialize primitives, arrays, iterables or maps
		* To create or open any stream, use InputAdv.* and OutputAdv.* static methods
	* Using SQL interface with ResSet
		* Built-in support for MySQL, SQLite and any JDBC-based database
	* Using ResSet and ResSetDB - low-level database interface
		* Query, remove and update database in iterator-like way, insert any value
		* Serialize to binary (WNDB)
	* Using Database bridge
		* Select, Insert, Update or Delete from any ResSetDB or SQL based databases (no SQL injections)
	* Instead of creating and allocating new collection or map, use Immutables (package `eu.wordnice.cols`)
		* Create Map from one array (key1, val1, key2, val2, ...), two arrays or two iterables (first contains keys, second values)
		* Create Set (possibly List) from one array, one existing iterable, or create list from array with skipping elements
* MultiThreading, Timeouted-threads (see package `eu.wordnice.threads`)
	* Timeout blocks of code using TimeoutThread and Runa
	* Run piece of code on multiple threads using MultiThreading class with any MultiThreadable* interface instance
* Parse simple HTTP requests fast (see package `eu.wordnice.sockets`)
	* Create HIOServer to deploy simple HTTP server
	* Create HIO instanceof from given Socket to parse URL, HEAD, GET and POST parameters

Usable as Bukkit and Sponge plugin, with small changes compatible with plain Java. Under MIT license.

To build MainAPI as is for Bukkit & Sponge & Plain java, you have to got included craftbukkit (+ PlaceholderAPI), spongeapi (+ slf4j and guice) in your `Build Path`!

For code snippets with documentation see [MainAPIExamples repository](//github.com/wordnice/MainAPIExamples)!

