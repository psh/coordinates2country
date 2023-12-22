package io.github.coordinates2country

import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.imageio.ImageIO

private const val WIDTH = 2400 // Width of the map image.
private const val HEIGHT = 949 // Height of the map image.

private const val GREENWICH_X = 939 // At what pixel is the Greenwich longitude.
private const val EQUATOR_Y = 555 // At what pixel is the Equator latitude.
private const val MIN_LATITUDE = -58.55 // South tip of Sandwich Islands
private const val MAX_LATITUDE = 83.64 // North tip of Canada

/**
 * Converts coordinates (example: 50.1, 10.2) into a country identifier (example: "Germany").
 * @param wikidataOrNot Whether to return the result as a Wikidata QID number or a country name in English.
 */
internal fun country(latitude: Double, longitude: Double, wikidataOrNot: Boolean): String? {
    if (longitude < -180 || longitude > 180 || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
        return null // TODO return Russia or Canada or Chile/etc based on longitude and pole.
    }

    // https://en.wikipedia.org/wiki/Equirectangular_projection
    val x = (WIDTH + (GREENWICH_X + longitude * WIDTH / 360).toInt()) % WIDTH
    val y = (EQUATOR_Y - latitude * HEIGHT / (MAX_LATITUDE - MIN_LATITUDE)).toInt()

    // Each country is a shade of gray in this image which is a map of the world using the https://en.wikipedia.org/wiki/Equirectangular_projection with phi0=0 and lambda0=0.
    // Load it within this method and do not cache it, in order to allow garbage collection between each call, because we consider that low memory usage is more important than speed.
    return with(ImageIO.read(resourceStream("/countries-8bitgray.png"))) {
        // If are in the sea or right on a border, check surrounding pixels.
        countryFromPixel(x, y, wikidataOrNot, this) ?: searchForCountry(x, y, wikidataOrNot, this)
    }
}

private fun searchForCountry(
    x: Int, y: Int, wikidataOrNot: Boolean, map: BufferedImage?
): String {
    var radius = 1
    var country: String?
    do {
        country = countryAtDistance(x, y, radius, wikidataOrNot, map)
        radius++

    } while (country == null)
    return country
}

/**
 * Finds the most represented country in the pixels situated at given distance ("radius") of given pixel.
 * Distance is currently not implemented as real radius, but as a rectangle.
 */
internal fun countryAtDistance(
    centerX: Int, centerY: Int, radius: Int, wikidataOrNot: Boolean, map: BufferedImage?
): String? {
    println("radius=$radius")
    val x1 = centerX - radius
    val x2 = centerX + radius
    val y1 = centerY - radius
    val y2 = centerY + radius
    val countriesOccurrences = mutableMapOf<String, Int>()

    // Horizontal parts of the rectangle.
    for (x in x1..x2) {
        var y = y1
        while (y <= y2) {
            // y1 then y2 then end the loop.
            //System.out.println("vertical, radius=" + radius + " x=" + x + " y=" +y);
            val country = countryFromPixel(x, y, wikidataOrNot, map)
            if (country != null) {
                var occurrences = 0
                if (countriesOccurrences.containsKey(country)) {
                    occurrences = countriesOccurrences[country]!!
                }
                countriesOccurrences[country] = occurrences
            }
            y += y2 - y1
        }
    }
    // Vertical parts of the rectangle, excluding corners.
    for (y in y1 + 1..y2 - 1) {
        var x = x1
        while (x <= x2) {
            // x1 then x2 then end the loop.
            //System.out.println("horizontal, radius=" + radius + " x=" + x + " y=" +y);
            val country = countryFromPixel(x, y, wikidataOrNot, map)
            if (country != null) {
                var occurrences = 0
                if (countriesOccurrences.containsKey(country)) {
                    occurrences = countriesOccurrences[country]!!
                }
                countriesOccurrences[country] = occurrences
            }
            x += x2 - x1
        }
    }

    // None of the searched pixels contained a country.
    if (countriesOccurrences.isEmpty()) {
        return null
    }

    // Return the country with most pixels.
    return countriesOccurrences.maxBy { it.value }.key
}

/**
 * Finds the country under a given pixel.
 */
internal fun countryFromPixel(x: Int, y: Int, wikidataOrNot: Boolean, map: BufferedImage?): String? =
    countryFromGrayshade(map?.raster?.getSample(x, y, 0), wikidataOrNot)

/**
 * Finds the country represented by a given gray shade.
 * The shades are stored in a CSV file.
 */
internal fun countryFromGrayshade(wantedGrayshade: Int?, wikidataOrNot: Boolean): String? {
    val countriesByGrayshade = mutableMapOf<Int, String>() // Key: Gray value from 0 to 255. Value: Country.
    try {
        // Load it within this method and do not cache it, in order to allow garbage collection between each call, because we consider that low memory usage is more important than speed. This method is called only once per country(...) call.
        val br = BufferedReader(InputStreamReader(resourceStream("/countries.csv")))
        var line = br.readLine()
        while (line != null) {
            val parts = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val grayshade = parts[0].toInt()
            val country = if (wikidataOrNot) parts[2] else parts[1]
            countriesByGrayshade[grayshade] = country
            line = br.readLine()
        }
        br.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return countriesByGrayshade[wantedGrayshade]
}

private object Resources

private fun resourceStream(name: String): InputStream? = Resources.javaClass.getResourceAsStream(name)
