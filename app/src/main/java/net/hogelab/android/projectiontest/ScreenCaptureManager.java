package net.hogelab.android.projectiontest;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;


//--------------------------------------------------
// class ScreenCaptureManager
//--------------------------------------------------

public class ScreenCaptureManager {
    private static final String TAG = ScreenCaptureManager.class.getSimpleName();

    //--------------------------------------------------
    // callback interface
    //--------------------------------------------------

    public interface CaptureStateListener {
        void onCaptureStateChanged(boolean isCapturing);
    }


    //--------------------------------------------------
    // singleton
    //--------------------------------------------------

    private static ScreenCaptureManager singleton;

    public static void init(Context context) {
        if (singleton == null) {
            singleton = new ScreenCaptureManager(context);
        }
    }

    public static ScreenCaptureManager getInstance() {
        return singleton;
    }


    //--------------------------------------------------
    // member variables
    //--------------------------------------------------

    private boolean captureState;
    private CaptureStateListener captureStateListener;

    private final Handler mainHandler;
    private final HandlerThread captureHandlerThread;
    private final Handler captureHandler;

    private final MediaProjectionManager mediaProjectionManager;
    private final MediaProjection.Callback mediaProjectionCallback;
    private MediaProjection mediaProjection;

    private final VirtualDisplay.Callback virtualDisplayCallback;
    private VirtualDisplay virtualDisplay;

    private int pixelFormat = PixelFormat.RGBA_8888;
    private int maxImages = 2;
    private float scaleFactor = 0.1f;

    private int captureWidth;
    private int captureHeight;
    private int captureDensityDpi;


    //--------------------------------------------------
    // constructor
    //--------------------------------------------------

    private ScreenCaptureManager(Context context) {
        Log.d(TAG, "constructor");

        mainHandler = new Handler(Looper.getMainLooper());
        captureHandlerThread = new HandlerThread("capture_handler_thread");
        captureHandlerThread.start();
        captureHandler = new Handler(captureHandlerThread.getLooper());

        mediaProjectionManager =
                (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    //--------------------------------------------------
    // public functions
    //--------------------------------------------------

    public synchronized void setDefaultPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public synchronized void setDefaultMaxImages(int maxImages) {
        this.maxImages = maxImages;
    }

    public synchronized void setDefaultScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }


    public synchronized boolean getCaptureState() {
        Log.d(TAG, "getCaptureState: " + captureState);

        return captureState;
    }

    public synchronized void setCaptureStateListener(CaptureStateListener listener) {
        Log.d(TAG, "setCaptureStateListener");

        captureStateListener = listener;
    }


    public Intent createScreenCaptureIntent() {
        Log.d(TAG, "createScreenCaptureIntent");

        return mediaProjectionManager.createScreenCaptureIntent();
    }

    public void startScreenCapture(int resultCode, Intent resultData,
                                   int width, int height, int densityDpi) {
        Log.d(TAG, "startScreenCapture");

        captureHandler.post(() -> {
            if (!getCaptureState()) {
                startScreenCaptureInner(
                        resultCode, resultData, width, height, densityDpi);
            }
        });

        setCaptureState(true);
    }


    public void stopScreenCapture() {
        Log.d(TAG, "stopScreenCapture");

        captureHandler.post(() -> {
            if (getCaptureState()) {
                stopScreenCaptureInner();
            }
        });

        setCaptureState(false);
    }


    //--------------------------------------------------
    // private functions
    //--------------------------------------------------

    private synchronized void setCaptureState(boolean newState) {
        Log.d(TAG, "setCaptureState: " + newState);

        if (captureState != newState) {
            captureState = newState;
            mainHandler.post(() -> {
                if (captureStateListener != null) {
                    captureStateListener.onCaptureStateChanged(newState);
                }
            });
        }
    }


    public void startScreenCaptureInner(int resultCode, Intent resultData,
                                   int width, int height, int densityDpi) {
        Log.d(TAG, "startScreenCaptureInner");

        captureWidth = (int) (width * scaleFactor);
        captureHeight = (int) (height * scaleFactor);
        captureDensityDpi = densityDpi;

        ImageReader imageReader = ImageReader.newInstance(
                captureWidth, captureHeight,
                pixelFormat, maxImages);
        imageReader.setOnImageAvailableListener(this::onImageAvailable, captureHandler);

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        mediaProjection.registerCallback(mediaProjectionCallback, null);

        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ProjectionTest",
                captureWidth, captureHeight, captureDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                virtualDisplayCallback,
                captureHandler);
    }

    public void stopScreenCaptureInner() {
        Log.d(TAG, "stopScreenCaptureInner");

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }


    // MediaProjection.Callback
    {
        mediaProjectionCallback = new MediaProjection.Callback() {

            @Override
            public void onStop() {
                Log.d(TAG, "MediaProjection.Callback: onStop");
                super.onStop();
            }

            @Override
            public void onCapturedContentResize(int width, int height) {
                Log.d(TAG, "MediaProjection.Callback: onCapturedContentResize");
                super.onCapturedContentResize(width, height);
            }

            @Override
            public void onCapturedContentVisibilityChanged(boolean isVisible) {
                Log.d(TAG, "MediaProjection.Callback: onCapturedContentVisibilityChanged");
                super.onCapturedContentVisibilityChanged(isVisible);
            }
        };
    }

    // VirtualDisplay.Callback
    {
        virtualDisplayCallback = new VirtualDisplay.Callback() {

            @Override
            public void onPaused() {
                Log.d(TAG, "VirtualDisplay.Callback: onPaused");
                super.onPaused();
            }

            @Override
            public void onResumed() {
                Log.d(TAG, "VirtualDisplay.Callback: onResumed");
                super.onResumed();
            }

            @Override
            public void onStopped() {
                Log.d(TAG, "VirtualDisplay.Callback: onStopped");
                super.onStopped();
            }
        };
    }

    // ImageReader.OnImageAvailableListener
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG, "ImageReader.OnImageAvailableListener: onImageAvailable");

        Image image = reader.acquireLatestImage();
        if (image != null) {
//            Image.Plane[] planes = image.getPlanes();
//            Image.Plane plane = planes[0];
            image.close();
        }
    }
}
