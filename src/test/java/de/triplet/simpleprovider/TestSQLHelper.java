package de.triplet.simpleprovider;

import android.content.Context;

public class TestSQLHelper extends SimpleSQLHelper {

    public TestSQLHelper(Context context) {
        super(context, "foo", 1);
    }

    @Table
    public static final class Foo {

        @Column(value = Column.FieldType.TEXT, primaryKey = true)
        public static final String BAR = "bar";
        @Column(value = Column.FieldType.FLOAT, since = 2)
        public static final String LATE = "late";

    }

}
