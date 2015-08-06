package de.triplet.simpleprovider;

public class TestProvider extends AbstractProvider {

    public static final String AUTHORITY = "de.triplet.simpleprovider.TESTDATA";

    @Override
    protected String getAuthority() {
        return AUTHORITY;
    }

    @Table
    public class Post {

        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String ID = "_id";

        @Column(Column.FieldType.TEXT)
        public static final String CONTENT = "content";
    }

    @Table
    public class Comment {
        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String ID = "_id";

        @Column(Column.FieldType.TEXT)
        public static final String RESPONSE = "response";

        @Column(value = Column.FieldType.INTEGER, notNull = true)
        @ForeignKey(references = Post.class)
        public static final String POST = "post_id";
    }
}
