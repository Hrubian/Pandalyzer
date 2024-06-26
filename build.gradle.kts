import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    val kotlinVersion = libs.versions.kotlin

    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    alias(libs.plugins.ktlint)
}

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
