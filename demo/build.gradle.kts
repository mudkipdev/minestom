plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(rootProject)
    implementation("net.goldenstack:trove:3.0")

    runtimeOnly(libs.bundles.logback)
}

application {
    mainClass.set("net.minestom.demo.Main")
    mainModule.set("net.minestom.demo")

    applicationDefaultJvmArgs += "-ea"
}
