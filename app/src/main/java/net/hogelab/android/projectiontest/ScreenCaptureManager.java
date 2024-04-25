package net.hogelab.android.projectiontest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import androidx.annotation.MainThread;

import java.util.LinkedList;
import java.util.List;


//--------------------------------------------------
// class ScreenCaptureManager
//--------------------------------------------------

public class ScreenCaptureManager {
    private static final String TAG = ScreenCaptureManager.class.getSimpleName();

    //--------------------------------------------------
    // callback interface
    //--------------------------------------------------

    public interface Callback {
        void onCapturingChanged(boolean isCapturing);
        void onImageAvailableChanged(boolean isImageAvailable);
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

    private boolean isCapturing;
    private boolean isImageAvailable;
    private Bitmap capturedImage;

    private final List<Callback> callbacks;

    private final MediaProjectionManager mediaProjectionManager;
    private final MediaProjection.Callback mediaProjectionCallback;
    private MediaProjection mediaProjection;

    private final VirtualDisplay.Callback virtualDisplayCallback;
    private VirtualDisplay virtualDisplay;

    private int pixelFormat = PixelFormat.RGBA_8888;
    private int maxImages = 2;
    private float scaleFactor = 0.05f;

    private int captureWidth;
    private int captureHeight;
    private int captureDensityDpi;


    //--------------------------------------------------
    // constructor
    //--------------------------------------------------

    private ScreenCaptureManager(Context context) {
        Log.d(TAG, "constructor");

        callbacks = new LinkedList<>();

        mediaProjectionManager =
                (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    //--------------------------------------------------
    // public functions
    //--------------------------------------------------

    @MainThread
    public boolean getCapturing() {
        Log.d(TAG, "getCaptureState: " + isCapturing);

        return isCapturing;
    }

    @MainThread
    public boolean getImageAvailable() {
        Log.d(TAG, "getImageAvailable: " + isImageAvailable);

        return isImageAvailable;
    }

    @MainThread
    public Bitmap getCapturedImage() {
        Log.d(TAG, "getCapturedImage");

        return capturedImage;
    }

    @MainThread
    public void addCallback(Callback callback) {
        callbacks.add(callback);
    }

    @MainThread
    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }


    @MainThread
    public void setDefaultPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @MainThread
    public void setDefaultMaxImages(int maxImages) {
        this.maxImages = maxImages;
    }

    @MainThread
    public void setDefaultScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }


    @MainThread
    public Intent createScreenCaptureIntent() {
        Log.d(TAG, "createScreenCaptureIntent");

        return mediaProjectionManager.createScreenCaptureIntent();
    }

    @MainThread
    public void startScreenCapture(int resultCode, Intent resultData,
                                   int width, int height, int densityDpi) {
        Log.d(TAG, "startScreenCapture");

        if (!getCapturing()) {
            setCapturing(true);

            MyExecutor.postScreenCaptureHandler(() -> {
                startScreenCaptureInner(
                        resultCode, resultData, width, height, densityDpi);
            });
        }
    }


    @MainThread
    public void stopScreenCapture() {
        Log.d(TAG, "stopScreenCapture");

        if (getCapturing()) {
            setCapturing(false);

            MyExecutor.getScreenCaptureHandler().post(() -> {
                stopScreenCaptureInner();
            });
        }
    }


    @MainThread
    public void doSnapshot() {
        if (isImageAvailable) {
        }
    }


    //--------------------------------------------------
    // private functions
    //--------------------------------------------------

    @MainThread
    private void setCapturing(boolean newState) {
        Log.d(TAG, "setCapturing: " + newState);

        if (isCapturing != newState) {
            isCapturing = newState;

            for (Callback callback : callbacks) {
                callback.onCapturingChanged(isCapturing);
            }
        }
    }

    @MainThread
    private void setImageAvailable(boolean newState) {
        Log.d(TAG, "setImageAvailable: " + newState);

        if (isImageAvailable != newState) {
            isImageAvailable = newState;

            for (Callback callback : callbacks) {
                callback.onImageAvailableChanged(isImageAvailable);
            }
        }
    }


    public void startScreenCaptureInner(int resultCode, Intent resultData,
                                   int width, int height, int densityDpi) {
        Log.d(TAG, "startScreenCaptureInner");

        captureWidth = (int) (width * scaleFactor);
        captureHeight = (int) (height * scaleFactor);
        captureDensityDpi = densityDpi;

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        mediaProjection.registerCallback(mediaProjectionCallback, null);

        ImageReader imageReader = ImageReader.newInstance(
                captureWidth, captureHeight,
                pixelFormat, maxImages);
        imageReader.setOnImageAvailableListener(this::onImageAvailable,
                MyExecutor.getScreenCaptureHandler());

        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ProjectionTest",
                captureWidth, captureHeight, captureDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                virtualDisplayCallback,
                MyExecutor.getScreenCaptureHandler());
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

                MyExecutor.postMainHandler(() -> stopScreenCapture());
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
            Image.Plane[] planes = image.getPlanes();
            Image.Plane plane = planes[0];
            Bitmap bitmap = Bitmap.createBitmap(
                    plane.getRowStride() / plane.getPixelStride(),
                    captureHeight,
                    Bitmap.Config.ARGB_8888);
            MyExecutor.postMainHandler(() -> capturedImage = bitmap);

            image.close();
        }

        MyExecutor.postMainHandler(() -> setImageAvailable(true));
    }
}
