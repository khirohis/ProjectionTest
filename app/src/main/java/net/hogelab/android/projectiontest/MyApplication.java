package net.hogelab.android.projectiontest;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        MyExecutor.init();

        ScreenCaptureManager.init(this);

        ScreenCaptureNotificationManager.init(this);
        ScreenCaptureNotificationManager.getInstance().createNotificationChannel(this);
    }
}
