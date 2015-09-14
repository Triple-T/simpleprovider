package de.triplet.simpleprovider;

import android.content.ContentResolver;
import android.content.ContentUris;
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
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleProviderTest {

    private static final String CONTENT_1 = "This is some content we want to store in a post!";
    private static final String CONTENT_2 = "Another post";
    private static final String COMMENT_RESPONSE_1 = "this is a comment!";
    private static final String COMMENT_RESPONSE_2 = "this is a second comment!";
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
        assertEquals("Entry should have the correct content", CONTENT_1, c.getString(c.getColumnIndex(TestProvider.Post.CONTENT)));
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
        assertEquals("Entry should have the correct content", CONTENT_2, c.getString(c.getColumnIndex(TestProvider.Post.CONTENT)));
        assertFalse("There shouldn't be any more entries", c.moveToNext());
    }

    @Test
    public void testInsertByParentId() {
        ContentValues values = new ContentValues();
        values.put(TestProvider.Post.CONTENT, "first post");

        Uri firstPost = mContentResolver.insert(mPostsUri, values);

        ContentValues comment = new ContentValues();

        comment.put(TestProvider.Comment.RESPONSE, COMMENT_RESPONSE_1);

        Uri commentUri = mContentResolver.insert(Uri.parse("content://" + TestProvider.AUTHORITY + "/posts/" + Long.toString(ContentUris.parseId(firstPost)) + "/comments"), comment);

        // Perform a "SELECT * FROM posts" query
        Cursor c = mContentResolver.query(commentUri, new String[] { TestProvider.Comment.POST, TestProvider.Comment.RESPONSE}, null, null, null);

        // Make sure the query has returned (the) one element
        assertNotNull("Resulting cursor must not be null", c);
        assertEquals("Query should return one comment", c.getCount(), 1);
        assertTrue(c.moveToFirst());
        assertEquals("Entry should have the correct response", COMMENT_RESPONSE_1, c.getString(c.getColumnIndex(TestProvider.Comment.RESPONSE)));
        assertEquals("Entry should have the correct post_id", ContentUris.parseId(firstPost), c.getLong(c.getColumnIndex(TestProvider.Comment.POST)));

    }

    @Test
    public void testQueryByParentId() {
        ContentValues values = new ContentValues();
        values.put(TestProvider.Post.CONTENT, "first post");

        Uri firstPost = mContentResolver.insert(mPostsUri, values);

        values.put(TestProvider.Post.CONTENT, "second");

        Uri secondPost = mContentResolver.insert(mPostsUri, values);

        ContentValues firstComment = new ContentValues();
        firstComment.put(TestProvider.Comment.RESPONSE, COMMENT_RESPONSE_1);
        firstComment.put(TestProvider.Comment.POST, ContentUris.parseId(firstPost));

        Uri commentOne = mContentResolver.insert(Uri.parse("content://" + TestProvider.AUTHORITY + "/comments"), firstComment);

        ContentValues secondComment = new ContentValues();
        secondComment.put(TestProvider.Comment.RESPONSE, COMMENT_RESPONSE_2);
        secondComment.put(TestProvider.Comment.POST, ContentUris.parseId(secondPost));

        mContentResolver.insert(Uri.parse("content://" + TestProvider.AUTHORITY + "/comments"), secondComment);

        Uri firstPostComments = Uri.parse("content://" + TestProvider.AUTHORITY + "/posts/" + ContentUris.parseId(firstPost) + "/comments");

        // Now get all the comments for the first post.
        Cursor c = mContentResolver.query(firstPostComments, new String[] { TestProvider.Comment.ID, TestProvider.Comment.POST, TestProvider.Comment.RESPONSE}, null, null, null);

        // Make sure the query has returned (the) one element
        assertNotNull("Resulting cursor must not be null", c);
        assertEquals("Query should return one comment", c.getCount(), 1);
        assertTrue(c.moveToFirst());
        assertEquals("Entry should have the correct id", ContentUris.parseId(commentOne), c.getLong(c.getColumnIndex(TestProvider.Comment.ID)));
        assertEquals("Entry should have the correct response", COMMENT_RESPONSE_1, c.getString(c.getColumnIndex(TestProvider.Comment.RESPONSE)));
        assertEquals("Entry should have the correct post_id", ContentUris.parseId(firstPost), c.getLong(c.getColumnIndex(TestProvider.Comment.POST)));
    }

    @Test
    public void testGetType() {
        String actual = mContentResolver.getType(mPostsUri);

        assertEquals("vnd.android.cursor.dir/vnd." + TestProvider.AUTHORITY + ".post", actual);

        actual = mContentResolver.getType(ContentUris.withAppendedId(mPostsUri , 1));

        assertEquals("vnd.android.cursor.item/vnd." + TestProvider.AUTHORITY + ".post", actual);
    }
}
