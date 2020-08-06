package com.ufrst.app.trombi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.ufrst.app.trombi.util.FileUtil;
import com.ufrst.app.trombi.util.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_E;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_NOM;
import static com.ufrst.app.trombi.ui.ActivityMain.PREFS_FIXED_RATIO;
import static com.ufrst.app.trombi.ui.ActivityMain.PREFS_QUALITY_OR_LATENCY;

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
    private TrombiViewModel trombiViewModel;
    private LinearLayout linearLayout;
    private FloatingActionButton fab;
    private PreviewView previewView;
    private CropImageView editImage;
    private ImageView helperFrame;
    private TextView tvName;
    private CardView banner;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private List<Eleve> listEleves;
    private int qualityOrLatency;       // Paramètre définit par l'utilisateur pour choisir une meilleure qualité de photo ou le moins de latence
    private boolean isFixedRatio;
    private File imageToDelete;         // Image enregistrée en attendant le recadrage
    private Eleve currentEleve;
    private FileUtil fileUtil;
    private String nomTrombi;
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
        retrieveSharedPreferences();
        observeEleve();
        setListeners();

        // Instancie un objet gérant les fichiers qui pourraient être créer depuis cette activité
        fileUtil = new FileUtil(this, nomTrombi);

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
        buttonDismiss = findViewById(R.id.CAPT_buttonDismiss);
        linearLayout = findViewById(R.id.CAPT_linearLayout);
        buttonPrevious = findViewById(R.id.CAPT_previous);
        previewView = findViewById(R.id.CAPT_previewView);
        editImage = findViewById(R.id.CAPT_resultImage);
        helperFrame = findViewById(R.id.CAPT_imgView);
        buttonNext = findViewById(R.id.CAPT_next);
        tvName = findViewById(R.id.CAPT_tvName);
        banner = findViewById(R.id.CAPT_banner);
        fab = findViewById(R.id.CAPT_takePic);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idEleve = intent.getLongExtra(EXTRA_ID_E, -1);
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
        mode = intent.getIntExtra(EXTRA_MODE, TAKE_PHOTO_MODE);
        nomTrombi = intent.getStringExtra(EXTRA_NOM);
    }

    private void retrieveSharedPreferences(){
        SharedPreferences prefs = getSharedPreferences("com.ufrst.app.trombi", Context.MODE_PRIVATE);
        isFixedRatio = prefs.getBoolean(PREFS_FIXED_RATIO, true);
        boolean isLowLatency = prefs.getBoolean(PREFS_QUALITY_OR_LATENCY, false);

        if(isLowLatency)
            qualityOrLatency = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;
        else
            qualityOrLatency = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY;
    }

    private void observeEleve(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        // Observer la liste de tous les élèves du trombi si on est dans le mode correspondant
        if(mode == TAKE_ALL_PHOTO_MODE){
            // Cet observeur regarde la liste de tous les élèves, puis actualise la valeur
            // de l'élève à observé selon l'attribut index de la classe (voir bloc suivant)
            trombiViewModel.getElevesByTrombi(idTrombi).observe(this, eleves -> {
                listEleves = eleves;

                // Le mode capture de classe est lancé alors qu'il n'y a pas d'élève dans le trombi
                if(eleves.isEmpty()){
                    showToast(getResources().getText(R.string.CAPT_noEleve));
                    finish();
                    return;
                }

                trombiViewModel.setIdEleve(listEleves.get(index).getIdEleve());
            });
        } else if(mode == TAKE_PHOTO_MODE){
            trombiViewModel.setIdEleve(idEleve);
            buttonNext.setVisibility(View.GONE);
            buttonPrevious.setVisibility(View.GONE);
        }

        // eleveForPhoto est une transformation, voir TrombiViewModel pour plus d'informations
        trombiViewModel.eleveForPhoto.observe(ActivityCapture.this, eleve -> {
            // Ne pas déclencher l'animation si on est sur le même élève et qu'il reçoit une photo
            if(currentEleve == null || eleve.getIdEleve() != currentEleve.getIdEleve())
                updateUI(eleve);

            currentEleve = eleve;
            checkForExistingPhoto();

        });

    }

    // Listener du fab dans ActivityCapture#bindPreview
    private void setListeners(){
        buttonNext.setOnClickListener(view -> nextEleve());

        buttonPrevious.setOnClickListener(view -> {
            if(mode != TAKE_ALL_PHOTO_MODE)
                switchMode(TAKE_ALL_PHOTO_MODE);

            index--;

            // Changement de l'id de l'élève à observer, donc changement de la valeur currentEleve
            if(index >= 0){
                trombiViewModel.setIdEleve(listEleves.get(index).getIdEleve());
            } else{
                showToast(getResources().getText(R.string.CAPT_firstEleve));
                index++;
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

        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setCaptureMode(qualityOrLatency)
                //.setFlashMode(ImageCapture.FLASH_MODE_ON)
                .build();

        fab.setOnClickListener(view -> {
            if(mode == EDIT_MODE)
                saveEditedImage();

            else if(mode == TAKE_PHOTO_MODE || mode == TAKE_ALL_PHOTO_MODE)
                saveImage(imageCapture);

            else
                throw new IllegalStateException("Mode inconnu");
        });

        // Le paramètre imageCapture correspond à un useCase qui envisage la prise d'une photo
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    // Enregistre l'image prise depuis la caméra
    private void saveImage(ImageCapture imageCapture){
        if(fileUtil == null){
            Logger.logV("NPE", "fileUtil is null");
            return;
        }

        String path = fileUtil.getPathNameForEleve(currentEleve);
        File f = new File(path);

        imageCapture.takePicture(f,
                Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull File file){
                        imageToDelete = file;
                        loadImageEditor(file);
                    }

                    @Override
                    public void onError(int imageCaptureError,
                                        @NonNull String message,
                                        @Nullable Throwable cause){
                        showToast(getResources().getText(R.string.CAPT_error));
                    }
                });
    }

    // Fait apparaître l'outil d'édition de la photo prise
    private void loadImageEditor(File capturedImage){
        switchMode(EDIT_MODE);

        editImage.post(() -> {
            editImage.setAspectRatio(1, 1);
            editImage.setFixedAspectRatio(isFixedRatio);
            editImage.setImageUriAsync(Uri.fromFile(capturedImage));
        });

        /*runOnUiThread(() -> {
            //editImage.setCropShape(CropImageView.CropShape.OVAL);     // https://github.com/ArthurHub/Android-Image-Cropper/issues/553

        });*/
    }

    private void saveEditedImage(){
        Bitmap bitmap = editImage.getCroppedImage();

        if(fileUtil == null)
            return;

        CompletableFuture.supplyAsync(() ->
                fileUtil.savePhotoForEleve(bitmap, currentEleve))
                .exceptionally(throwable -> null)
                .thenApply(this::changeElevePhoto)
                .thenAccept(this::alertImageSaved)
                .thenRun(() -> imageToDelete.delete())
                .thenRun(this::nextEleveOrFinish);
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
        if(photo != null && fileUtil != null){
            // Supprime la photo actuelle de l'élève du système de fichier
            fileUtil.deletePhotoForEleve(currentEleve);

            currentEleve.setPhoto(Uri.fromFile(photo).toString());
            trombiViewModel.update(currentEleve);
            return true;
        }

        return false;
    }

    // Passe à l'élève suivant ou termine l'activité si on est pas en mode TAKE_ALL_PHOTO
    private void nextEleveOrFinish(){
        if(listEleves == null)
            finish();
        else
            nextEleve();
    }

    // Passe à l'élève suivant
    private void nextEleve(){
        if(mode != TAKE_ALL_PHOTO_MODE)
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

    // Attribue les bonnes valeures aux champs de l'UI et déclenche une animation
    // isNextDétermine le sens de l'animation, 1 pour suivant, ou -1 pour précédent
    private void updateUI(Eleve eleve){
        tvName.setText(eleve.getNomPrenom());

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

        // Le LinearLayout descends de la taille de la bannière
        banner.post(() -> linearLayout.animate()
                .translationY(banner.getHeight())
                .setDuration(300));

        // Mise en place des listeners
        buttonDismiss.setOnClickListener(view -> hideBanner());

        // Si on est en photo de classe, on passe à l'élève suivant sinon on sort☺
        buttonNextIfPhoto.setOnClickListener(view -> buttonNext.callOnClick());
    }

    private void hideBanner(){
        banner.animate()
                .translationY(-banner.getHeight())
                .setDuration(300);

        banner.postDelayed(() -> banner.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(0), 400);

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
