# Performance measurements

Measurements are captured on a Toshiba 58UA2263DT (Android 14, 4K) with a release-signed APK and the default solid `#0F172A` background.

| Metric | OZLauncher v1.0.0 |
|---|---:|
| APK | 82,847 bytes |
| Cold start after install/update | 959 ms |
| Warm activity delivery | 0 ms |
| Idle CPU sample | 0.0% |
| Idle PSS | 50,871 KB |
| Graphics PSS | 8,208 KB |

The first design target of 35 MB PSS was not met on this Android 14 television. Android framework and graphics mappings account for most of the measured PSS. The app reduced its initial visible view count from 92 to 38 by deferring the hidden app drawer, lowering PSS from 55,459 KB to 50,871 KB. This document will be updated as device-level improvements land.
