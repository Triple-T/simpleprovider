# SimpleProvider

Create simple ContentProviders for Android Applications reducing boilerplate code.

## Goals

Writing Content Providers in Android Applications can be really annoying. In most cases there is not much complexity in regards of the data. Still, you have to write a great deal of boilerplate code to get going. This library is intended to ease up the creation of very simple Content Providers using annotations.

## Usage

To write your own simple ContentProvider you have to extend the `AbstractProvider` class and implement the abstract method `getAuthority()`:

```java
public class BlogProvider extends AbstractProvider {

	protected String getAuthority() {
		// return an authority based on a constant or a resource string
	}

}
```

Next you will want to define the tables and their columns that make up your data. To do so, create inner classes and use the provided annotations `@Table` and `@Column`

## Extending the default behavior

TODO