# Menzomat

Read your student canteen card with your phone. Hold the card against the back of an NFC phone and see how many breakfasts, lunches and dinners you have left, your cash balance, your last transaction, when the card was issued and the rest of what is stored on it.

It runs on Android today. It is built with Kotlin Multiplatform and Compose Multiplatform (Material 3 Expressive, in the ISIC brand colors), so an iOS app can follow. Only the **University of Novi Sad** card is supported so far, but the app is built so other universities can be added.

## What it shows

- Meals left this month, how many can still be bought, and meals already bought for next month
- Cash balance and the last transaction
- Name, faculty, year of study, index number, card number, and the issue and expiry dates
- A few extras, such as the last medical check-up and the transaction counter
- The raw data exactly as read from the card, to copy or share (useful for adding a new kind of card)

Serbian and English, light and dark theme, and optionally the colors of your wallpaper.

## How it works

1. **One tap reads everything.** The card is a MIFARE DESFire EV3, and the canteen data on it is readable without a key. The phone reads every application and file the card allows into a generic dump.
2. **A profile decodes it.** Each university has a *profile* that knows how that university lays out its card. A profile scores a dump (does this look like my card?) and decodes it into meals, balances and dates. The app picks the best match by itself, and you can switch the profile in the app at any time. The saved dump is decoded again, so you do not have to tap the card again.
3. **The screen shows the result.** The last card you read is kept on the phone, so you can look at it without the card.

## The card

How the Novi Sad card is laid out, and how it was reverse engineered, is written up in this gist: **[Reverse engineered UNS ISIC cards data](https://gist.github.com/MikMik1011/0a923f203c2ce98c4b5bf1537de42fe7)**.

The short version: DESFire application `0x0D0000` with files `0x00` to `0x0A`, all free to read. Meals, balances and dates are plain binary and one terminal-software quirk (months counted from 0 on older terminals, from 1 on newer) is handled per card.

File `0x00` also holds the holder's national id (JMBG). The app deliberately leaves it out and never decodes or shows it, though it is still in the raw data, which is the card exactly as read.

## Privacy

Nothing leaves your phone. The app asks for the NFC permission and nothing else, so it cannot reach the internet. The last card read is stored on the phone only and is left out of backups. Sharing or copying the raw data is something you do yourself.

## Build and run

You need JDK 17, the Android SDK (`local.properties` with `sdk.dir`), and a phone with NFC. Emulators have no NFC.

```sh
./gradlew :shared:testAndroidHostTest      # unit tests
./gradlew :androidApp:installDebug         # install on a connected phone
./gradlew :androidApp:installBenchmark     # release build signed with the debug key
```

Judge scrolling and animation on the benchmark build: debug builds run Compose several times slower.

## Adding a university

A university is one profile class and one line in the dependency setup; the app then detects it and offers it in the profile picker. [CLAUDE.md](CLAUDE.md) has the checklist, the project layout and the conventions.

## License

[GNU AGPL-3.0](LICENSE)
