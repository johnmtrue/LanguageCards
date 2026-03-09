package net.thetrues.languagecards.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.Foundation.dataWithContentsOfURL
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
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }
}
