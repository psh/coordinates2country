@file:JvmName("Coordinates2Country")

package io.github.coordinates2country

/**
 * Converts coordinates (example: 50.1, 10.2) into a country name in English (example: "Germany").
 */
fun country(latitude: Double, longitude: Double): String? =
    country(latitude, longitude, false)

/**
 * Converts coordinates (example: 50.1, 10.2) into a the numerical part of a Wikidata QID identifier (example: 183, meaning http://www.wikidata.org/entity/Q183).
 */
fun countryQID(latitude: Double, longitude: Double): String? =
    country(latitude, longitude, true)

/**
 * Command line utility, mostly for testing purposes.
 *
 * Usage example:
 * java -cp build/libs/coordinates2country.jar io.github.coordinates2country.Coordinates2Country 50.1 10.2
 * Output: Germany
 */
fun main(args: Array<String>) {
    println(country(args[0].toDouble(), args[1].toDouble()))
}
