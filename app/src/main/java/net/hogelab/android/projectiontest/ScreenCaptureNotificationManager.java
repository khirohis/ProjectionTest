package net.hogelab.android.projectiontest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ScreenCaptureNotificationManager {
    private static final String TAG = ScreenCaptureNotificationManager.class.getSimpleName();

    private static final String NOTIFICATION_CHANNEL_ID = "screen_capturing_status_notification";


    //--------------------------------------------------
    // singleton
    //--------------------------------------------------

    private static ScreenCaptureNotificationManager singleton;

    public static void init(Context context) {
        if (singleton == null) {
            singleton = new ScreenCaptureNotificationManager(context);
        }
    }

    public static ScreenCaptureNotificationManager getInstance() {
        return singleton;
    }


    //--------------------------------------------------
    //
    //--------------------------------------------------

    private final NotificationManager notificationManager;


    //--------------------------------------------------
    // constructor
    //--------------------------------------------------

    private ScreenCaptureNotificationManager(Context context) {
        Log.d(TAG, "constructor");

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    //--------------------------------------------------
    // public functions
    //--------------------------------------------------

    public void createNotificationChannel(Context context) {
        Log.d(TAG, "createNotificationChannel");

        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.app_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.app_notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    public Notification createNotification(Context context) {
        Log.d(TAG, "createNotification");

        return createNotificationInner(context)
                .build();
    }

    public Notification createNotification(Context context, Bitmap largeIcon) {
        Log.d(TAG, "createNotification");

        return createNotificationInner(context)
                .setLargeIcon(largeIcon)
                .build();
    }

    public void notify(int id, Notification notification) {
        notificationManager.notify(id, notification);
    }


    //--------------------------------------------------
    // private functions
    //--------------------------------------------------

    public NotificationCompat.Builder createNotificationInner(Context context) {
        Log.d(TAG, "createNotificationInner");

        PendingIntent settingsIntent = PendingIntent.getActivity(
                context,
                0,
                MainActivity.createSettingIntent(context),
                PendingIntent.FLAG_MUTABLE);

        PendingIntent snapShotIntent = PendingIntent.getService(
                context,
                0,
                ScreenCaptureService.createSnapshotIntent(context),
                PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.app_notification_title))
                .setContentText(context.getString(R.string.app_notification_text))
                .addAction(R.drawable.baseline_settings_24,
                        context.getString(R.string.app_notification_button_settings),
                        settingsIntent)
                .addAction(R.drawable.baseline_camera_alt_24,
                        context.getString(R.string.app_notification_button_do_snapshot),
                        snapShotIntent);
    }
}
