package com.ufrst.app.trombi.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_E;

public class ActivityCapture extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private MaterialButton buttonDismiss, buttonNextIfPhoto;
    private ImageButton buttonPrevious, buttonNext;
    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private LinearLayout linearLayout;
    private FloatingActionButton fab;
    private PreviewView previewView;
    private BottomAppBar toolbar;
    private ImageView imageView;
    private CardView banner;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Eleve currentEleve;
    private long idEleve;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        findViews();
        getExtras();
        observeEleve();
        setListeners();
        checkForExistingPhoto();

        if(allPermissionsGranted()){
            startCamera();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    private void findViews(){
        buttonNextIfPhoto = findViewById(R.id.CAPT_buttonNextIfPhoto);
        coordinatorLayout = findViewById(R.id.CAPT_coordinator);
        buttonDismiss = findViewById(R.id.CAPT_buttonDismiss);
        linearLayout = findViewById(R.id.CAPT_linearLayout);
        buttonPrevious = findViewById(R.id.CAPT_previous);
        previewView = findViewById(R.id.CAPT_previewView);
        imageView = findViewById(R.id.CAPT_imgView);
        buttonNext = findViewById(R.id.CAPT_next);
        toolbar = findViewById(R.id.CAPT_toolbar);
        banner = findViewById(R.id.CAPT_banner);
        fab = findViewById(R.id.CAPT_takePic);
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

        fab.setOnClickListener(new View.OnClickListener() {
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
                                            R.string.CAPT_photoTaken,
                                            Toast.LENGTH_LONG).show());

                                    currentEleve.setPhoto(Uri.fromFile(f).toString());
                                    trombiViewModel.update(currentEleve);
                                }

                                @Override
                                public void onError(int imageCaptureError,
                                                    @NonNull String message,
                                                    @Nullable Throwable cause){
                                    previewView.post(() -> Toast.makeText(ActivityCapture.this,
                                            R.string.CAPT_error,
                                            Toast.LENGTH_LONG).show());
                                }
                            });
                } else{
                    Snackbar.make(coordinatorLayout,
                            R.string.CAPT_error2,
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

    private void setListeners(){
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                hideBanner();
            }
        });
    }

    // Récupère l'élève dans la base de données et vérifie s'il a une photo
    private void checkForExistingPhoto(){
        CompletableFuture.supplyAsync(() -> trombiViewModel.getEleveByIdWithGroupsNotLive(idEleve))
                .thenApply(EleveWithGroups::getEleves)
                .thenApply(eleve -> !eleve.getPhoto().trim().equals(""))
                .thenAccept(hasPhoo -> {
                    if(hasPhoo)
                        showBanner();
                });
    }

    private void showBanner(){
        banner.animate()
                .alpha(1.0f)
                .setDuration(1000);

        // Le LinearLayout descends de la taille de la bannière
        banner.post(() -> linearLayout.animate()
                .translationY(banner.getHeight())
                .setDuration(300));

        // Mise en place des listeners
        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                hideBanner();
            }
        });

        buttonNextIfPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(ActivityCapture.this, "bruh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideBanner(){
        banner.animate()
                .translationY(-banner.getHeight())
                .setDuration(300);

        // Le LinearLayout retourne à la position initiale
        linearLayout.animate()
                .translationY(0)
                .setDuration(300);

        // Retrait des listeners
        buttonNextIfPhoto.setOnClickListener(null);
        buttonDismiss.setOnClickListener(null);
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
