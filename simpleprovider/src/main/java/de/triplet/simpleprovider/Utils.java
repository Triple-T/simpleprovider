package de.triplet.simpleprovider;

import android.provider.BaseColumns;
import android.text.TextUtils;

import java.lang.reflect.Field;

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

    static String getMimeName(Class<?> clazz, Table table) {
        String mimeName = table.mimeSuffix();
        if(TextUtils.isEmpty(mimeName)) {
            return clazz.getSimpleName().toLowerCase(java.util.Locale.US);
        }
        return mimeName;
    }

    static String pluralize(String string) {
        string = string.toLowerCase();

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

    static String getColumnConstraint(Field field, Column column, ForeignKey foreignKey) throws IllegalAccessException {
        StringBuilder columnDefinition = new StringBuilder();
        columnDefinition.append(field.get(null));
        columnDefinition.append(" " + column.value());

        if(column.primaryKey()) {
            columnDefinition.append(" PRIMARY KEY");
        }

        if(column.notNull()) {
            columnDefinition.append(" NOT NULL");
        }

        if(column.unique()) {
            columnDefinition.append(" UNIQUE");
        }

        // According to SQLite documentation don't have to worry about the order of table creation
        // when creating foreign key constraints, as they are only checked when DML statements
        // are executed; see: https://www.sqlite.org/foreignkeys.html#fk_schemacommands
        if(foreignKey != null) {
            columnDefinition.append(" REFERENCES ");
            Class<?> parentClazz = foreignKey.references();
            Table parentTable = parentClazz.getAnnotation(Table.class);
            columnDefinition.append(getTableName(parentClazz, parentTable));
            columnDefinition.append("(" + BaseColumns._ID + ")"); // always use _id as the parent key.

            // defer the constraints so that batch operations can be done unordered.
            columnDefinition.append(" DEFERRABLE INITIALLY DEFERRED");
        }

        return columnDefinition.toString();
    }

}
