package de.triplet.simpleprovider;

public class TestProvider extends AbstractProvider {

    public static final String AUTHORITY = "de.triplet.simpleprovider.TESTDATA";

    @Override
    protected String getAuthority() {
        return AUTHORITY;
    }

    @Table
    public class Post {

        @Column(Column.FieldType.INTEGER)
        public static final String ID = "_id";

        @Column(Column.FieldType.TEXT)
        public static final String CONTENT = "content";
    }
}
