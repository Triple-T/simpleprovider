# SimpleProvider

Create simple ContentProviders for Android Applications reducing boilerplate code.

## Goals

Writing Content Providers in Android Applications can be really annoying. In most cases there is not much complexity in regards of the data. Still, you have to write a great deal of boilerplate code to get going. This library is intended to ease up the creation of very simple Content Providers using annotations.

## Usage

SimpleProvider uses annotations on classes and fields to define the database structure. Everything else (database and table creation, URI matching, CRUD operations) is handled by the abstract class `AbstractProvider`.

### Writing your own Provider

To write your own ContentProvider you have to extend the `AbstractProvider` class and implement the abstract method `getAuthority()`:

```java
public class BlogProvider extends AbstractProvider {

  protected String getAuthority() {
		// return an authority based on a constant or a resource string
	}

}
```

Next you will want to define the tables and columns that make up your data. 
To do so, create inner classes inside your ContentProvider and use the provided annotations `@Table` and `@Column`:

```java
private static final String POSTS_TABLE = "posts";

@Table(POSTS_TABLE)
public static final class Posts {

    @Column("INTEGER primary key")
    public static final String KEY_ID = "_id";

    @Column("TEXT")
    public static final String KEY_TITLE = "title";
    
    @Column("TEXT")
    public static final String KEY_CONTENT = "content";

    @Column("TEXT")
    public static final String KEY_AUTHOR = "author";

}

private static final String COMMENTS_TABLE = "comments";

@Table(COMMENTS_TABLE)
public static final class Comments {

    @Column("INTEGER primary key")
    public static final String KEY_ID = "_id";

    @Column("INTEGER")
    public static final String KEY_POST_ID = "post_id";
    
    @Column("TEXT")
    public static final String KEY_CONTENT = "content";

    @Column("TEXT")
    public static final String KEY_AUTHOR = "author";

}

```

In the example above we create a simple data structure for a blog application that has posts and comments on posts.

The `@Table`-Annotation registers a class as a database table and requires a name for that table. Note, that we used a constant for that name so we may reference it if we need that.

The `@Column`-Annotation defines a database column for that table. It requires a column type like `INTEGER` or `TEXT`. You may also define SQL extras like `default 0` or `primary key` as we used for `Comments.KEY_ID`.

That's all. The `AbstractProvider` will handle the database creation and default CRUD operations for us.

### Accessing your Provider

The above example creates two tables that can be accessed using the standard `insert()`, `query()`, `update()`, `delete()` operations provided by the `ContentResolver` class. `AbstractProvider` automatically handles the standard URIs that directly access a single table.

Based on the example above, and assuming your authority is something like ``com.example.blog.DATA``, then your ContentProvider will automatically handle the following URIs:

```
com.example.blog.DATA/posts
com.example.blog.DATA/posts/*
com.example.blog.DATA/comments
com.example.blog.DATA/comments/*
```

## Extending the default behavior

In the example we created a column for comments called `post_id` that will be used as a foreign key to the Posts table.
However, `BlogProvider` does not yet respond to URIs such as the following:
```
com.example.blog.DATA/posts/*/comments
com.example.blog.DATA/posts/*/comments/*
```
To add that feature you have to extend the default behavior by overriding some methods.

```java
// TODO
```

## Upgrading the database

We may find ourselves in the situation where we need to change our database schema after we have released our app. Let's assume, we want to add a column to the Posts table that holds the creation date for a post. First of all, we obviously need to update the `Posts` class to define the additional column:

```java
@Table(POSTS_TABLE)
public static final class Posts {
	
	// ... (previously defined columns)
	
	@Column("Integer")
	public static final String KEY_CREATION_DATE = "creation_date";

}
```

On a fresh installation, the new column will be created automatically when the database is set up. However, this is not enough if the database has already been created by the system. Instead we need to use the `onUpgrade()` method. The first step is to announce a change to the database schema by overriding the `getSchemaVersion()` method:

```java
@Override
protected int getSchemaVersion() {
	return 2;
}
```

Next, we have to override `onUpgrade()` to add the new column to the existing table:

```java
@Override
protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// use db.execSQL() to add and remove columns
}
```
