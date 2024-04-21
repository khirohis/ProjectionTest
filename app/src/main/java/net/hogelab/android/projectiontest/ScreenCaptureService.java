package net.hogelab.android.projectiontest;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


//--------------------------------------------------
// class MainService
//--------------------------------------------------

public class ScreenCaptureService extends Service {
    private static final String TAG = ScreenCaptureService.class.getSimpleName();

    public static final int SERVICE_ID = 1000;

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_DO_SNAPSHOT = "action_do_snapshot";

    public static final String EXTRA_RESULT_CODE = "extra_result_code";
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";
    public static final String EXTRA_DENSITY_DPI = "extra_density_dpi";


    //--------------------------------------------------
    // static functions
    //--------------------------------------------------

    public static Intent createSnapshotIntent(Context context) {
        Log.d(TAG, "createSnapshotIntent");

        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.setAction(ACTION_DO_SNAPSHOT);

        return intent;
    }

    public static void startService(Context context, int resultCode, Intent resultData,
                                    int width, int height, int densityDpi) {
        Log.d(TAG, "startService");

        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtras(resultData);
        intent.putExtra(EXTRA_WIDTH, width);
        intent.putExtra(EXTRA_HEIGHT, height);
        intent.putExtra(EXTRA_DENSITY_DPI, densityDpi);
        context.startForegroundService(intent);
    }

    public static void stopService(Context context) {
        Log.d(TAG, "stopService");

        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.setAction(ACTION_STOP);
        context.startForegroundService(intent);
    }


    //--------------------------------------------------
    // override functions
    //--------------------------------------------------

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_START:
                    onCommandActionStart(intent);
                    break;
                case ACTION_STOP:
                    onCommandActionStop();
                    break;
                case ACTION_DO_SNAPSHOT:
                    onCommandActionDoSnapshot();
                    break;

                default:
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    //--------------------------------------------------
    // private functions
    //--------------------------------------------------

    private void onCommandActionStart(Intent intent) {
        Notification notification = ScreenCaptureNotificationManager.getInstance()
                .createNotification(this);
        startForeground(SERVICE_ID, notification);

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        int width = intent.getIntExtra(EXTRA_WIDTH, 0);
        int height = intent.getIntExtra(EXTRA_HEIGHT, 0);
        int densityDpi = intent.getIntExtra(EXTRA_DENSITY_DPI, 0);
        ScreenCaptureManager.getInstance().startScreenCapture(
                resultCode, intent, width, height, densityDpi);
    }

    private void onCommandActionStop() {
        ScreenCaptureManager.getInstance().stopScreenCapture();

        stopForeground(true);
        stopSelf(SERVICE_ID);
    }

    private void onCommandActionDoSnapshot() {
        Log.d(TAG, "ACTION_DO_SNAPSHOT");
    }
}
