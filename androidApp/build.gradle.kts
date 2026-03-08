plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val copyDeckResources = tasks.register<Copy>("copyDeckResourcesToAssets") {
    from(project(":shared").file("src/commonMain/composeResources/files"))
    into(file("build/generated/compose-deck-assets/composeResources/net.thetrues.languagecards.shared.generated.resources/files"))
    include("*.deck.json")
}

android {
    namespace = "net.thetrues.languagecards"
    compileSdk = 36

    // Workaround: copy shared compose file resources into app assets so they're
    // available at runtime (fixes MissingResourceException when adding decks)
    sourceSets["main"].assets.srcDirs("build/generated/compose-deck-assets")

    defaultConfig {
        applicationId = "net.thetrues.languagecards"
        minSdk = 35
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.sqldelight.sqlite.driver)
    testImplementation(libs.sqlite.jdbc)
    testImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// TODO: Fix NDK install (see docs/GRADLE_10_READINESS.md) and remove this block.
afterEvaluate {
    tasks.findByName("stripDebugDebugSymbols")?.enabled = false
    listOf("mergeDebugAssets", "mergeReleaseAssets").forEach { taskName ->
        tasks.findByName(taskName)?.dependsOn(copyDeckResources)
    }
}
