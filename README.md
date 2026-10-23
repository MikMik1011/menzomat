# Menzomat

Kotlin Multiplatform app (Compose Multiplatform, Material 3 Expressive, ISIC colors) that reads a student canteen card over NFC and shows meal balance, cash balance, validity and the rest of what is on it. It runs on Android today; the shared module is set up so an iOS app can be added later.

Only the **University of Novi Sad** card is supported so far, but one app is meant to hold every university: a card is read once into a generic `CardDump`, and any *profile* can then decode that dump. The profile is auto-detected and can be switched in the app at any time without tapping the card again.

## Build

```sh
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :shared:testAndroidHostTest   # unit tests (81)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :androidApp:installDebug      # install on a connected phone (needs NFC)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :shared:compileCommonMainKotlinMetadata   # proves commonMain uses only common APIs
```

Needs the Android SDK (`local.properties` → `sdk.dir`), JDK 17, and a phone with NFC; emulators have no NFC. AGP is pinned to 9.3.0 to match Android Studio. The Compose Multiplatform Material3 is an alpha because the stable one lacks the Expressive APIs.

## Structure

```
shared/                         Kotlin Multiplatform library
  src/commonMain/kotlin/st/misa/menzomat/
    MenzomatApp.kt              the whole UI entry point; an app passes in PlatformServices
    card/                       the one feature: read a card and show it
      domain/   model/ nfc/ profile/ repository/    what the app is about; interfaces and models
      data/     local/ preferences/ profile/<university>/   DataStore storage, card layouts, DefaultCardRepository
      presentation/card/        CardScreen, CardViewModel, CardUiState, components/
      presentation/settings/    SettingsScreen, SettingsViewModel
    core/                       generic code the feature builds on
      domain/desfire/  Aid, CardDump, DesfireClient, Transceiver, DesfireError
      domain/util/     ByteCursor, date helpers, Windows-1250 table
      data/desfire/    IsoDesfireClient, CardDumper
    di/                         Koin: sharedModule
    ui/                         theme/ (ISIC brand scheme), components/, platform/ (what each platform provides)
  src/commonMain/composeResources/   strings (Serbian Latin default, values-en)
  src/commonTest/                    unit tests; fakes in the `fake` package
  src/androidMain/                   AndroidNfcReader, Android platform services, Koin androidPlatformModule
  src/androidHostTest/               tests that need the JVM (Windows-1250 table vs the real charset)
androidApp/                     MainActivity, MenzomatApplication (starts Koin), manifest, launcher icon, UI tests
```

Dependencies point inwards: `presentation → domain ← data`, and everything may use `core` and `ui`. Nothing in `domain` or `core/domain` may import from `data` or `presentation`.

Everything in `commonMain` is plain common Kotlin. What genuinely differs per platform is behind small interfaces (one per capability) that each app provides:

| Interface | Android binding |
|---|---|
| `NfcReader` / `NfcTag` | `AndroidNfcReader` (reader mode while the activity is in the foreground) |
| `ShareSheet`, `TextClipboard`, `NfcSettings` | `androidPlatformServices(activity)` |
| `DateFormatter`, `ThemePlatform` | same file; locale dates, wallpaper colors, reduced motion |
| `PreferencesPath`, IO dispatcher | `androidPlatformModule` |

## UI copy

Labels only: no taglines, helper text or secondary descriptions. If a screen needs them to make sense, change the layout (alignment, icons, grouping, dimming) instead. A value that sits next to a label does not need a title over the group, and a state that has an icon and a title does not need a sentence. Anything that is only an icon or a number still gets a content description, so screen readers (and UI-driving agents) read the sentence that sighted users infer. Two kept on purpose: the personal-data warning next to Share/Copy, and the detail in "Error talking to the card".

## Adding a university

1. Add `card/data/profile/<name>/<Name>Profile.kt` in `shared/src/commonMain` (copy `novisad/`): an `internal class … : CardProfile`, and keep the file ids and sizes in a `<Name>Layout` object next to it.
   - `score(dump)` says how much a dump looks like this university's card (0 = not at all).
   - `decode(dump)` fills a `StudentCard`; it must never throw. Put anything university-specific into `extras`.
2. Add it to the profile list in `di/SharedModule.kt`.
3. Test it against dumps of real cards: the app's "Raw data" section exports a dump as JSON. Put anonymized fixtures next to the test, like `novisad/Fixtures.kt`.

## Adding iOS later

The iOS targets (`iosArm64`, `iosSimulatorArm64`) are already declared in `shared/build.gradle.kts` and are only built on macOS (`kotlin.native.ignoreDisabledTargets=true` skips them elsewhere). Nothing iOS-specific exists yet. To add the app:

1. In `shared/src/iosMain`: an `IosNfcReader` (Core NFC `NFCTagReaderSession`; iOS scans only on request, so `NfcReader` will probably need a way to start a scan from a button), an `iosPlatformModule` (IO dispatcher, `PreferencesPath`, the reader), and iOS versions of `ShareSheet`, `TextClipboard`, `NfcSettings`, `DateFormatter` and `ThemePlatform`.
2. A `MainViewController()` that starts Koin and returns `ComposeUIViewController { MenzomatApp(platform) }`.
3. An `iosApp/` Xcode project: a SwiftUI shell hosting that controller, an `Info.plist` with `NFCReaderUsageDescription`, and the NFC tag-reading entitlement.
4. Check on a real iPhone that Core NFC can send the DESFire commands this app uses (list applications, select, list files, read data) before building more.

## Card format (Novi Sad)

DESFire application `0x0D0000` (as shown by TagInfo; `0D 00 00` on the wire), files `0x00`–`0x0A`, all readable without a key. That includes the value file `0x03`, the transaction counter, even though TagInfo lists its read key as blocked.
The byte layout is documented in code in `card/data/profile/novisad/NoviSadProfile.kt` (file ids in `NoviSadLayout.kt`) and was derived from TagInfo dumps of real cards. Terminals before 2026 stored months of the last transaction (and of the card number validity) 0-based, newer ones store them 1-based; the profile resolves this per card.
