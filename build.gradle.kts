import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    val kotlinVersion = libs.versions.kotlin
    kotlin("jvm") version kotlinVersion
    application
    kotlin("plugin.serialization") version kotlinVersion

    alias(libs.plugins.ktlint)
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlinSerialization)
    implementation(libs.kotlinCoroutines)
    implementation(libs.apacheCommonsCLI)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
