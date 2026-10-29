# Menzomat: guide for agents

Kotlin Multiplatform app (Compose Multiplatform, Material 3 Expressive) that reads a MIFARE DESFire EV3 student canteen card over NFC. Android only for now, iOS targets declared. One app for every university: a card is read once into a generic `CardDump`, and a per-university `CardProfile` decodes it. Only Novi Sad exists. Human-facing overview: [README.md](README.md). The card was reverse engineered in this gist (read it with `gh gist view 0a923f203c2ce98c4b5bf1537de42fe7`): https://gist.github.com/MikMik1011/0a923f203c2ce98c4b5bf1537de42fe7

Package and application id: `st.misa.menzomat`.

## Commands

Gradle needs JDK 17 (set `JAVA_HOME` if it is not the default).

| Task | Command |
|---|---|
| Unit tests | `./gradlew :shared:testAndroidHostTest` (one class: `--tests '*ClassName*'`) |
| Compile the UI tests | `./gradlew :androidApp:assembleDebugAndroidTest` |
| Check commonMain uses only common APIs | `./gradlew :shared:compileCommonMainKotlinMetadata` |
| Install, debug | `./gradlew :androidApp:installDebug` |
| Install, release-like (for judging smoothness) | `./gradlew :androidApp:installBenchmark` |

Before saying a change is done: unit tests pass, the UI tests compile, and for UI work look at it on the phone.

## Working environment

- The test device is a real phone over adb. There is no emulator (no NFC). Check `adb devices` first. If the phone is locked, ask the user to unlock it.
- Never run `connectedAndroidTest` on a phone that holds a card you want to keep: it uninstalls the app and wipes its stored data. The UI tests are compiled, not run.
- Debug builds are several times slower than release. Measure scrolling on `installBenchmark` (`adb shell dumpsys gfxinfo st.misa.menzomat reset`, swipe, then dump again).
- Run Gradle one build at a time, and not next to a busy Android Studio: two together can run out of memory (exit 137).
- Commit and push only when asked. Conventional commit prefixes (`feat`, `fix`, `refactor`, `build`, `chore`, optional scope), one logical change per commit.

## Layout

```
shared/                              KMP library: nearly everything
  src/commonMain/kotlin/st/misa/menzomat/
    MenzomatApp.kt                   UI entry point; an app passes in PlatformServices
    card/                            the one feature
      domain/    model/ nfc/ profile/ repository/      interfaces and models, no framework
      data/      DefaultCardRepository, local/ (last dump), preferences/ (settings), profile/<university>/
      presentation/card/             CardScreen, CardViewModel, CardUiState, CardActions, components/
      presentation/settings/         SettingsScreen, SettingsViewModel
    core/                            generic DESFire code
      domain/desfire/                Aid, CardDump, DesfireClient, Transceiver, DesfireError
      domain/util/                   ByteCursor, dates, Hex, Windows-1250
      data/desfire/                  IsoDesfireClient (APDU framing), CardDumper
    di/SharedModule.kt               Koin module and the profile list
    ui/theme/ ui/components/ ui/platform/
  src/commonMain/composeResources/   values (Serbian Latin, default) and values-en
  src/androidMain/                   AndroidNfcReader, IsoDepConnection, Android platform services, androidPlatformModule
  src/commonTest/                    unit tests; fakes in fake/
  src/androidHostTest/               JVM-only tests
androidApp/                          MainActivity, MenzomatApplication (starts Koin), manifest; src/androidTest has the UI tests
```

Dependencies point inwards: `presentation → domain ← data`; anything may use `core` and `ui`. `domain` and `core/domain` never import `data` or `presentation`. Keep implementation classes `internal` (concrete profiles, repositories); only interfaces and screens are public.

Platform differences sit behind small interfaces, provided once by each app through `PlatformServices` (`ui/platform/PlatformServices.kt`): `NfcReader`/`NfcTag` (domain/nfc), `ShareSheet`, `TextClipboard`, `FileSaver`, `NfcSettings`, `DateFormatter`, `ThemePlatform`, `AppInfo`, plus `PreferencesPath` and the `named("io")` dispatcher in the platform Koin module. To add a capability: interface in `PlatformServices.kt`, Android binding in `androidPlatformServices`, and the fake in `androidApp/src/androidTest/.../TestPlatform.kt`.

## How a read flows

`AndroidNfcReader.tags` → `DefaultCardRepository.scans` (`transformLatest`, so a new tag cancels the read in progress) → `CardDumper` over `IsoDesfireClient` → `CardDump`, stored by `LastDumpStore`. Platform exceptions become `CardReadError` at the repository, so nothing Android-specific reaches a ViewModel. `CardViewModel` combines NFC status, the stored dump, the profile selection, `isReading` and `error`, and decodes through `ProfileResolver` (auto picks the best `score`, manual forces a profile, a forced profile with score 0 is `isLowConfidence`). A forced profile that another profile beats gets `betterMatch`, which shows `BetterMatchDialog` (Switch goes back to Auto; Keep is remembered per card uid until the app restarts). The profile is changed in Settings, or from the picker on an unrecognized card. Changing the profile re-decodes the stored dump; no new tap.

`IsoDepConnection.kt` (`withIsoDep`): one lock for all NFC I/O, and if Android still marks a `Tag` as connected (`Only one TagTechnology can be connected at a time`) the connection is used as it is. Do not replace it with a plain `connect()`.

## The card (Novi Sad)

AID `0D 00 00` on the wire (the profile accepts `0x00000D` and `0x0D0000`), files `0x00`-`0x0A`, all readable without a key, including value file `0x03`. The byte layout of every file is in `NoviSadLayout.kt` and `NoviSadProfile.kt`, and in the gist with the reverse-engineering notes; it is not repeated here. In short the card holds: the holder's name (and national id), the card number and its validity date, the monthly meal balances, the transaction counter (a value file), the last transaction, the issue and validation dates, faculty, study year and index, the cash balance and the medical check date.

Rules that came from real cards and from the user:
- Months in files 0x01 and 0x04 are 0-based on old terminal software and 1-based on newer; 0x02, 0x06, 0x09 are always 1-based. `resolveTransactionDate` and `resolveCardDate` decide per card.
- The validated-until date (0x06) exists only once the card is renewed. The app does not show it; its presence adds 1 to the study year stored in 0x07.
- The holder's national id (JMBG) is deliberately never decoded, shown or masked. Do not add it back. It stays in the raw data, which is the card as read.
- Faculty ids 1, 2 and 4 are known (Technical Sciences, Philosophy, Sciences) and listed in `NoviSadLayout.facultyKeys`; any other id shows "Faculty N". A new faculty needs a key there and its name in both string files (`FacultyNamesTest` checks).

## Conventions

- **UI copy: labels only.** No taglines, helper text or secondary descriptions; fix the layout instead (alignment, icons, grouping, dimming). Icon-only or number-only elements keep a content description. Kept on purpose: the personal-data warning next to Share/Copy and the detail in "Error talking to the card".
- **Text the user reads lives in the string resources**, in both languages. Models carry data and keys (`ExtraField.key`, `WarningReason`, `Faculty.id`), the UI words them. `StringResourcesParityTest` fails if the two files drift (names and placeholders).
- **Colors only in `ui/theme/Color.kt`** (ISIC palette: Light Sea Green `#40B8B8` is the hero, brand Black `#120B29` is the text on it). No raw colors or hard-coded corner shapes in components: use `MaterialTheme` roles and shapes, and `MaterialTheme.spacing` for gaps. `ContrastTest` checks the pairs.
- **Tests first** for logic. Profiles are tested against fixtures built from made-up values (`novisad/Fixtures.kt`); never commit real card data.
- **Test names** in `commonTest` are backticked plain-word sentences: no punctuation at all (Kotlin/Native rejects `. , : ; ( ) / \ < > [ ]` and more).
- Compose Multiplatform resources only substitute `%1$s` and `%1$d`: pass formatted values as arguments, never `%02X` or `%%`. Accessors are imported one by one from `st.misa.menzomat.shared.resources`.
- `Dispatchers.IO` is not common: each platform module provides `named("io")`. Copying to the clipboard goes through `TextClipboard`; saving a file goes through `FileSaver`, whose Android binding must be created in `onCreate` (it registers a result launcher) and whose result must not be awaited inside `repeatOnLifecycle`, because the save dialog stops the activity.
- Koin: bind lists with the type spelled out (`get<List<CardProfile>>()`); `SharedModuleTest` resolves the whole graph.
- Tests touching DataStore use real files, so wait in wall-clock time, not virtual time (see `DefaultCardRepositoryTest`).
- Versions are pinned in `gradle/libs.versions.toml`. AGP stays at 9.3.0 (Android Studio's maximum). Material3 is an alpha because the stable release lacks the Expressive APIs.
- The launcher icon has two drawables on purpose: `ic_launcher_foreground.xml` (dark card, light waves) and `ic_launcher_monochrome.xml` for themed icons. The system tints the monochrome layer as one flat shape, so the waves there are cut out of the card; reusing the colored drawable makes them vanish.
- There is no `iosMain` yet, and `kotlin.native.ignoreDisabledTargets=true` skips the Apple targets off macOS.

## Adding a university

1. `card/data/profile/<name>/<Name>Profile.kt` in `shared/src/commonMain` (copy `novisad/`): an `internal class … : CardProfile` with a `<Name>Layout` object for file ids and sizes. `score(dump)`: how much a dump looks like this card (0 means not at all, 100 is certain). `decode(dump)`: fills a `StudentCard`, never throws; anything specific goes into `extras` (give each a stable key and a label string) and unreadable fields become `DecodeWarning`s.
2. Add it to the profile list in `di/SharedModule.kt`. Detection and the picker pick it up.
3. Test it with fixtures from real dumps (the app's Raw data section exports JSON), with personal data replaced. Add the extras' labels in both languages (`ExtraLabelsTest` shows the pattern).

## Adding iOS later

Targets `iosArm64` and `iosSimulatorArm64` are declared. Needed: an `IosNfcReader` (Core NFC `NFCTagReaderSession`; iOS scans only on request, so `NfcReader` will need a way to start a scan from a button), an `iosPlatformModule` (IO dispatcher, `PreferencesPath`, the reader), iOS versions of `ShareSheet`, `TextClipboard`, `FileSaver` (a document picker), `NfcSettings`, `DateFormatter`, `ThemePlatform`, `AppInfo`, a `MainViewController()` returning `ComposeUIViewController { MenzomatApp(platform) }`, and an `iosApp/` Xcode project with `NFCReaderUsageDescription` and the NFC tag-reading entitlement. First check on a real iPhone that Core NFC can send the DESFire commands used here.
