package de.triplet.simpleprovider;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void pluralize() {
        String expected = "whiskies";
        String actual = Utils.pluralize("Whisky");
        assertEquals(expected, actual);

        expected = "ytitties";
        actual = Utils.pluralize("YTitty");
        assertEquals(expected, actual);

        expected = "ways";
        actual = Utils.pluralize("Way");
        assertEquals(expected, actual);

        expected = "boys";
        actual = Utils.pluralize("Boy");
        assertEquals(expected, actual);

        expected = "journeys";
        actual = Utils.pluralize("Journey");
        assertEquals(expected, actual);

        expected = "beers";
        actual = Utils.pluralize("Beer");
        assertEquals(expected, actual);

        expected = "ballantines";
        actual = Utils.pluralize("Ballantines");
        assertEquals(expected, actual);
    }

    @Test
    public void pluralizeWithDifferentLocale() {
        String expected = "interests";
        String actual;
        Locale current = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            actual = Utils.pluralize("Interest");
        } finally {
            Locale.setDefault(current);
        }

        assertEquals(expected, actual);
    }

}
