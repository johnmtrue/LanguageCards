package net.thetrues.languagecards

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.thetrues.languagecards.platform.IosStatsStore
import net.thetrues.languagecards.repository.StatsRepository
import net.thetrues.languagecards.ui.App
import kotlin.system.exitProcess
import platform.UIKit.UIViewController

/**
 * iOS entry point: returns a UIViewController that hosts the shared Compose UI.
 * Swift/SwiftUI should call this (e.g. via Shared_iosKt.MainViewController()) and present it.
 * Uses [IosStatsStore] with main-thread scope and exits the process when the user taps Exit.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val statsStore: StatsRepository = IosStatsStore(scope)
    App(
        statsStore = statsStore,
        onExit = { exitProcess(0) },
        authorName = "John True",
    )
}
