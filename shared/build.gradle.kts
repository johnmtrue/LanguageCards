plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
}

@Suppress("DEPRECATION")
kotlin {
    androidLibrary {
        namespace = "net.thetrues.languagecards.shared"
        compileSdk = 36
        minSdk = 35
        @Suppress("UnusedPrivateMember")
        androidResources.enable = true
    }

    listOf(
        iosArm64(),           // Physical devices
        iosSimulatorArm64(),  // Apple Silicon simulator
        iosX64(),             // Intel Mac simulator (x86_64)
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.sqldelight.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        findByName("androidUnitTest")?.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.sqlite.jdbc)
        }
        findByName("iosSimulatorArm64Test")?.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

sqldelight {
    databases {
        create("LanguageCardsDatabase") {
            packageName.set("net.thetrues.languagecards.db")
        }
    }
}

compose.resources {
    packageOfResClass = "net.thetrues.languagecards.shared.generated.resources"
}
