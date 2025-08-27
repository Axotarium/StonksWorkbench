plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "fr.qg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    implementation("com.github.Qg9:StonksMenu:27")
    implementation("com.github.Qg9:Drink:1")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

kotlin {
    jvmToolchain(21)
}