package net.thetrues.languagecards.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.UniformTypeIdentifiers.UTTypeItem

/**
 * Holds a reference to the hosting UIViewController for presenting the document picker.
 * Set by Swift's updateUIViewController when the Compose view is embedded.
 */
object HostViewControllerHolder {
    var viewController: UIViewController? = null
}

/**
 * Call this from Swift's updateUIViewController to register the hosting view controller.
 */
fun setHostViewController(viewController: UIViewController) {
    HostViewControllerHolder.viewController = viewController
}

/**
 * Creates the Import deck handler for iOS. Uses HostViewControllerHolder.viewController
 * to present UIDocumentPickerViewController. Must be called after setHostViewController.
 */
fun createImportDeckHandler(): ((String) -> Unit) -> Unit = { callback ->
    val vc = HostViewControllerHolder.viewController
    if (vc != null) {
        presentDocumentPicker(from = vc, onResult = callback)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentDocumentPicker(
    from: UIViewController,
    onResult: (String) -> Unit,
) {
    val types = listOf(UTTypeItem)
    val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = types,
        asCopy = true,
    )
    val delegate = DocumentPickerDelegate(onResult)
    picker.delegate = delegate
    from.presentViewController(picker, animated = true) {}
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class DocumentPickerDelegate(
    private val onResult: (String) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        controller.dismissViewControllerAnimated(true) {}
        val url = didPickDocumentsAtURLs.firstOrNull() ?: return
        val nsUrl = url as? platform.Foundation.NSURL ?: return
        val content = readFileContent(nsUrl)
        if (content != null) {
            onResult(content)
        }
    }

    private fun readFileContent(url: platform.Foundation.NSURL): String? {
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        val size = data.length.toInt()
        if (size <= 0) return ""
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes.decodeToString()
    }
}
