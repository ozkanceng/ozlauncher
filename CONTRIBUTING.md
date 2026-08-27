# Contributing

Contributions are welcome under Apache License 2.0.

1. Keep runtime dependencies at zero unless a proposal proves a measurable net benefit.
2. Do not add networking, ads, analytics, telemetry or accessibility-service launcher hijacking.
3. Preserve D-pad, TalkBack, RTL and API 26 compatibility.
4. Run `./gradlew testDebugUnitTest lintDebug assembleRelease` before opening a pull request.
5. Include tests for collection, navigation, persistence or archive-format changes.

Translations live in `app/src/main/res/values-*/strings.xml`. Native-speaker corrections are especially welcome.
