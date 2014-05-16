# SimpleProvider

Create simple ContentProviders for Android Applications reducing boilerplate code.

[![Build Status](https://travis-ci.org/Triple-T/simpleprovider.svg?branch=master)](https://travis-ci.org/Triple-T/simpleprovider)

## Goals

Writing Content Providers in Android Applications can be really annoying. In most cases there is not much complexity in regards of the data. Still, you have to write a great deal of boilerplate code to get going. This library is intended to ease up the creation of very simple Content Providers using annotations.

## Usage

SimpleProvider uses annotations on classes and fields to define the database structure. Everything else (database and table creation, URI matching, CRUD operations) is handled by the abstract class `AbstractProvider`.

### Writing your own Provider

To write your own ContentProvider you have to extend the `AbstractProvider` class and implement the abstract method `getAuthority()`:

```java
public class BlogProvider extends AbstractProvider {

    protected String getAuthority() {
        return "com.example.blog.DATA";
    }

}
```

Next you will want to define the tables and columns that make up your data. 
To do so, create inner classes inside your BlogProvider and use the provided annotations `@Table` and `@Column`:

```java
@Table
public class Post {

    @Column(Column.FieldType.INTEGER, primaryKey = true)
    public static final String KEY_ID = "_id";

    @Column(Column.FieldType.TEXT)
    public static final String KEY_TITLE = "title";
    
    @Column(Column.FieldType.TEXT)
    public static final String KEY_CONTENT = "content";

    @Column(Column.FieldType.TEXT)
    public static final String KEY_AUTHOR = "author";

}

@Table
public class Comment {

    @Column(Column.FieldType.INTEGER, primaryKey = true)
    public static final String KEY_ID = "_id";

    @Column(Column.FieldType.INTEGER)
    public static final String KEY_POST_ID = "post_id";
    
    @Column(Column.FieldType.TEXT)
    public static final String KEY_CONTENT = "content";

    @Column(Column.FieldType.TEXT)
    public static final String KEY_AUTHOR = "author";

}

```

In the example above we create a simple data structure for a blog application that has posts and comments on posts.

The `@Table`-Annotation registers a class as a database table. `AbstractProvider` will take care of creating tables called `posts` and `comments`. Note, how we used the plural version of the class name. You can override this behaviour by providing an additional String as the table name.

The `@Column`-Annotation defines a database column for that table. It requires a column type (One of `Column.FieldType.INTEGER`, `Column.FieldType.TEXT`,  `Column.FieldType.FLOAT` or `Column.FieldType.BLOB`). You may also add SQL extras, e.g. to define a column as the primary key for that table as we did for `Post.KEY_ID`.

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

## Upgrading the database

We may find ourselves in the situation where we need to change our database schema after we have released our app. Let's assume, we want to add a column to the Posts table that holds the creation date for a post. We obviously need to update the `Post` class to define the additional column:

```java
@Table
public class Post {

    // ... (previously defined columns)

    @Column(Column.FieldType.INTEGER, since = 2)
    public static final String KEY_CREATION_DATE = "creation_date";

}
```

Note, how we used the `since`-key of the `@Column`-Annotation to state that this column has been added to the database schema in version 2. To make sure all the upgrade routines are called we also have to override the `getSchemaVersion()` method:

```java
@Override
protected int getSchemaVersion() {
    return 2;
}
```

## License

	 The MIT License (MIT)
	 
	 Copyright (c) 2014 Christian Becker
	 Copyright (c) 2014 Björn Hurling

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

