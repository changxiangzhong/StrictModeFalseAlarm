package no.telia.strictmodefalsealarm

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
    }

    override fun onResume() {
        super.onResume()
        val beforeOp = SystemClock.uptimeMillis()
        getSharedPreferences("Pref1", Context.MODE_PRIVATE)
        val timeConsumed = SystemClock.uptimeMillis() - beforeOp
        Log.d("StrictMode", "Real time consumption by getSharedPreferences = $timeConsumed")
        Thread.sleep(1000) // To simulate a heavy workload on main thread.
    }
}

/*
The following stack trace indicates that AndroidBlockGuardPolicy is complaining about the File OP is taking too much time (1029ms). But in reality
it is not the File OP. It's the Thread.sleep() caused this delay. The File OP only takes 2 ms.

2022-08-30 12:50:02.279 29534-29534/no.telia.strictmodefalsealarm D/StrictMode: Real time consumption by getSharedPreferences = 2
2022-08-30 12:50:03.309 29534-29534/no.telia.strictmodefalsealarm D/StrictMode: StrictMode policy violation; ~duration=1029 ms: android.os.strictmode.DiskReadViolation
        at android.os.StrictMode$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1596)
        at libcore.io.BlockGuardOs.access(BlockGuardOs.java:71)
        at libcore.io.ForwardingOs.access(ForwardingOs.java:72)
        at android.app.ActivityThread$AndroidOs.access(ActivityThread.java:7533)
        at java.io.UnixFileSystem.checkAccess(UnixFileSystem.java:281)
        at java.io.File.exists(File.java:815)
        at android.app.ContextImpl.getDataDir(ContextImpl.java:2539)
        at android.app.ContextImpl.getPreferencesDir(ContextImpl.java:626)
        at android.app.ContextImpl.getSharedPreferencesPath(ContextImpl.java:853)
        at android.app.ContextImpl.getSharedPreferences(ContextImpl.java:475)
        at android.content.ContextWrapper.getSharedPreferences(ContextWrapper.java:188)
        at android.content.ContextWrapper.getSharedPreferences(ContextWrapper.java:188)
        at no.telia.strictmodefalsealarm.MainActivity.onResume(MainActivity.kt:20)
        at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1456)
        at android.app.Activity.performResume(Activity.java:8135)
        at android.app.ActivityThread.performResumeActivity(ActivityThread.java:4434)
        at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:4476)
        at android.app.servertransaction.ResumeActivityItem.execute(ResumeActivityItem.java:52)
        at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:176)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2066)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:223)
        at android.app.ActivityThread.main(ActivityThread.java:7656)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:592)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:947)


Source code:
        - StrictMode::startHandlingViolationException() - startTime marked
        - StrictMode::handleViolationWithTimingAttempt() - schedule a new Runnable() and get the endTime. 
        https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/os/StrictMode.java
*/