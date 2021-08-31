buildscript {
    repositories {
        google()
        mavenLocal()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
    }
}

repositories {
    google()
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://s3.amazonaws.com/mirego-maven/public")
    }
}
