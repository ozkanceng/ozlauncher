# Performance measurements

Measurements were captured on a Toshiba 58UA2263DT running Android 14. The
physical panel is 3840×2160 and Android renders the launcher at its configured
1920×1080 override. Tests use the release-signed APK and the solid background.

## v1.0.1 result

| Metric | v1.0.0 | v1.0.1 | Change |
|---|---:|---:|---:|
| Published APK | 82,847 bytes | 75,504 bytes | −7,343 bytes (−8.9%) |
| Idle CPU sample | 0.0% | 0.0% | no regression |
| Settled idle PSS | 50,871 KB | 32,566 KB | −18,305 KB (−36.0%) |
| Graphics PSS | 8,208 KB | 9,312 KB | +1,104 KB |
| Visible views | 38 | 44 | +6 |
| Warm activity time | 0 ms | 0 ms (7 ms wait) | unchanged |

The v1.0.1 settled PSS value is the median of three samples taken ten seconds
apart: 32,979 KB, 32,566 KB and 32,562 KB. All three CPU samples were 0.0%.
The original ≤35 MB steady-state PSS target is therefore met on the target TV.

Memory is intentionally reported with its timing context. Immediately after a
forced process restart, v1.0.1 measured 44,189 KB PSS and fell to about 32.6 MB
as the process settled. A longer-idle observation reached 24,038 KB. Android can
reclaim or swap shared framework and graphics pages, so the lowest observation
is not used as the headline result.

Graphics PSS varied between 7,568 KB and 10,896 KB. The v1.0.1 row uses the
9,312 KB value measured alongside the three settled PSS samples. The modest
graphics increase is expected from the larger native Android TV banner artwork;
total PSS still fell by about 18 MB.

## Startup notes

v1.0.0 recorded a 959 ms cold start immediately after installation. A directly
comparable v1.0.1 cold start is not reported because OZLauncher is now the sole
enabled user HOME: Android restarts it immediately after `force-stop`, before
`am start -W` can begin timing. Clearing app data or changing HOME merely to
produce a number would disturb the accepted TV configuration. Warm delivery
continues to report 0 ms activity time (7 ms command wait in the recorded run).

## Why v1.0.1 is better

- The published APK is smaller despite the refined focus and banner treatment.
- Settled PSS dropped from 50,871 KB to 32,566 KB.
- Idle CPU remained at 0.0%.
- Native Android TV banners improved presentation without adding runtime
  dependencies, network access, blur passes or background services.
- BareLauncher, the debug build, FLauncher and Google TV Launcher are disabled,
  leaving only OZLauncher and Android's safety `FallbackHome` as HOME handlers.

## Historical optimization

The initial implementation measured 55,459 KB PSS. Deferring creation of the
hidden app drawer reduced that to 50,871 KB in v1.0.0. The v1.0.1 measurements
above supersede that result for the accepted Toshiba configuration.
