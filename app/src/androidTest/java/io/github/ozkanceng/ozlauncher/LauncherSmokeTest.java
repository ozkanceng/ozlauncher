package io.github.ozkanceng.ozlauncher;

import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class LauncherSmokeTest {
    @Test public void launcherCreatesAndReceivesFocus() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), LauncherActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LauncherActivity activity = (LauncherActivity) instrumentation.startActivitySync(intent);
        assertNotNull(activity);
        instrumentation.waitForIdleSync();
        assertFalse(activity.isFinishing());
        activity.finish();
    }
}
