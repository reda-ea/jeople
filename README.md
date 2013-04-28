# JEOPLe
Easy Object Persistence for Java

### What is it ?

It allows you to do

```java
	db.select(Person.class).where(Person.olderThan(10)).orderBy(Person.nameLength())
```

### Really. What is it ?

Basically, it's an Object persistence layer with the following design principles:

1. No configuration. None. Just code.
2. No generated code. Just the code you type. No boilerplate code either.
3. Natural Java syntax (as in the example above). Also, standard types are used whenever possible (eg. the select query above is an `Iterable<Person>`).
4. Client side conditions and sorting<sup>[1]</sup>, making it possible to define "where" conditions on Java `Object`s instead of database SQL, and to sort using standard Java `Comparator`s. 
5. Data source agnostic design. It defaults to JDBC, but can as well be used to store data in a file or over a webservice...
And most importantly
6. Ease of use. This [1 minute tutorial][tuto1] is all you need to get started.

### Ok but "Jeoples" ? seriously ? what name is this ?

It sounds like "people" and looks like an acronym for "Java Easy Object Persistance Layer".

### OK, I want it! Where is it ?

Here, take [this jar][jar].

**Please note that the API is still unstable for now.**
**DO NOT DEPEND ON THE API SYNTAX UNLESS YOU ARE READY TO REWRITE CODE**

[1] Of course, this means a loss in performance, especially for very large tables, since all records are retrieved from the database to be checked on the client side (by Java code), but this is balanced by the convenience of doing pure object oriented filtering.
 *Plus, this is still a proof of concept, I'm not worrying about performance yet.*

[tuto1]: ./tutorial.md
[jar]: ./jeople-v001.jar
