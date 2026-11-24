# Myanmar KeyMap 0.1.0
Experimental hardware-only IME, classic Myanmar3 mapping (not every Pyidaungsu or SIL variant). No network permission, no logging, no storage of typed text. US QWERTY recommended.

## Build
Install Android Studio, Android SDK Platform 35 and JDK 17. Open this folder. This source archive does not bundle a Gradle wrapper JAR or APK. Install Gradle 8.9 from https://gradle.org/releases/ and run `gradle wrapper --gradle-version 8.9` from the project root, then sync in Android Studio. Run `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`). APK: app/build/outputs/apk/debug/app-debug.apk. Alternatively generate a debug APK from Android Studio after syncing. Full Android build not verified in the authoring environment.

## Pixel installation
Enable USB debugging and use Android Studio Run or `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Alternatively transfer APK and authorize installation from the chosen file manager. Open Myanmar KeyMap, enable it and select it using the two buttons. Connect a Bluetooth/USB keyboard, set physical layout to English (US), and test in the text field. Ctrl+Space toggles Myanmar/English if Android does not intercept it. Use system input picker to return to Gboard. This IME has no touch keyboard; use Gboard when hardware is disconnected.

## Tests
A=ေ, U=က, S=ျ, J=ြ, Shift+G=ွ, Shift+F=္. Lowercase in examples means unshifted, uppercase means Shift.
- au → ကေ
- ausmif; → ကျောင်း
- aujG; → ကြွေး
- rjdkh / rjkdh → မြို့
- puFul → စက္ကူ
- oifFabm → သင်္ဘော
- 1230 → ၁၂၃၀
- Backspace after au returns pending ေ; backspace again clears it.

Run pure Kotlin test (requires Kotlin compiler): `kotlinc app/src/main/java/com/example/myanmarkeymap/{MyanmarLayout,UnicodeEngine}.kt tests/EngineTest.kt -include-runtime -d engine-tests.jar` then `java -jar engine-tests.jar`. All listed engine tests passed during creation. Android lifecycle, app compatibility, Ctrl+Space, cursor editing and APK installation still require real device testing.

## Limits
Pre-vowel is always treated as visual-order input. Isolated pending vowel is shown as a combining mark and may remain on commit. Basic mark ordering and stacks only; full orthography, duplicates, unusual sequences, editing committed clusters, alternative keyboard variants, and app-specific composing behavior are not implemented or verified. Password/email/URI/numeric fields use native pass-through. No touchscreen input. System shortcut may intercept Ctrl+Space. Switch IME via system picker if needed.

References: https://victorskl.github.io/m3kb/web/kbd.html (classic mapping); https://help.keyman.com/keyboard/sil_myanmar_my3/1.7.4/sil_myanmar_my3 (typing examples); https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method ; https://developer.android.com/build/releases/agp-8-7-0-release-notes
