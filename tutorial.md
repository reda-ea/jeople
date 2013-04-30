# JEOPLe : the one minute tutorial

For this tutorial, we'll be assuming there is a well configured database, and the appropriate JDBC driver.
I'd suggest SQLite since it requires no server or anything, just a file.

### Data model

We'll be working on a database named `PERSON` with the following columns:
* `ID` an integer identifying the person
* `NAME` the name of the person
* `BORN` the person's day of birth

Note that we will use the typename `date` in SQLite even though it doesn't support dates (it will store the typename as it is and we'll use it when nedded)

But enough of that, let's start coding.

### Object model

First, we'll create a `Person` class extending the `Entity` class (the base class for all entities)

```java
public class Person extends Entity {
	public int id;
	public String name;
	public Date born;
}
```

And that's all we need. Really.

But let's give it a nice text output by overriding `toString`
```java
	@Override
	public String toString() {
		return this.id + ". " + this.name + " (born " + this.born + ")";
	}
```

### Querying objects

In order to do database operations, we need to define our data source, for that we'll use the `DataSource` interface.

The provided data source type for JDBC data sources is the `JDBCDataSource` class, so we could

```java
	DataSource db = new JDBCDataSource(driver, url, user, password);
```

For SQLite, we'll the more appropriate `SQLiteDataSource` implementation, that properly handles the missing data types (dates in our case) and some database locking logic.

```java
    DataSource db = new JDBCDataSource(filepath);
```

Note that we only have to provide the path to the database file.

#### Selecting

A select on the `person` table is as easy as a

```java
	db.select(Person.class);
```

Since `select` returns a standard Java `Iterator`, we can loop through the retrieved records with

```java
	for(Person p: db.select(Person.class))
		System.out.println(p.toString());
```

The same can be achieved by just printing the whole thing.

```java
	System.out.println(db.select(Person.class));
```

#### Saving and Deleting

Saving an object is just a method call away.
The following code will add a dot to each name in the database.

```java
	for(Person p: db.select(Person.class)) {
		p.name += ".";
		p.save();
	}
```

A new `Person` can be created in this data source with

```java
	db.create(Person.class);
```

and can be modified and saved just like any other `Person`.

Note that entities created through their own constructor (eg. `new Person()`) are not attached to a data source, and thus can not be saved.

Finally, deleting is also done right from the entity, through the `delete()` method.

```java
	for(Person p: db.select(Person.class)) {
		if(p.name.startsWith("J")
			p.delete();
	}
```

#### Advanced querying

What about those `where` and `orderBy` statements ?

*I'll write about them later.*

