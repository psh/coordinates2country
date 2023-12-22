package io.github.coordinates2country

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader

/**
 * Tests for coordinates2country, ensuring it can be called correctly from KOTLIN
 */
class Coordinates2CountryTest {
    @Test
    fun testOneCoordinate() {
        assertEquals("Germany", country(50.0, 10.0))
    }

    @Test
    fun testOneQID() {
        assertEquals("183", countryQID(50.0, 10.0))
    }

    @Test
    fun testBlackPixel() {
        //  Black pixel at x=1204 y=713, which by the way might be a mistake for Europa Island
        assertEquals("France", country(-23.7, 39.8))
    }

    @Test
    fun testSea() {
        // Sea a dozen pixels far from of Madagascar
        assertEquals("Madagascar", country(-31.0, 45.0))
    }

    @Test
    fun testAllSamples() {
        BufferedReader(FileReader("./data/countries.csv")).use { br ->
            var line = br.readLine()
            line = br.readLine() // Skip CSV header

            while (line != null) {
                val parts = line.split(",").dropLastWhile { it.isEmpty() }
                if (parts.size > 1 && parts[0].isNotEmpty()) { // Skip countries not yet implemented
                    assertCountry(
                        country = parts[1],
                        latitude = parts[2].toDouble(),
                        longitude = parts[3].toDouble()
                    )
                }

                line = br.readLine()
            }
        }
    }

    private fun assertCountry(country: String, latitude: Double, longitude: Double) =
        assertEquals(country, country(latitude, longitude))
}