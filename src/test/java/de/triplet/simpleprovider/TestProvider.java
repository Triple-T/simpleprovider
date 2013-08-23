package de.triplet.simpleprovider;

public class TestProvider extends AbstractProvider {

    @Override
    protected String getAuthority() {
        return null;
    }

    @Table("foo")
    public static final class Foo {

        @Column("BAR")
        public static final String KEY_BAR = "bar";
        @Column(value = "LATE", since = 2)
        public static final String KEY_LATE = "late";

    }

}
