package io.github.coordinates2country;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for coordinates2country, ensuring it can be called correctly from JAVA
 */
public class LibraryTest {
    @Test
    public void testOneCoordinate() {
        assertEquals("Germany", Coordinates2Country.country(50, 10));
    }

    @Test
    public void testOneQID() {
        assertEquals("183", Coordinates2Country.countryQID(50, 10));
    }

    @Test
    public void testBlackPixel() {
        assertEquals("France", Coordinates2Country.country(-23.7, 39.8)); // Black pixel at x=1204 y=713, which by the way might be a mistake for Europa Island
    }

    @Test
    public void testSea() {
        assertEquals("Madagascar", Coordinates2Country.country(-31, 45)); // Sea a dozen pixels far from of Madagascar
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void testAllSamples() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("./data/countries.csv"));
        String line = br.readLine();
        line = br.readLine(); // Skip CSV header

        while (line != null) {
            System.out.println(line);
            String[] parts = line.split(",");
            if (parts.length > 1 && !parts[0].isEmpty()) { // Skip countries not yet implemented
                String country = parts[1];
                double latitude = Double.parseDouble(parts[2]);
                double longitude = Double.parseDouble(parts[3]);
                assertEquals(country, Coordinates2Country.country(latitude, longitude));
            }

            line = br.readLine();
        }
        br.close();
    }

    /* Disabled because too lengthy @Test public void testAllPixels() {
        for (int latitude = -90; latitude <= 90; latitude += 0.1) { // Resolution is less than 10 pixels per degree.
            for (int longitude = -180; longitude <= 180; longitude += 0.1) {
                Coordinates2Country.country(latitude, longitude);
            }
        }
    }*/

}
