package com.arm.mgd.androidapp.applist;

import android.content.Intent;
import android.util.Log;

import com.arm.mgd.androidapp.features.FeatureAuthorisation;

public class DaemonLauncher {
    private static final String HEADLESS_MODE = "HeadlessMode";
    public static final String NATIVE_MGD_LIBRARY_EXIT_CODE = "NATIVE_MGD_LIBRARY_EXIT_CODE";
    private static final String SEND_TRACE_DATA_TO_HOST = "SendTraceDataToHost";
    private static final String TAG = "MGD Daemon Launcher";

    private final NativeMGDLibrary.IAuthorisationCallback authorisationCallback = new NativeMGDLibrary.IAuthorisationCallback()
    {
        public void accept(FeatureAuthorisation.Feature paramAnonymousFeature, boolean paramAnonymousBoolean, String paramAnonymousString)
        {
            Log.d(TAG, "Author call back: ");
        }
    };
    private final NativeMGDLibrary.IExitCodeCallback exitCodeCallback = new NativeMGDLibrary.IExitCodeCallback()
    {
        public void accept(int paramAnonymousInt)
        {
            Log.d(TAG, "exit call back");
        }
    };

    public static String getIntentIdentifier()
    {
        return "MGD Daemon Launcher";
    }


    public int StartCommand(Intent paramIntent, int paramInt1, int paramInt2)
    {
        Log.i("MGD Daemon Launcher", "Received start id " + paramInt2 + ": " + paramIntent);
        boolean bool1 = paramIntent.getBooleanExtra("SendTraceDataToHost", true);
        Log.i("MGD Daemon Launcher", "Send data to host: " + bool1);
        boolean bool2 = paramIntent.getBooleanExtra("HeadlessMode", false);
        Log.i("MGD Daemon Launcher", "Headless mode: " + bool2);
        NativeMGDLibrary.startDaemonService(this.authorisationCallback, this.exitCodeCallback, bool1, bool2, paramIntent.getExtras());
        return 2;
    }
}
