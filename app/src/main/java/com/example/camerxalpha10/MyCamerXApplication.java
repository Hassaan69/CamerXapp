package com.example.camerxalpha10;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

public class MyCamerXApplication extends Application implements CameraXConfig.Provider {
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

}
