plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidLibrary {
        namespace = "net.thetrues.languagecards.shared"
        compileSdk = 36
        minSdk = 35
        @Suppress("UnusedPrivateMember")
        androidResources.enable = true
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
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

compose.resources {
    packageOfResClass = "net.thetrues.languagecards.shared.generated.resources"
}
