package de.triplet.simpleprovider;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AbstractProviderTest {

    private TestProvider mProvider;
    private SQLiteDatabase mDatabase;

    @Before
    public void setUp() {
        mProvider = new TestProvider();
        mDatabase = mock(SQLiteDatabase.class);
    }

    @Test
    public void onCreate() {
        mProvider.onCreate(mDatabase);

        verify(mDatabase).execSQL("CREATE TABLE foo (bar BAR, late LATE);");
    }

    @Test
    public void onUpgrade() {
        mProvider.onUpgrade(mDatabase, 1, 2);

        verify(mDatabase).execSQL("ALTER TABLE foo ADD COLUMN late LATE;");
    }

    @Test
    public void onUpgradeEmpty() {
        mProvider.onUpgrade(mDatabase, 2, 3);

        verifyNoMoreInteractions(mDatabase);
    }

    @Test(expected = SQLiteException.class)
    public void testOnDowngrade() {
        mProvider.onDowngrade(mDatabase, 2, 1);
    }

}