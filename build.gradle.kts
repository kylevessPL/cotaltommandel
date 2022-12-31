import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "pl.piasta"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.controls")
}

dependencies {
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation("wtf.metio.storage-units:storage-units-model:2022.12.13")
    implementation("com.devskiller.friendly-id:friendly-id:1.1.0")
}

application {
    mainClass.set("pl.piasta.cotaltommandel.CotalTommandelKt")
}
