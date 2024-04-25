package net.hogelab.android.projectiontest;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyExecutor {
    private static final String TAG = MyExecutor.class.getSimpleName();


    //--------------------------------------------------
    // singleton
    //--------------------------------------------------

    private static MyExecutor singleton;

    public static void init() {
        if (singleton == null) {
            singleton = new MyExecutor();
        }
    }


    //--------------------------------------------------
    // member variables
    //--------------------------------------------------

    private final Handler mainHandler;
    private final Handler screenCaptureHandler;
    private final ExecutorService workerExecutor;


    private MyExecutor() {
        mainHandler = new Handler(Looper.getMainLooper());

        HandlerThread handlerThread = new HandlerThread("capture_handler_thread");
        handlerThread.start();
        screenCaptureHandler = new Handler(handlerThread.getLooper());

        workerExecutor = Executors.newCachedThreadPool();
    }


    @NonNull
    public static Handler getMainHandler() {
        Log.d(TAG, "getMainHandler");

        return singleton.mainHandler;
    }

    public static void postMainHandler(Runnable runnable) {
        Log.d(TAG, "postMainHandler");

        singleton.mainHandler.post(runnable);
    }

    public static void executeMainHandler(Runnable runnable) {
        Log.d(TAG, "executeMainHandler");

        if (Thread.currentThread() == singleton.mainHandler.getLooper().getThread()) {
            runnable.run();
        } else {
            singleton.mainHandler.post(runnable);
        }
    }

    @NonNull
    public static Handler getScreenCaptureHandler() {
        Log.d(TAG, "getScreenCaptureHandler");

        return singleton.screenCaptureHandler;
    }

    public static void postScreenCaptureHandler(Runnable runnable) {
        Log.d(TAG, "postScreenCaptureHandler");

        singleton.screenCaptureHandler.post(runnable);
    }


    @NonNull
    public static ExecutorService getWorkerExecutor() {
        Log.d(TAG, "getWorkerExecutor");

        return singleton.workerExecutor;
    }
}
