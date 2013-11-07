package de.triplet.simpleprovider;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2) // FIXME: 4.4 is not yet supported
public class UtilsTest {

    @Test
    public void pluralize() {
        String expected = "whiskies";
        String actual = Utils.pluralize("Whisky");
        assertThat(actual).isEqualTo(expected);

        expected = "ytitties";
        actual = Utils.pluralize("YTitty");
        assertThat(actual).isEqualTo(expected);

        expected = "beers";
        actual = Utils.pluralize("Beer");
        assertThat(actual).isEqualTo(expected);

        expected = "ballantines";
        actual = Utils.pluralize("Ballantines");
        assertThat(actual).isEqualTo(expected);
    }

}
