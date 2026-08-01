plugins {
    id("minestom.java-library")
    id("minestom.publishing")
}

dependencies {
    api(libs.adventure.api)
    implementation(libs.adventure.text.logger.slf4j)
    implementation(libs.slf4j)
}
