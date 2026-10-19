import UIKit
import AppMetricaCore
import AppMetricaCrashes
import ComposeApp

class PlatformDependencyImplementation: IosPlatformDependency {
    func openUrl(url: URL) {
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }

    func reportError(identifier: String, message: String) {
        let error = AppMetricaError(identifier: identifier, message: message, parameters: nil)
        AppMetricaCrashes.crashes().report(error: error, onFailure: nil)
    }

    func appMetricaDeviceId() -> String? {
        AppMetrica.deviceID
    }

    func trackScreen(screen: String) {
        AppMetrica.reportEvent(name: "screen_view", parameters: ["screen": screen], onFailure: nil)
    }

    func isAnalyticsEnabled() -> Bool {
        AppMetrica.isActivated
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
        // Before the UI: AppMetrica drops events, e.g. the first screen view, until it's activated
        let appmetricaApiKey = IosPlatformDependencyKt.getAppmetricaKey()
        if let configuration = AppMetricaConfiguration(apiKey: appmetricaApiKey) {
            // configuration.areLogsEnabled = true
            AppMetrica.activate(with: configuration)
        }

        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController()
            window.makeKeyAndVisible()
        }

        return true
    }
}
