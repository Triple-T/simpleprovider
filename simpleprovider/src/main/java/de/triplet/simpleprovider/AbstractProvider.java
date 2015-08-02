package de.triplet.simpleprovider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
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
    protected Object mMatcherSynchronizer;
    protected UriMatcher mMatcher;
    protected MatchDetail[] mMatchDetails;

    protected AbstractProvider() {
        mLogTag = getClass().getName();
        mMatcherSynchronizer = new Object();
    }

    @Override
    public boolean onCreate() {
        try {
            SimpleSQLHelper dbHelper = new SimpleSQLHelper(getContext(), getDatabaseFileName(),
                    getSchemaVersion()) {

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    super.onUpgrade(db, oldVersion, newVersion);

                    // Call onUpgrade of outer class so derived classes can extend the default
                    // behaviour
                    AbstractProvider.this.onUpgrade(db, oldVersion, newVersion);
                }

            };
            dbHelper.setTableClass(getClass());
            mDatabase = dbHelper.getWritableDatabase();
            return true;
        } catch (SQLiteException e) {
            Log.w(mLogTag, "Database Opening exception", e);
        }

        return false;
    }

    /**
     * Initializes the matcher, based on the tables contained within the subclass,
     * also guarantees to have initialized the mTableNames[] array as well.
     */
    protected void initializeMatcher() {
        // initialize the UriMatcher once, use a synchronized block
        // here, so that subclasses can implement this by static initialization if
        // they want.
        synchronized (mMatcherSynchronizer) {
            if(mMatcher != null) {
                return;
            }

            mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            List<MatchDetail> details = new ArrayList<>();

            String authority = getAuthority();

            for (Class<?> clazz : getClass().getClasses()) {
                Table table = clazz.getAnnotation(Table.class);
                if (table != null) {
                    String tableName = Utils.getTableName(clazz, table);
                    String mimeName = Utils.getMimeName(clazz, table);

                    // Add the plural version
                    mMatcher.addURI(authority, tableName, details.size());
                    details.add(new MatchDetail(tableName, "vnd.android.cursor.dir/vnd." + getAuthority() + "." + mimeName, false));

                    // Add the singular version
                    mMatcher.addURI(authority, tableName + "/#", details.size());
                    details.add(new MatchDetail(tableName, "vnd.android.cursor.item/vnd." + getAuthority() + "." + mimeName, true));
                }
            }

            // Populate the rest.
            mMatchDetails = details.toArray(new MatchDetail[0]);
        }
    }

    private static class MatchDetail
    {
        public final String tableName;
        public final String mimeType;
        public final boolean forceIdColumn;

        public MatchDetail(String tableName, String mimeType, boolean forceIdColumn) {
            this.tableName = tableName;
            this.mimeType = mimeType;
            this.forceIdColumn = forceIdColumn;
        }
    }

    /**
     * Called when the database needs to be updated and after <code>AbstractProvider</code> has
     * done its own work. That is, after creating columns that have been added using the
     * {@link Column#since()} key.<br />
     * <br />
     * For example: Let <code>AbstractProvider</code> automatically create new columns. Afterwards,
     * do more complicated work like calculating default values or dropping other columns inside
     * this method.<br />
     * <br />
     * This method executes within a transaction. If an exception is thrown, all changes will
     * automatically be rolled back.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // override if needed
    }

    protected String getDatabaseFileName() {
        return getClass().getName().toLowerCase() + ".db";
    }

    /**
     * Returns the current schema version. This number will be used to automatically trigger
     * upgrades and downgrades. You may override this method in derived classes if anything has
     * changed in the schema classes.
     *
     * @return Current schema version.
     */
    protected int getSchemaVersion() {
        return 1;
    }

    protected abstract String getAuthority();

    @Override
    public String getType(Uri uri) {
        initializeMatcher();

        int match = mMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH) {
            return null;
        }

        return mMatchDetails[match].mimeType;
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
        initializeMatcher();

        int match = mMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported content uri");
        }

        MatchDetail detail = mMatchDetails[match];

        SelectionBuilder builder = new SelectionBuilder(detail.tableName);

        if(detail.forceIdColumn) {
            builder.whereEquals(BaseColumns._ID, uri.getLastPathSegment());
        }

        return builder;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        initializeMatcher();

        int match = mMatcher.match(uri);

        if(match == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported content uri");
        }

        long rowId = mDatabase.insert(mMatchDetails[match].tableName, null, values);

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
