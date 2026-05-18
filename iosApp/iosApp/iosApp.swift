import UIKit
import AppMetricaCore
import ComposeApp

class PlatformDependencyImplementation: IosPlatformDependency {
    func openUrl(url: URL) {
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }
}

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    override init() {
        let dependency = PlatformDependencyImplementation()
        IosPlatformDependencyKt.setInstance(new: dependency)
    }

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController()
            window.makeKeyAndVisible()
        }

        let appmetricaApiKey = IosPlatformDependencyKt.getAppmetricaKey()
        if let configuration = AppMetricaConfiguration(apiKey: appmetricaApiKey) {
            // configuration.areLogsEnabled = true
            AppMetrica.activate(with: configuration)
        }
        
        return true
    }
}
