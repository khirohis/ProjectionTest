package net.hogelab.android.projectiontest;

import androidx.lifecycle.MutableLiveData;

public class MainBindingHandler {
    public MutableLiveData<String> externalDisplayTotalCount = new MutableLiveData<>();
    public MutableLiveData<String> externalDisplayPresentationCount = new MutableLiveData<>();

    public void handleStartScreenCapture() {}
    public void handleStopScreenCapture() {}
    public void handleUpdateExternalDisplayCount() {}
}