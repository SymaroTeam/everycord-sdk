# Sample apps

Small apps that show a complete integration: configure the SDK with a test key, sign a user in, go live with a
destination, and watch the stream with the player and chat.

| Folder | Platform | What it shows |
|---|---|---|
| `ios/` | iOS 16 and later | an app and its broadcast upload extension (`EveryCordBroadcastHandler`), installed with Swift Package Manager |
| `android/` | Android 10 and later | an app using `ScreenCaptureContract` and `EveryCord.live.start`, installed with Gradle |

Each sample is added here with its platform's release (`android/` with Android 1.0.0). Until a folder is here, its
quickstart walks through the same steps: [iOS](../docs/quickstart-ios.md) and [Android](../docs/quickstart-android.md).

To run a sample, put your own test publishable key (`ecpk_test_...`) and, on iOS, your App Group and bundle IDs where
the sample's README says. Never put a secret key (`ecsk_...`) in an app.
