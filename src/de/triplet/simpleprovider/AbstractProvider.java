package de.triplet.simpleprovider;



import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public abstract class AbstractProvider extends ContentProvider {

    protected final String mTag;

    protected SQLiteDatabase mDatabase;

	protected AbstractProvider() {
		this("AbstractProvider");
	}

    protected AbstractProvider(String tag) {
        mTag = tag;
    }

    @Override
    public final boolean onCreate() {
        SQLiteOpenHelper dbHelper = new SQLHelper(getContext());
        try {
            mDatabase = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            mDatabase = null;
            Log.w(mTag, "Database Opening exception", e);
        }

        return mDatabase != null;
    }

    protected abstract String getAuthority();

    protected String getDatabaseFileName() {
        return getClass().getName().toLowerCase() + ".db";
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        return builder.where(selection, selectionArgs).query(mDatabase, projection, sortOrder);
    }

    private final SelectionBuilder buildBaseQuery(Uri uri) {
        SelectionBuilder builder = new SelectionBuilder(uri.getPathSegments().get(0));

        if (uri.getPathSegments().size() == 2) {
            builder.whereEquals(BaseColumns._ID, uri.getLastPathSegment());
        }

        return builder;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        List<String> segments = uri.getPathSegments();
        if (segments.size() != 1) {
            return null;
        }

        long rowId = mDatabase.insert(segments.get(0), null, values);

        if (rowId > -1) {
            getContext().getContentResolver().notifyChange(uri, null);

            return Uri.withAppendedPath(uri, String.valueOf(rowId));
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        return builder.where(selection, selectionArgs).delete(mDatabase);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        return builder.where(selection, selectionArgs).update(mDatabase, values);
    }

    private class SQLHelper extends SQLiteOpenHelper {

        private static final int SCHEMA_VERSION = 1;

        public SQLHelper(Context context) {
            super(context, getDatabaseFileName(), null, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* Stub. Do nothing. */
        }

    }

    private void createTables(SQLiteDatabase db) {
        for (Class<?> clazz : getClass().getClasses()) {
            Table table = clazz.getAnnotation(Table.class);
            if (table != null) {
                createTable(db, table.value(), clazz);
            }
        }
    }

    private void createTable(SQLiteDatabase db, String tableName, Class<?> tableClass) {
        ArrayList<String> columns = new ArrayList<String>();
        for (Field field : tableClass.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                try {
                    columns.add(field.get(null) + " " + column.value());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (");
        sql.append(TextUtils.join(", ", columns));
        sql.append(");");

        db.execSQL(sql.toString());
    }

}
