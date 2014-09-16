package de.triplet.simpleprovider.sample;

import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;

/**
 * Sample Provider that demonstrates the usage of AbstractProvider. This class does the following:
 * <ul>
 *     <li>Defines the Authority that will be used in the AndroidManifest</li>
 *     <li>Defines two tables</li>
 *     <li>Shows how to use the <code>since</code> attribute on tables and columns to automatically upgrade the schema</li>
 * </ul>
 */
public class BlogProvider extends AbstractProvider {

    // we have made two changes to our schema so we define the version manually.
    private static final int SCHEMA_VERSION = 3;

    @Override
    protected String getAuthority() {
        return getContext().getString(R.string.authority);
    }

    @Table
    public class Post {
        @Column(Column.FieldType.INTEGER)
        public static final String ID = "_id";

        @Column(Column.FieldType.TEXT)
        public static final String CONTENT = "content";

        @Column(Column.FieldType.TEXT)
        public static final String AUTHOR = "author";
    }

    // this table was added in schema version 2
    @Table(since = 2)
    public class Comment {
        @Column(Column.FieldType.INTEGER)
        public static final String ID = "_id";

        @Column(Column.FieldType.TEXT)
        public static final String CONTENT = "content";

        // this column was added in schema version 3
        @Column(value = Column.FieldType.TEXT, since = 3)
        public static final String AUTHOR = "author";

        @Column(Column.FieldType.INTEGER)
        public static final String POST_ID = "post_id";

        @Column(Column.FieldType.REAL)
        public static final String TIMESTAMP = "timestamp";
    }

    // we override the schema version so it reflects the changes to our schema. That way
    // the comments table will be added (since version 2) and the additional field author on
    // comments will be added (since schema version 3).
    @Override
    protected int getSchemaVersion() {
        return SCHEMA_VERSION;
    }
}
