package com.ufrst.app.trombi.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.util.ImageUtil;
import com.ufrst.app.trombi.util.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_E;

public class ActivityCapture extends AppCompatActivity {

    public static final String EXTRA_MODE = "com.ufrst.app.trombi.EXTRA_MODE";
    public static final int TAKE_PHOTO_MODE = 1;
    public static final int TAKE_ALL_PHOTO_MODE = 2;

    private static final int EDIT_MODE = 3;
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
    private ImageView helperFrame;
    private BottomAppBar toolbar;
    private CropImageView editImage;
    private TextView tvName;
    private CardView banner;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private List<Eleve> listEleves;
    private Eleve currentEleve;
    private long idTrombi;              // Utile seulement si on est en mode TAKE_ALL_PHOTO
    private long idEleve;
    private int index = 0;              // Détermine l'élève à modifier si le mode est TAKE_ALL_PHOTO
    private int mode;                   // Détermine si l'on modifie ou prend une photo

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        findViews();
        getExtras();
        observeEleve();
        setListeners();
        //updateUI();

        // Vérification des permissions accordées
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
        editImage = findViewById(R.id.CAPT_resultImage);
        helperFrame = findViewById(R.id.CAPT_imgView);
        buttonNext = findViewById(R.id.CAPT_next);
        toolbar = findViewById(R.id.CAPT_toolbar);
        tvName = findViewById(R.id.CAPT_tvName);
        banner = findViewById(R.id.CAPT_banner);
        fab = findViewById(R.id.CAPT_takePic);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idEleve = intent.getLongExtra(EXTRA_ID_E, -1);
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
        mode = intent.getIntExtra(EXTRA_MODE, TAKE_PHOTO_MODE);
    }

    private void observeEleve(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        // Observer la liste de tous les élèves du trombi si on est dans le mode correspondant
        if(mode == TAKE_ALL_PHOTO_MODE){
            // Cet observeur regarde la liste de tous les élèves, puis actualise la valeur
            // de l'élève à observé selon l'attribut index de la classe (voir bloc suivant)
            trombiViewModel.getElevesByTrombi(idTrombi).observe(this, new Observer<List<Eleve>>() {
                @Override
                public void onChanged(List<Eleve> eleves){
                    listEleves = eleves;

                    // Le mode capture de classe est lancé alors qu'il n'y a pas d'élève dans le trombi
                    if(eleves.isEmpty()){
                        showToast(getResources().getText(R.string.CAPT_noEleve));
                        finish();
                        return;
                    }

                    trombiViewModel.setIdEleve(listEleves.get(index).getIdEleve());
                }
            });
        } else if(mode == TAKE_PHOTO_MODE){ // || mode == EDIT_MODE ?
            trombiViewModel.setIdEleve(idEleve);
            buttonNext.setVisibility(View.GONE);
            buttonPrevious.setVisibility(View.GONE);
        }

        // eleveForPhoto est une transformation, voir TrombiViewModel pour plus d'informations
        trombiViewModel.eleveForPhoto.observe(ActivityCapture.this, new Observer<Eleve>() {
            @Override
            public void onChanged(Eleve eleve){
                currentEleve = eleve;
                checkForExistingPhoto();
                updateUI();
            }
        });

    }

    // Listener du fab dans ActivityCapture#bindPreview
    private void setListeners(){
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                switchMode(TAKE_ALL_PHOTO_MODE);
                index++;

                // Changement de l'id de l'élève à observer, donc changement de la valeur currentEleve
                if(index < listEleves.size()){
                    trombiViewModel.setIdEleve(listEleves.get(index).getIdEleve());
                } else{
                    showToast(getResources().getText(R.string.CAPT_lastEleve));
                    index--;
                }
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                switchMode(TAKE_ALL_PHOTO_MODE);
                index--;

                // Changement de l'id de l'élève à observer, donc changement de la valeur currentEleve
                if(index >= 0){
                    trombiViewModel.setIdEleve(listEleves.get(index).getIdEleve());
                } else{
                    showToast(getResources().getText(R.string.CAPT_firstEleve));
                    index++;
                }
            }
        });
    }

    // Récupère l'élève dans la base de données et vérifie s'il a une photo
    private void checkForExistingPhoto(){
        CompletableFuture.supplyAsync(() ->
                trombiViewModel.getEleveByIdWithGroupsNotLive(currentEleve.getIdEleve()))
                .thenApply(EleveWithGroups::getEleves)
                .thenApply(eleve -> !eleve.getPhoto().trim().equals(""))
                .thenAccept(hasPhoto -> {
                    if(hasPhoto)
                        showBanner();
                    else
                        hideBanner();
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
                if(mode == EDIT_MODE)
                    saveEditedImage();

                else if(mode == TAKE_PHOTO_MODE || mode == TAKE_ALL_PHOTO_MODE)
                    saveImage(imageCapture);

                else
                    throw new IllegalStateException("Mode inconnu");
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

    // Enregistre l'image prise depuis la caméra
    private void saveImage(ImageCapture imageCapture){
        Logger.logV("Current eleve", currentEleve.getNomPrenom());

        if(getExternalFilesDir(null) != null){
            String path = ImageUtil.getPathNameForEleve(
                    getExternalFilesDir(null).getPath(),
                    currentEleve);

            File f = new File(path);

            imageCapture.takePicture(f,
                    Executors.newSingleThreadExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull File file){
                            //showToast(getResources().getText(R.string.CAPT_photoTaken));

                            changeElevePhoto(f);
                            loadImageEditor(Uri.fromFile(f));
                        }

                        @Override
                        public void onError(int imageCaptureError,
                                            @NonNull String message,
                                            @Nullable Throwable cause){
                            showToast(getResources().getText(R.string.CAPT_error));
                        }
                    });
        } else{
            showToast(getResources().getText(R.string.CAPT_error2));
        }
    }

    // Fait apparaître l'outil d'édition de la photo prise
    private void loadImageEditor(Uri capturedImage){
        switchMode(EDIT_MODE);

        //editImage.setShowCropOverlay(false);
        editImage.post(() -> editImage.setImageUriAsync(capturedImage));

        /*runOnUiThread(() -> {
            //editImage.setCropShape(CropImageView.CropShape.OVAL);     // https://github.com/ArthurHub/Android-Image-Cropper/issues/553

        });*/
    }

    private void saveEditedImage(){
        Bitmap bitmap = editImage.getCroppedImage();
        ImageUtil imageUtil = new ImageUtil();

        CompletableFuture.supplyAsync(() ->
                imageUtil.saveImage(bitmap, getExternalFilesDir(null).getPath(), currentEleve))
                .exceptionally(throwable -> null)
                .thenApply(this::changeElevePhoto)
                .thenAccept(this::alertImageSaved);
    }

    // Avertit l'utilisateur lors de la sauvegarde d'une image modifiée
    private void alertImageSaved(boolean isImageSaved){
        runOnUiThread(() -> {
            if(isImageSaved)
                Toast.makeText(this, R.string.CAPT_photoModified, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.U_erreur, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean changeElevePhoto(File photo){
        if(photo != null){
            currentEleve.setPhoto(Uri.fromFile(photo).toString());
            trombiViewModel.update(currentEleve);
            return true;
        }

        return false;
    }

    // Attribue les bonnes valeures aux champs de l'UI et déclenche une animation
    // isNextDétermine le sens de l'animation, 1 pour suivant, ou -1 pour précédent
    private void updateUI(){
        tvName.setText(currentEleve.getNomPrenom());

        tvName.animate()
                .translationY(tvName.getHeight())
                .setDuration(0);

        tvName.postDelayed(() -> tvName.animate()
                .translationY(0)
                .setDuration(500), 100);
    }

    private void showBanner(){
        // Pas de bannière en mode photo unique ou edite
        if(mode == TAKE_PHOTO_MODE || mode == EDIT_MODE){
            return;
        }

        // Animations
        banner.animate()
                .alpha(1.0f)
                .setDuration(1000);

        linearLayout.clearAnimation();

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
                // Si on est en photo de classe, on passe à l'élève suivant sinon on sort
                buttonNext.callOnClick();
            }
        });
    }

    private void hideBanner(){
        banner.animate()
                .translationY(-banner.getHeight())
                .setDuration(300);

        banner.postDelayed(() -> banner.animate().translationY(0).alpha(0.0f).setDuration(0), 400);

        // Le LinearLayout retourne à la position initiale
        linearLayout.animate()
                .translationY(0)
                .setDuration(300);

        // Retrait des listeners
        buttonNextIfPhoto.setOnClickListener(null);
        buttonDismiss.setOnClickListener(null);
    }

    // Change l'UI pour refléter le mode édition ou prise de photo
    private void switchMode(int newMode){
        mode = newMode;

        // La modification des vues se fait sur le ThreadUI
        if(mode == EDIT_MODE){
            runOnUiThread(() -> {
                previewView.setVisibility(View.GONE);
                helperFrame.setVisibility(View.GONE);
                editImage.setVisibility(View.VISIBLE);

                fab.hide();
                fab.postDelayed(() -> fab.setImageResource(R.drawable.ic_valid), 500);
                fab.postDelayed(() -> fab.show(), 1000);
            });
        } else if(mode == TAKE_PHOTO_MODE || mode == TAKE_ALL_PHOTO_MODE){
            runOnUiThread(() -> {
                previewView.setVisibility(View.VISIBLE);
                helperFrame.setVisibility(View.VISIBLE);
                editImage.setVisibility(View.GONE);

                fab.hide();
                fab.postDelayed(() -> fab.setImageResource(R.drawable.ic_photo), 500);
                fab.postDelayed(() -> fab.show(), 1500);
            });
        } else{
            throw new IllegalStateException("Mode inconnu");
        }
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

    // Crée des Toast
    private void showToast(CharSequence text){
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed(){
        if(mode == EDIT_MODE){
            switchMode(TAKE_PHOTO_MODE);
        } else{
            super.onBackPressed();
        }
    }

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
