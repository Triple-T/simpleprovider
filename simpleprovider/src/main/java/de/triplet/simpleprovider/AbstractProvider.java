
package de.triplet.simpleprovider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused") // Public API
public abstract class AbstractProvider extends ContentProvider {

    protected final String mLogTag;
    protected SQLiteDatabase mDatabase;

    protected AbstractProvider() {
        mLogTag = getClass().getName();
    }

    @Override
    public final boolean onCreate() {
        try {
            SimpleSQLHelper dbHelper = new SimpleSQLHelper(getContext(), getDatabaseFileName(), getSchemaVersion());
            dbHelper.setTableClass(getClass());
            mDatabase = dbHelper.getWritableDatabase();
            return true;
        } catch (SQLiteException e) {
            Log.w(mLogTag, "Database Opening exception", e);
        }

        return false;
    }

    protected String getDatabaseFileName() {
        return getClass().getName().toLowerCase() + ".db";
    }

    /**
     * Returns the current schema version. This number will be used to automatically trigger
     * upgrades and downgrades. You may override this method in derived classes if anything has
     * changed in the schema classes.
     * @return
     */
    protected int getSchemaVersion() {
        return 1;
    }

    protected abstract String getAuthority();

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        final Cursor cursor = builder.where(selection, selectionArgs).query(mDatabase, projection,
                sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContentResolver(), uri);
        }
        return cursor;
    }

    private ContentResolver getContentResolver() {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        return context.getContentResolver();
    }

    private SelectionBuilder buildBaseQuery(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments == null) {
            return null;
        }

        SelectionBuilder builder = new SelectionBuilder(pathSegments.get(0));

        if (pathSegments.size() == 2) {
            builder.whereEquals(BaseColumns._ID, uri.getLastPathSegment());
        }

        return builder;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        List<String> segments = uri.getPathSegments();
        if (segments == null || segments.size() != 1) {
            return null;
        }

        long rowId = mDatabase.insert(segments.get(0), null, values);

        if (rowId > -1) {
            getContentResolver().notifyChange(uri, null);

            return ContentUris.withAppendedId(uri, rowId);
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        int count = builder.where(selection, selectionArgs).delete(mDatabase);

        if (count > 0) {
            getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SelectionBuilder builder = buildBaseQuery(uri);
        int count = builder.where(selection, selectionArgs).update(mDatabase, values);

        if (count > 0) {
            getContentResolver().notifyChange(uri, null);
        }

        return count;

    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        ContentProviderResult[] result = null;
        mDatabase.beginTransaction();
        try {
            result = super.applyBatch(operations);
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        return result;
    }

}
