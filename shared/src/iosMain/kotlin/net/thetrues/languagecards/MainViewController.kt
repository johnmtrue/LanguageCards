package net.thetrues.languagecards

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import net.thetrues.languagecards.data.SqlDelightDeckRepository
import net.thetrues.languagecards.data.createDatabase
import net.thetrues.languagecards.platform.createImportDeckHandler
import net.thetrues.languagecards.platform.setHostViewController
import net.thetrues.languagecards.repository.SqlDelightStatsRepository
import net.thetrues.languagecards.ui.App
import kotlin.system.exitProcess
import platform.UIKit.UIViewController

/**
 * Registers the hosting UIViewController for the document picker.
 * Call from Swift's makeUIViewController and updateUIViewController.
 */
fun setHostViewControllerForImport(viewController: UIViewController) {
    setHostViewController(viewController)
}

/**
 * iOS entry point: returns a UIViewController that hosts the shared Compose UI.
 * Swift/SwiftUI should call this (e.g. via MainViewControllerKt.MainViewController()) and present it.
 * Exits the process when the user taps Exit.
 * Import deck uses UIDocumentPickerViewController; Swift must call setHostViewControllerForImport.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val database = remember { createDatabase(null) }
    val deckRepository = remember(database) { SqlDelightDeckRepository(database) }
    val statsStore = remember(database) { SqlDelightStatsRepository(database) }
    App(
        deckRepository = deckRepository,
        statsStore = statsStore,
        onExit = { exitProcess(0) },
        authorName = "John True",
        onRequestImportDeck = createImportDeckHandler(),
    )
}
