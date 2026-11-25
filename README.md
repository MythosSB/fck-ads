# F\*CK ads

Open-source Android apps and DIY alternatives for everyday tasks without ads or tracking.

## Principles

These principles apply to both Android apps and DIY projects.

- No ads, trackers, or analytics.
- No mandatory F\*CK ads accounts. Projects that connect to a third-party service use your own account with that service.
- Request only the permissions a project needs. If an app works offline, it does not request internet access.
- Your data stays under your control, on your device or in a service you choose.
- All code is open source under GPL-3.0.

## Apps

Android apps live in `apps/<app-name>/`.

| App | Description | Status |
| --- | --- | --- |
| [Control de Gastos](apps/control-gastos/) | Record expenses and review them by month and category. Everything is stored locally on your phone. | released 👍 |

## DIY & Alternatives

This section is for plugins, scripts, guides, and configurations that replace services with ads, tracking, or subscriptions when a standalone Android app isn't the right fit. Projects will live in `diy/<project-name>/`. Nothing has been published in this section yet.

| Project | Description | Status |
| --- | --- | --- |
| Obsidian Google Drive Sync | A planned Obsidian plugin to sync vaults with your own Google Drive on desktop and Android. | Coming soon |

## Build an app

The minimum supported version is Android 8.0 (API 26).

1. Install Android Studio.
2. Open the app's folder, such as `apps/control-gastos`.
3. Wait for Gradle sync to finish.
4. Select an Android device or emulator and click **Run**.

## Contributing

Open an issue before starting a large change so we can discuss the scope. Keep contributions aligned with the principles above.

## License

This repository is licensed under GPL-3.0. See [LICENSE](LICENSE) for the full terms.