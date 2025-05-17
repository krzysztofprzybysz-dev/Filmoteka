// Top-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Replace KAPT with KSP
    // alias(libs.plugins.kotlin.kapt) apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}