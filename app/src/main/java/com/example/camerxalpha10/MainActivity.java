package com.example.camerxalpha10;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton capturebutton;
    private Executor mainexecuter;
    private final static int REQUEST_CODE_PERMISSION = 10;
    private final static String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    Context context = this;
    private static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    private static final double RATIO_16_9_VALUE = 16.0 / 9.0;
    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";
    private  File outputDirectory;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        capturebutton = findViewById(R.id.capture_button);
        mainexecuter = ContextCompat.getMainExecutor(this);
        previewView = findViewById(R.id.view_finder);
        outputDirectory = getExternalMediaDirs()[0];
        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        cameraProviderFuture.addListener(() -> {
            try {
               cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future
                // This should never be reached
            }
        }, ContextCompat.getMainExecutor(context));

        if (allPermissionsGranted()) {
            previewView.post(new Runnable() {
                @Override
                public void run() {

                    bindPreview(cameraProvider);

                }
            });

        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_PERMISSION) {
//            if (allPermissionsGranted()) {
//                previewView.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
//                        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
//                        cameraProviderFuture.addListener(() -> {
//                            try {
//                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                                bindPreview(cameraProvider);
//                            } catch (ExecutionException | InterruptedException e) {
//                                // No errors need to be handled for this Future
//                                // This should never be reached
//                            }
//                        }, ContextCompat.getMainExecutor(context));
//
//                    }
//                });
//            }
//        }
//    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        DisplayMetrics matrix = new DisplayMetrics();
        previewView.getDisplay().getMetrics(matrix);
        Integer ratio = aspectRatio(matrix.widthPixels,matrix.heightPixels);
        Preview preview = new Preview.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .setTargetAspectRatio(ratio)
                .build();

//        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());
        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());


        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();


        ImageCapture imageCapture =
                new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(ratio)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

        // Create output file to hold the image
        File photoFile = createFile(outputDirectory,FILENAME , PHOTO_EXTENSION);
        imageCapture.takePicture(photoFile, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull Image image) {
                super.onCaptureSuccess(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        } {
        });




        cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture);
        }


    private  Integer aspectRatio(Integer width, Integer height){
        Double previewRatio = ((double) max(width, height))/ min(width, height);
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    /** Helper function used to create a timestamped file
     * @return*/
    private File createFile(File basefolder, String format, String extension) {
//        File(baseFolder, SimpleDateFormat(format, Locale.US)
//                .format(System.currentTimeMillis()) + extension)
        format = new SimpleDateFormat(format,Locale.US).format(System.currentTimeMillis())+extension;
        File file = new File(basefolder,format);
        return file;
    }




    private boolean allPermissionsGranted() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
