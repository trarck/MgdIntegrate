package com.trarck.integrate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;
import com.arm.mgd.androidapp.applist.*;

public class InterceptUnityPlayerActivity extends Activity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    protected  boolean mUnityShow;
    protected boolean mUnityFullScreen;

    protected String mUnityStartScene;

    protected DaemonLauncher mDaemonLauncher;

    protected  void LoadIntercept()
    {
        try
        {
            Log.d("aga_","load mgd");
            System.loadLibrary("AGA");
        }
        catch (UnsatisfiedLinkError e)
        {
            // Feel free to remove this log message.
            Log.e("aga_", "GA not loaded: " + e.getMessage());
            Log.d("aga_", Log.getStackTraceString(e));
        }
    }
    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("aga_", "onCreate: in");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //run Daemon
        mDaemonLauncher=new DaemonLauncher();
        mDaemonLauncher.StartCommand(getIntent(),0,1);

        Log.d("aga_", "onCreate: before");
        super.onCreate(savedInstanceState);
        Log.d("aga_", "onCreate: after");
        //init unity
        initUnityPlayer();
        Log.d("aga_", "int unity: after");
        mUnityShow=false;
        mUnityFullScreen=false;

        InitContent();
    }

    protected void InitContent(){
        setContentView(R.layout.activity_main);

        Button btnIntercept =  findViewById(R.id.btn_intercept);
        Button btnStart =  findViewById(R.id.btn_start);

        btnIntercept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadIntercept();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnityPlayerFullScreen();
            }
        });
    }

    protected  void initUnityPlayer(){
        mUnityPlayer = new UnityPlayer(this);
        if (mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
            setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        initUnitySurfaceView();
    }

    private void initUnitySurfaceView(){
        FrameLayout unityView= (FrameLayout) mUnityPlayer.getView();
        if(unityView!=null){
            SurfaceView renderer=null;
            for(int i=0,l=unityView.getChildCount();i<l;++i){
                if(unityView.getChildAt(i) instanceof SurfaceView){
                    renderer=(SurfaceView)unityView.getChildAt(i);
                    break;
                }
            }
            if(renderer!=null){
                renderer.setZOrderOnTop(true);
            }
        }
    }

    protected void showUnityPlayerFullScreen(){
        Log.d("Amino", "showUnityPlayerFullScreen start ");
        if(!mUnityShow || !mUnityFullScreen ){
            mUnityShow=true ;
            mUnityFullScreen=true;
            setContentView(mUnityPlayer.getView());
            mUnityPlayer.requestFocus();
        }
        Log.d("Amino", "showUnityPlayerFullScreen end ");
    }

    public void showUnityPlayerFullScreenMono(){

        final InterceptUnityPlayerActivity self=this;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                self.showUnityPlayerFullScreen();
            }
        });
    }

    public String getUnityStartScene(){
        return mUnityStartScene;
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        if(mUnityPlayer!=null)
            mUnityPlayer.destroy();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        if(mUnityPlayer!=null)
            mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        if(mUnityPlayer!=null)
            mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        if(mUnityPlayer!=null)
            mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        if(mUnityPlayer!=null)
            mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        if(mUnityPlayer!=null)
            mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            if(mUnityPlayer!=null)
                mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if(mUnityPlayer!=null)
            mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(mUnityPlayer!=null)
            mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            if(mUnityPlayer!=null)
                return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     {
        if(mUnityPlayer!=null) {
            return mUnityPlayer.injectEvent(event);
        }else {
            return false;
        }
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   {
        if(mUnityPlayer!=null) {
            return mUnityPlayer.injectEvent(event);
        }else {
            return false;
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event)          {
        if(mUnityPlayer!=null) {
            return mUnityPlayer.injectEvent(event);
        }else {
            return  false;
        }
    }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event) {
        if (mUnityPlayer != null) {
            return mUnityPlayer.injectEvent(event);
        } else {
            return false;
        }

    }
}
