package net.hogelab.android.projectiontest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import net.hogelab.android.projectiontest.databinding.ActivityMainBinding;


//--------------------------------------------------
// class MainActivity
//--------------------------------------------------

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    //--------------------------------------------------
    // static functions
    //--------------------------------------------------

    public static Intent createSettingIntent(Context context) {
        Log.d(TAG, "createSettingIntent");

        return new Intent(context, MainActivity.class);
    }


    //--------------------------------------------------
    // member variables
    //--------------------------------------------------

    private ActivityMainBinding binding;
    private final MainBindingHandler mainBindingHandler;

    private ActivityResultLauncher<Intent> requestPermissionLauncher;

    private final ScreenCaptureManager.Callback screenCaptureCallback;


    //--------------------------------------------------
    // public functions
    //--------------------------------------------------

    //--------------------------------------------------
    // override functions
    //--------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainBindingHandler(mainBindingHandler);
        binding.setLifecycleOwner(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onActivityResult);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        ScreenCaptureManager.getInstance().addCallback(screenCaptureCallback);
        // TODO: can force update?
        mainBindingHandler.capturing.setValue(ScreenCaptureManager.getInstance().getCapturing());

        mainBindingHandler.externalDisplayTotalCount.setValue("-");
        mainBindingHandler.externalDisplayPresentationCount.setValue("-");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        ScreenCaptureManager.getInstance().removeCallback(screenCaptureCallback);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        binding.setMainBindingHandler(null);
    }


    //--------------------------------------------------
    // private functions
    //--------------------------------------------------

    private void onCapturingChanged(boolean isCapturing) {
        Log.d(TAG, "onCapturingChanged");

        mainBindingHandler.capturing.setValue(isCapturing);
    }

    private void onImageAvailableChanged(boolean isCapturing) {
        Log.d(TAG, "onImageAvailableChanged");
    }


    private void startScreenCapture() {
        Log.d(TAG, "startScreenCapture");

        Intent intent = ScreenCaptureManager.getInstance().createScreenCaptureIntent();
        requestPermissionLauncher.launch(intent);
    }

    private void onActivityResult(ActivityResult result) {
        Log.d(TAG, "onScreenCaptureIntentResult");

        int resultCode = result.getResultCode();
        Intent resultData = result.getData();
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);

            ScreenCaptureService.startService(this, resultCode, resultData,
                    size.x, size.y, displayMetrics.densityDpi);
        }
    }

    private void stopScreenCapture() {
        Log.d(TAG, "stopScreenCapture");

        ScreenCaptureService.stopService(this);
    }


    private void updateExternalDisplayCount() {
        Log.d(TAG, "updateExternalDisplayCount");

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        mainBindingHandler.externalDisplayTotalCount.setValue(String.valueOf(displays.length));

        int presentationCount = 0;
        for (Display display: displays) {
            if (display.getState() == Display.STATE_ON &&
                    (display.getFlags() & Display.FLAG_PRESENTATION) != 0) {
                presentationCount++;
            }
        }
        mainBindingHandler.externalDisplayPresentationCount.setValue(String.valueOf(presentationCount));
    }


    // BindingHandler Overrides
    {
        mainBindingHandler = new MainBindingHandler() {

            @Override
            public void handleStartScreenCapture() {
                MainActivity.this.startScreenCapture();
            }

            @Override
            public void handleStopScreenCapture() {
                MainActivity.this.stopScreenCapture();
            }

            @Override
            public void handleUpdateExternalDisplayCount() {
                MainActivity.this.updateExternalDisplayCount();
            }
        };
    }

    // ScreenCaptureManager.Callback Overrides
    {
        screenCaptureCallback = new ScreenCaptureManager.Callback() {

            @Override
            public void onCapturingChanged(boolean isCapturing) {
                MainActivity.this.onCapturingChanged(isCapturing);
            }

            @Override
            public void onImageAvailableChanged(boolean isImageAvailable) {
                MainActivity.this.onImageAvailableChanged(isImageAvailable);
            }
        };
    }
}
