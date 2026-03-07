// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Fix NDK missing source.properties (corrupted/incomplete install)
file("local.properties").takeIf { it.exists() }?.let { localProps ->
    java.util.Properties().apply { localProps.reader().use { load(it) } }
        .getProperty("sdk.dir")?.replace("\\\\", "\\")?.let { sdkPath ->
            java.io.File(sdkPath, "ndk").takeIf { it.exists() }?.listFiles()
                ?.filter { it.isDirectory }
                ?.forEach { versionDir ->
                    java.io.File(versionDir, "source.properties").takeIf { !it.exists() }?.let { propsFile ->
                        propsFile.writeText("Pkg.Desc = Android NDK\nPkg.Revision = ${versionDir.name}\n")
                        println("Created missing source.properties in NDK ${versionDir.name}")
                    }
                }
        }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}