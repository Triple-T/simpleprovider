package de.triplet.simpleprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SimpleSQLHelper extends SQLiteOpenHelper {

    private Class<?> mTableClass;

    public SimpleSQLHelper(Context context, String fileName, int schemaVersion) {
        super(context, fileName, null, schemaVersion);
    }

    public void setTableClass(Class<?> tableClass) {
        mTableClass = tableClass;
    }

    private Class<?> getTableClass() {
        if (mTableClass != null) {
            return mTableClass;
        } else {
            return getClass();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Override in derived classes */
        upgradeTables(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Override in derived classes */
        throw new SQLiteException("Can't downgrade database from version " + oldVersion + " to "
                + newVersion);
    }

    private void createTables(SQLiteDatabase db) {
        for (Class<?> clazz : getTableClass().getClasses()) {
            Table table = clazz.getAnnotation(Table.class);
            if (table != null) {
                createTable(db, Utils.getTableName(clazz, table), clazz);
            }
        }
    }

    private void createTable(SQLiteDatabase db, String tableName, Class<?> tableClass) {
        ArrayList<String> columns = new ArrayList<String>();
        for (Field field : tableClass.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                try {
                    columns.add(Utils.getColumnConstraint(field, column));
                } catch (Exception e) {
                    Log.e("SimpleSQLHelper", "Error accessing " + field, e);
                }
            }
        }

        db.execSQL("CREATE TABLE " + tableName + " (" + TextUtils.join(", ", columns) + ");");
    }

    private void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SimpleSQLHelper", "Upgrading Tables: " + oldVersion + " -> " + newVersion);

        for (Class<?> clazz : getTableClass().getClasses()) {
            Table table = clazz.getAnnotation(Table.class);
            if (table != null) {
                int since = table.since();
                if (oldVersion < since && newVersion >= since) {
                    createTable(db, Utils.getTableName(clazz, table), clazz);
                } else {
                    upgradeTable(db, oldVersion, newVersion, Utils.getTableName(clazz, table), clazz);
                }
            }
        }
    }

    private void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion, String tableName, Class<?> tableClass) {
        for (Field field : tableClass.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                int since = column.since();
                if (oldVersion < since && newVersion >= since) {
                    try {
                        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + Utils.getColumnConstraint(field, column) + ";");
                    } catch (Exception e) {
                        Log.e("SimpleSQLHelper", "Error accessing " + field, e);
                    }
                }
            }
        }
    }

}
