package de.triplet.simpleprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleProviderTest {

    private static final String CONTENT_1 = "This is some content we want to store in a post!";
    private static final String CONTENT_2 = "Another post";
    TestProvider mProvider;
    ContentResolver mContentResolver;
    Uri mPostsUri;

    @Before
    public void setUp() {
        // fetch references to all the stuff we need like ContentProvider
        mProvider = new TestProvider();
        mContentResolver = Robolectric.application.getContentResolver();
        mPostsUri = Uri.parse("content://" + TestProvider.AUTHORITY + "/posts");

        // create and register the provider
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(TestProvider.AUTHORITY, mProvider);
    }

    @Test
    public void testInsert() {
        // Create content values to insert
        ContentValues values = new ContentValues();
        values.put(TestProvider.Post.CONTENT, CONTENT_1);
        mContentResolver.insert(mPostsUri, values);

        // Perform a "SELECT * FROM posts" query
        Cursor c = mContentResolver.query(mPostsUri, null, null, null, null);

        // Make sure the query has returned (the) one element
        assertNotNull("Resulting cursor must not be null", c);
        assertEquals("Query should return one post", c.getCount(), 1);
        assertTrue(c.moveToFirst());
        assertEquals("Entry should have the correct content",
                CONTENT_1, c.getString(c.getColumnIndex(TestProvider.Post.CONTENT)));
    }

    @Test
    public void testQueryById() {
        // Create content values to insert
        ContentValues values = new ContentValues();
        values.put(TestProvider.Post.CONTENT, CONTENT_1);
        values.put(TestProvider.Post.ID, 100);
        mContentResolver.insert(mPostsUri, values);

        // Create more content values to insert
        values = new ContentValues();
        values.put(TestProvider.Post.CONTENT, CONTENT_2);
        values.put(TestProvider.Post.ID, 101);
        mContentResolver.insert(mPostsUri, values);

        // Query by id 101
        String selection = TestProvider.Post.ID + " = ?";
        String[] selectionArgs = {"101"};
        Cursor c = mContentResolver.query(mPostsUri, null, selection, selectionArgs, null);

        // Make sure the query has returned the correct entity
        assertNotNull("Resulting cursor must not be null", c);
        assertTrue("We should be able to get the first entry", c.moveToFirst());
        assertEquals("Entry should have the correct content",
                CONTENT_2, c.getString(c.getColumnIndex(TestProvider.Post.CONTENT)));
        assertFalse("There shouldn't be any more entries", c.moveToNext());
    }

    @Test
    public void testSyncAdapterQuery() {
        assertNull("The query parameter should resolve to false", mPostsUri.getQueryParameter(TestProvider.QUERY_CALLER_IS_SYNC_ADAPTER));
        Uri syncUri = AbstractProvider.makeUriFromSyncAdapter(mPostsUri);
        assertTrue("The query parameter should resolve to true", syncUri.getQueryParameter(TestProvider.QUERY_CALLER_IS_SYNC_ADAPTER).equals("1"));
    }
}
