import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

/// Wraps the shared Compose UI (MainViewController) so it can be used in SwiftUI.
/// Kotlin/Native exports top-level functions from MainViewController.kt as MainViewControllerKt.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let vc = MainViewControllerKt.MainViewController()
        MainViewControllerKt.setHostViewControllerForImport(viewController: vc)
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        MainViewControllerKt.setHostViewControllerForImport(viewController: uiViewController)
    }
}

#Preview {
    ContentView()
}
