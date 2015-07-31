package de.triplet.simpleprovider;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.Locale;

final class Utils {

    private Utils() {
        /* Utility classes must not have a public constructor */
    }

    static String getTableName(Class<?> clazz, Table table) {
        String value = table.value();
        if (TextUtils.isEmpty(value)) {
            return pluralize(clazz.getSimpleName());
        } else {
            return value;
        }
    }

    static String pluralize(String string) {
        string = string.toLowerCase(Locale.US);

        if (string.endsWith("s")) {
            return string;
        } else if (string.endsWith("ay")) {
            return string.replaceAll("ay$", "ays");
        } else if (string.endsWith("ey")) {
            return string.replaceAll("ey$", "eys");
        } else if (string.endsWith("oy")) {
            return string.replaceAll("oy$", "oys");
        } else if (string.endsWith("uy")) {
            return string.replaceAll("uy$", "uys");
        } else if (string.endsWith("y")) {
            return string.replaceAll("y$", "ies");
        } else {
            return string + "s";
        }
    }

    static String getColumnConstraint(Field field, Column column) throws IllegalAccessException {
        return field.get(null) + " " + column.value()
                + (column.primaryKey() ? " PRIMARY KEY" : "")
                + (column.notNull() ? " NOT NULL" : "")
                + (column.unique() ? " UNIQUE" : "");
    }

}
