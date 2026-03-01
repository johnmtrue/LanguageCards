plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "net.thetrues.languagecards.shared"
        compileSdk = 36
        minSdk = 35
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
        }
        androidMain.dependencies {
            api(libs.androidx.datastore.preferences)
        }
        iosMain.dependencies {
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
