plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    kotlin("jvm") version "1.9.21"
}

group = "io.github.coordinates2country"
version = "1.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}