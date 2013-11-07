package de.triplet.simpleprovider;

public class TestProvider extends AbstractProvider {

    @Override
    protected String getAuthority() {
        return null;
    }

    @Table
    public static final class Foo {

        @Column(value = Column.FieldType.TEXT, primaryKey = true)
        public static final String BAR = "bar";
        @Column(value = Column.FieldType.FLOAT, since = 2)
        public static final String LATE = "late";

    }

}
