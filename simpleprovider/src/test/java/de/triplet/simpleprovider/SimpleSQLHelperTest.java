package de.triplet.simpleprovider;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleSQLHelperTest {

    private TestSQLHelper mProvider;
    private SQLiteDatabase mDatabase;

    @Before
    public void setUp() {
        mProvider = new TestSQLHelper(Robolectric.application);
        mDatabase = mock(SQLiteDatabase.class);
    }

    @Test
    public void onCreate() {
        mProvider.onCreate(mDatabase);

        verify(mDatabase).execSQL("CREATE TABLE foos (bar TEXT PRIMARY KEY, late FLOAT NOT NULL UNIQUE, time REAL);");
    }

    @Test
    public void onUpgrade() {
        mProvider.onUpgrade(mDatabase, 1, 2);

        verify(mDatabase).execSQL("ALTER TABLE foos ADD COLUMN late FLOAT NOT NULL UNIQUE;");
    }

    @Test
    public void onUpgradeEmpty() {
        mProvider.onUpgrade(mDatabase, 2, 3);

        verifyNoMoreInteractions(mDatabase);
    }

    @Test(expected = SQLiteException.class)
    public void onDowngrade() {
        mProvider.onDowngrade(mDatabase, 2, 1);
    }

}