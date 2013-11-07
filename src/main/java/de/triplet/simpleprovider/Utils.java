package de.triplet.simpleprovider;

import android.text.TextUtils;

import java.lang.reflect.Field;

public final class Utils {

    private Utils() {
        /* Utility classes must not have a public constructor */
    }

    public static String getTableName(Class<?> clazz, Table table) {
        String value = table.value();
        if (TextUtils.isEmpty(value)) {
            return pluralize(clazz.getSimpleName());
        } else {
            return value;
        }
    }

    public static String pluralize(String string) {
        string = string.toLowerCase();

        if (string.endsWith("s")) {
            return string;
        } else if (string.endsWith("y")) {
            return string.replaceAll("y$", "ies");
        } else {
            return string + "s";
        }
    }

    public static String getColumnConstraint(Field field, Column column) throws IllegalAccessException {
        return field.get(null) + " " + column.value() + (column.primaryKey() ? " PRIMARY KEY" : "");
    }

}
