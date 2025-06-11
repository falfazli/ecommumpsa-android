// Top-level build file where you can add configuration options common to all sub-projects/modules.
/*plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
*/
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // NOTE: Do not place your application plugin here; it belongs in the app module build.gradle.kts
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.android.application") version "8.10.1" apply false
    id("com.android.library") version "8.10.1" apply false
}

allprojects {
    // Optionally configure repositories here if not using settings.gradle.kts
    // repositories { ... }
}