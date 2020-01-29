package com.ufrst.app.trombi.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_E;

public class ActivityCapture extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private PreviewView previewView;
    private ImageView imageView;

    private Eleve currentEleve;
    private long idEleve;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        findViews();
        getExtras();
        observeEleve();

        if(allPermissionsGranted()){
            startCamera();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    private void findViews(){
        coordinatorLayout = findViewById(R.id.CAPT_coordinator);
        previewView = findViewById(R.id.CAPT_previewView);
        imageView = findViewById(R.id.CAPT_imgView);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idEleve = intent.getLongExtra(EXTRA_ID_E, -1);
    }

    private void observeEleve(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        trombiViewModel.getEleveById(idEleve).observe(this, new Observer<Eleve>() {
            @Override
            public void onChanged(Eleve eleve){
                currentEleve = eleve;

                // On a besoin de la valeur qu'une seule fois
                trombiViewModel.getEleveById(idEleve).removeObserver(this);
            }
        });
    }

    // Vérifie si les permission nécessaires sont accordées, sinon on les demande
    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        return true;
    }

    // Initialisation de la caméra
    private void startCamera(){
        // Récupération du caméra provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Une fois récupéré, on atteste de sa bonne instanciation et continue l'initialisation
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch(ExecutionException | InterruptedException e){
                // Ne peut pas se produire
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Le flux de la caméra est lié à la vue previewView
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider){
        Preview preview = new Preview.Builder()
                .setTargetName("Preview")
                .build();

        preview.setPreviewSurfaceProvider(previewView.getPreviewSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //-------------Prise de photos, experimental

        // CameraX est en alpha, la documentation est actuellement pas en accord avec la librairie
        /*ImageCaptureConfig config = new ImageCaptureConfig.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();*/

        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                //.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                //.setFlashMode(ImageCapture.FLASH_MODE_ON)
                .build();

        findViewById(R.id.CAPT_takePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(getExternalFilesDir(null) != null){
                    String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                    File f = new File(getExternalFilesDir(null).getPath()
                            + "/" + timeStamp + ".jpeg");

                    imageCapture.takePicture(f,
                            Executors.newSingleThreadExecutor(),
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(@NonNull File file){
                                    previewView.post(() -> Toast.makeText(ActivityCapture.this,
                                            "Image saved at : " + file.getPath(),
                                            Toast.LENGTH_LONG).show());
                                    //Log.v("_______________________________", Uri.fromFile(f).toString());
                                    currentEleve.setPhoto(Uri.fromFile(f).toString());

                                    trombiViewModel.update(currentEleve);
                                }

                                @Override
                                public void onError(int imageCaptureError,
                                                    @NonNull String message,
                                                    @Nullable Throwable cause){
                                    previewView.post(() -> Toast.makeText(ActivityCapture.this,
                                            "ezrro",
                                            Toast.LENGTH_LONG).show());
                                }
                            });
                } else{
                    Snackbar.make(coordinatorLayout,
                            "Impossible d'accéder au répertoire externe de l'application",
                            Snackbar.LENGTH_LONG);
                }
            }
        });

        // Tentative de récupération de l'image prise sans la stocker dans le stockage interne
        // du téléphone
        /*ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();*/

        /*ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer(){
            @Override
            public void analyze(@NonNull ImageProxy image) {

            }
        });*/

        //-----------------------------------------

        // Le paramètre imageCapture correspond à un useCase qui envisage la prise d'une photo
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    // Retourne le Bitmap correspondant à l'image capturée
    /*private Bitmap getBitmap(ImageProxy imageProxy){
        Image i = imageProxy.getImage();

        Image.Plane[] planes = i.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, i.getWidth(), i.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(49, 7, 273, 231), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, R.string.CAPT_requestDenied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
