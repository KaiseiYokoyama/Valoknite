plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.0"
}

group = "me.kyokoyama"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.1")
    implementation("org.jsoup:jsoup:1.14.2")
}