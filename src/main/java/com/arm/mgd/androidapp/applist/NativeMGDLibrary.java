package com.arm.mgd.androidapp.applist;

import android.util.Log;
import com.arm.mgd.androidapp.features.FeatureAuthorisation;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NativeMGDLibrary {
    private static final Executor EXECUTOR;
    public static final int EXIT_CODE_INIT_ERROR = 1;
    public static final int EXIT_CODE_SUCCESS = 0;
    private static final NativeMGDLibrary INSTANCE;
    private static final String TAG = "MGD Native Library";
    private static NativeMGDLibrary.IAuthorisationCallback authorisationCallback;
    private static boolean isRunning;
    private static boolean lastStartedWithSendTraceDataToHost;

    static {
        System.loadLibrary("com.arm.mgd.mgddaemon");
        EXECUTOR = Executors.newSingleThreadExecutor();
        isRunning = false;
        lastStartedWithSendTraceDataToHost = false;
        INSTANCE = new NativeMGDLibrary();
        authorisationCallback = null;
    }

    public static void startDaemonService(final NativeMGDLibrary.IAuthorisationCallback var0, final NativeMGDLibrary.IExitCodeCallback var1, final boolean var2, final boolean var3, final Object var4) {
        EXECUTOR.execute(new Runnable() {
            public void run() {
                if (NativeMGDLibrary.isRunning && (NativeMGDLibrary.lastStartedWithSendTraceDataToHost != var2 || var3)) {
                    NativeMGDLibrary.INSTANCE.stopService();
                    NativeMGDLibrary.isRunning = false;
                }

                if (!NativeMGDLibrary.isRunning) {
                    NativeMGDLibrary.authorisationCallback = var0;
                    int var1x = NativeMGDLibrary.INSTANCE.startService(var2, var3, var4);
                    Log.i("MGD Native Library", "Returned from calling 'startservice' JNI function.");
                    if (var1x != 0) {
                        NativeMGDLibrary.INSTANCE.stopService();
                    } else {
                        NativeMGDLibrary.isRunning = true;
                        NativeMGDLibrary.lastStartedWithSendTraceDataToHost = var2;
                    }

                    if (var1 != null) {
                        var1.accept(var1x);
                    }
                }

            }
        });
    }

    private native int startService(boolean var1, boolean var2, Object var3);

    public static void stopDaemonService() {
        EXECUTOR.execute(new Runnable() {
            public void run() {
                if (NativeMGDLibrary.isRunning) {
                    NativeMGDLibrary.INSTANCE.stopService();
                }

                NativeMGDLibrary.isRunning = false;
            }
        });
    }

    private native int stopService();

    public void authorisationCallback(final FeatureAuthorisation.Feature var1, final boolean var2, final String var3) {
        EXECUTOR.execute(new Runnable() {
            public void run() {
                if (NativeMGDLibrary.authorisationCallback != null) {
                    NativeMGDLibrary.authorisationCallback.accept(var1, var2, var3);
                }

            }
        });
    }

    public interface IAuthorisationCallback {
        void accept(FeatureAuthorisation.Feature var1, boolean var2, String var3);
    }

    public interface IExitCodeCallback {
        void accept(int var1);
    }
}
